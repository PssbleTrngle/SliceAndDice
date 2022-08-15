package com.possible_triangle.thermomix.block.tile

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.config.Configs
import com.possible_triangle.thermomix.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.PressingBehaviourSpecifics
import com.simibubi.create.content.contraptions.processing.InWorldProcessing
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour
import com.simibubi.create.foundation.utility.VecHelper
import com.simibubi.create.foundation.utility.recipe.RecipeFinder
import net.minecraft.core.BlockPos
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
    MechanicalMixerTileEntity(type, pos, state), PressingBehaviourSpecifics {

    companion object {
        private val inWorldCacheKey = Any()
    }

    var heldItem = ItemStack.EMPTY
    private lateinit var pressingBehaviour: PressingBehaviour

    override fun addBehaviours(behaviours: MutableList<TileEntityBehaviour>) {
        super.addBehaviours(behaviours)
        pressingBehaviour = PressingBehaviour(this)
        behaviours.add(pressingBehaviour)
    }

    override fun getMatchingRecipes(): MutableList<Recipe<*>> {
        if (!heldItem.`is`(Content.ALLOWED_TOOLS)) return mutableListOf()
        val recipes = super.getMatchingRecipes()
        return recipes.mapNotNull {
            if (it is CuttingProcessingRecipe) it.takeIf {
                it.tool!!.test(heldItem)
            }
            else it
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
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        super.write(compound, clientPacket)
        if (!heldItem.isEmpty) {
            val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, heldItem).result()
            encoded.ifPresent { compound.put("HeldItem", it) }
        }
    }

    override fun renderParticles() {
        super.renderParticles()
        if (level!!.gameTime % 12 != 0L) return
        //cuttingParticles()
    }

    private fun cuttingParticles() {
        val world = level ?: return
        val center: Vec3 = VecHelper.getCenterOf(worldPosition.below(1))
        world.addParticle(
            ParticleTypes.SWEEP_ATTACK,
            center.x,
            center.y + 0.2 + world.random.nextDouble() * 0.3,
            center.z,
            world.random.nextDouble() - 0.5,
            0.1,
            world.random.nextDouble() - 0.5
        )
    }

    override fun getRenderedHeadOffset(partialTicks: Float): Float {
        //if(pressingBehaviour.mode == PressingBehaviour.Mode.BASIN) {
        //    return super.getRenderedHeadOffset(partialTicks)
        //}
        return pressingBehaviour.getRenderedHeadOffset(partialTicks) * 0.6F + 0.4F * pressingBehaviour.mode.headOffset
    }

    private fun recipeFor(stack: ItemStack): CuttingProcessingRecipe? {
        val recipes = RecipeFinder.get(inWorldCacheKey, level) {
            if (it !is CuttingProcessingRecipe) false
            else it.ingredients.size == 1 && it.fluidIngredients.isEmpty() && it.tool != null
        } as List<CuttingProcessingRecipe>
        return recipes.firstOrNull { it.ingredients[0].test(stack) && it.tool!!.test(heldItem) }
    }

    override fun tryProcessInBasin(simulate: Boolean): Boolean {
        return true
        //matchingRecipes.firstOrNull() ?: return false
        //applyBasinRecipe()
        //return true
    }

    override fun tryProcessOnBelt(
        input: TransportedItemStack,
        outputList: MutableList<ItemStack>?,
        simulate: Boolean,
    ): Boolean {
        val recipe = recipeFor(input.stack) ?: return false
        if (simulate) return true

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
        pressingBehaviour.start(PressingBehaviour.Mode.BASIN)
    }

    override fun onPressingCompleted() {
        cuttingParticles()

        val canContinue = pressingBehaviour.onBasin()
                && matchBasinRecipe(currentRecipe)
                && basin.filter { it.canContinueProcessing() }.isPresent

        if (canContinue) startProcessingBasin()
        else basinChecker.scheduleUpdate()
    }

    override fun getParticleAmount() = 10

    override fun getKineticSpeed() = getSpeed()

}