package com.possible_triangle.thermomix.block.tile

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.config.Configs
import com.possible_triangle.thermomix.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.Mode
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.PressingBehaviourSpecifics
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity
import com.simibubi.create.content.contraptions.processing.InWorldProcessing
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour
import com.simibubi.create.foundation.utility.VecHelper
import com.simibubi.create.foundation.utility.recipe.RecipeFinder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraftforge.items.ItemHandlerHelper


class ThermomixTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    BasinOperatingTileEntity(type, pos, state), PressingBehaviourSpecifics {

    companion object {
        private val inWorldCacheKey = Any()
        private val basinCacheKey = Any()
    }

    override fun getRecipeCacheKey() = basinCacheKey

    var heldItem = ItemStack.EMPTY
    private lateinit var behaviour: PressingBehaviour

    override fun addBehaviours(behaviours: MutableList<TileEntityBehaviour>) {
        super.addBehaviours(behaviours)
        behaviour = PressingBehaviour(this)
        behaviours.add(behaviour)
    }

    override fun getMatchingRecipes(): MutableList<Recipe<*>> {
        if (!heldItem.`is`(Content.ALLOWED_TOOLS)) return mutableListOf()
        val recipes = super.getMatchingRecipes()
        return recipes.mapNotNull {
            it.takeIf { hasRequiredTool(it) }
        }.toMutableList()
    }

    override fun applyBasinRecipe() {
        super.applyBasinRecipe()
        val world = level ?: return
        if (world is ServerLevel && Configs.SERVER.CONSUME_DURABILTY.get()) {
            if (heldItem.hurt(1, level!!.random, null)) {
                heldItem = ItemStack.EMPTY
                sendData()
            }
        }
    }

    override fun <C : Container> matchStaticFilters(recipe: Recipe<C>): Boolean {
        if (recipe !is CuttingProcessingRecipe) return false
        return recipe.tool != null && recipe.tool.items.any { it.`is`(Content.ALLOWED_TOOLS) }
    }

    override fun read(compound: CompoundTag, clientPacket: Boolean) {
        super.read(compound, clientPacket)
        heldItem = compound.get("HeldItem").let {
            val decoded = ItemStack.CODEC.parse(NbtOps.INSTANCE, it).result()
            decoded.orElse(ItemStack.EMPTY)
        }

        if (clientPacket && behaviour.mode != Mode.BASIN && compound.contains("ParticleItems", 9)) {
            val particles = compound.getList("ParticleItems", 10)
            if (particles.isNotEmpty()) cuttingParticles()
        }
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        super.write(compound, clientPacket)
        if (!heldItem.isEmpty) {
            val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, heldItem).result()
            encoded.ifPresent { compound.put("HeldItem", it) }
        }
    }

    private fun cuttingParticles() {
        val world = level ?: return

        val center: Vec3 = VecHelper.getCenterOf(worldPosition.below(2))
        world.addParticle(
            ParticleTypes.SWEEP_ATTACK,
            center.x,
            center.y + 0.2 + world.random.nextDouble() * 0.3,
            center.z,
            world.random.nextDouble() - 0.5,
            0.1,
            world.random.nextDouble() - 0.5,
        )
    }

    fun getRenderedHeadOffset(partialTicks: Float): Float {
        val modeOffset = when (behaviour.mode) {
            Mode.BASIN -> 0.8F
            Mode.BELT -> 0.4F
            else -> 1.0F
        }
        val base = behaviour.getRenderedHeadOffset(partialTicks)
        return base * modeOffset + 0.4F
    }

    private fun recipeFor(stack: ItemStack): CuttingProcessingRecipe? {
        val recipes = RecipeFinder.get(inWorldCacheKey, level) {
            if (it !is CuttingProcessingRecipe) false
            else it.ingredients.size == 1 && it.fluidIngredients.isEmpty() && it.tool != null
        } as List<CuttingProcessingRecipe>
        return recipes.firstOrNull { it.ingredients[0].test(stack) && it.tool!!.test(heldItem) }
    }

    override fun tryProcessInBasin(simulate: Boolean): Boolean {
        applyBasinRecipe()

        basin.ifPresent {
            val inputs = it.getInputInventory()
            for (slot in 0 until inputs.slots) {
                val stackInSlot = inputs.getItem(slot)
                if (stackInSlot.isEmpty) continue
                behaviour.particleItems.add(stackInSlot)
            }
        }

        return true
    }

    override fun tryProcessOnBelt(
        input: TransportedItemStack,
        outputList: MutableList<ItemStack>?,
        simulate: Boolean,
    ): Boolean {
        val recipe = recipeFor(input.stack) ?: return false
        if (simulate) return true

        behaviour.particleItems.add(input.stack)

        val toProcess = if (canProcessInBulk()) input.stack else ItemHandlerHelper.copyStackWithSize(input.stack, 1)
        val outputs = InWorldProcessing.applyRecipeOn(toProcess, recipe)
        outputList?.addAll(outputs)
        return true
    }

    override fun tryProcessInWorld(itemEntity: ItemEntity, simulate: Boolean): Boolean {
        return false
    }

    override fun canProcessInBulk() = false

    override fun startProcessingBasin() {
        behaviour.start(Mode.BASIN)
    }

    private fun hasRequiredTool(recipe: Recipe<*>): Boolean {
        return recipe !is CuttingProcessingRecipe || recipe.tool?.test(heldItem) == true
    }

    override fun tick() {
        super.tick()
        if (isRunning) {

        }
    }

    private fun renderParticles() {
        if (level == null) return
        behaviour.particleItems.filterNot { it.isEmpty }.forEach { stack ->
            val data = ItemParticleOption(ParticleTypes.ITEM, stack)
            spillParticle(data)
        }
    }

    private fun spillParticle(data: ParticleOptions?) {
        val angle = level!!.random.nextFloat() * 360
        var offset = Vec3(0.0, 0.0, 0.25)
        offset = VecHelper.rotate(offset, angle.toDouble(), Direction.Axis.Y)
        var target = VecHelper.rotate(offset, (if (getSpeed() > 0) 25 else -25).toDouble(), Direction.Axis.Y)
            .add(0.0, .25, 0.0)
        val center = offset.add(VecHelper.getCenterOf(worldPosition))
        target = VecHelper.offsetRandomly(target.subtract(offset), level!!.random, 1 / 128f)
        level!!.addParticle(data, center.x, center.y - 1.75f, center.z, target.x, target.y, target.z)
    }

    override fun onPressingCompleted() {
        val canContinue = behaviour.onBasin()
                && matchBasinRecipe(currentRecipe)
                && hasRequiredTool(currentRecipe)
                && basin.filter { it.canContinueProcessing() }.isPresent

        if (canContinue) startProcessingBasin()
        else basinChecker.scheduleUpdate()
    }

    override fun getParticleAmount() = 10

    override fun getKineticSpeed() = getSpeed()

    override fun onBasinRemoved() {
        behaviour.particleItems.clear()
        behaviour.running = false
        behaviour.runningTicks = 0
        sendData()
    }

    override fun isRunning(): Boolean {
        return behaviour.running
    }

    fun getRenderedHeadRotationSpeed(): Float {
        val speed = getSpeed()
        return if (isRunning) {
            if (behaviour.runningTicks <= 20) {
                speed * 2
            } else speed
        } else {
            speed / 2
        }
    }

}