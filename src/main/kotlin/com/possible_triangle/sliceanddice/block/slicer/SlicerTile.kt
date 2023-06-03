package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.kinetics.press.PressingBehaviour
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode
import com.simibubi.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.item.TooltipHelper
import com.simibubi.create.foundation.recipe.RecipeApplier
import com.simibubi.create.foundation.recipe.RecipeFinder
import com.simibubi.create.foundation.utility.Lang
import com.simibubi.create.foundation.utility.VecHelper
import net.minecraft.ChatFormatting
import net.minecraft.client.resources.language.I18n
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.ItemHandlerHelper


class SlicerTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    BasinOperatingBlockEntity(type, pos, state), PressingBehaviourSpecifics {

    companion object {
        private val inWorldCacheKey = Any()
        private val basinCacheKey = Any()
    }

    override fun getRecipeCacheKey() = basinCacheKey

    val correctDirection get() = Configs.SERVER.IGNORE_ROTATION.get() || getSpeed() < 0
    val canProcess get()  = correctDirection && isSpeedRequirementFulfilled

    private var _heldItem = ItemStack.EMPTY
    var heldItem: ItemStack
        get() = _heldItem
        set(value) {
            _heldItem = value
            sendData()
        }

    private var invHandler: LazyOptional<SlicerItemHandler>? = null

    override fun initialize() {
        super.initialize()
        initHandler()
    }

    private fun initHandler() {
        if (invHandler == null) {
            invHandler = LazyOptional.of { SlicerItemHandler(this) }
        }
    }

    override fun updateBasin(): Boolean {
        return !correctDirection || super.updateBasin()
    }

    override fun addToTooltip(tooltip: MutableList<Component>?, isPlayerSneaking: Boolean): Boolean {
        if (super.addToTooltip(tooltip, isPlayerSneaking)) return true
        if (!correctDirection && speed != 0F) {
            Lang.builder(SliceAndDice.MOD_ID)
                .translate("tooltip.rotationDirection")
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip)
            val hint = Lang.builder(SliceAndDice.MOD_ID)
                .translate("gui.contraptions.wrong_direction", I18n.get(blockState.block.descriptionId))
                .component()
            val cutString = TooltipHelper.cutTextComponent(hint, TooltipHelper.Palette.GRAY)
            for (i in cutString.indices) {
                Lang.builder().add(cutString[i].copy()).forGoggles(tooltip)
            }
            return true
        }
        return false
    }

    private lateinit var behaviour: PressingBehaviour
    val cuttingBehaviour get() = behaviour

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        super.addBehaviours(behaviours)
        behaviour = PressingBehaviour(this)
        behaviours.add(behaviour)
    }

    override fun getMatchingRecipes(): MutableList<Recipe<*>> {
        if (!_heldItem.`is`(Content.ALLOWED_TOOLS)) return mutableListOf()
        val recipes = super.getMatchingRecipes()
        return recipes.mapNotNull {
            it.takeIf { hasRequiredTool(it) }
        }.toMutableList()
    }

    override fun applyBasinRecipe() {
        super.applyBasinRecipe()
        val world = level ?: return
        if (world is ServerLevel && Configs.SERVER.CONSUME_DURABILTY.get()) {
            if (_heldItem.hurt(1, level!!.random, null)) {
                _heldItem = ItemStack.EMPTY
                sendData()
            }
        }
    }

    override fun <C : Container> matchStaticFilters(recipe: Recipe<C>): Boolean {
        if (recipe !is CuttingProcessingRecipe) return false
        return recipe.tool != null //&& recipe.tool.items.any { it.`is`(Content.ALLOWED_TOOLS) }
    }

    override fun read(compound: CompoundTag, clientPacket: Boolean) {
        super.read(compound, clientPacket)
        _heldItem = compound.get("HeldItem").let {
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

    @Suppress("UNCHECKED_CAST")
    private fun recipeFor(stack: ItemStack): CuttingProcessingRecipe? {
        val recipes = RecipeFinder.get(inWorldCacheKey, level) {
            if (it !is CuttingProcessingRecipe) false
            else it.ingredients.size == 1 && it.fluidIngredients.isEmpty() && it.tool != null
        } as List<CuttingProcessingRecipe>
        return recipes.firstOrNull { it.ingredients[0].test(stack) && it.tool!!.test(heldItem) }
    }

    override fun tryProcessInBasin(simulate: Boolean): Boolean {
        if(!canProcess) return false
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
        if(!canProcess) return false
        val recipe = recipeFor(input.stack) ?: return false
        if (simulate) return true

        behaviour.particleItems.add(input.stack)

        val toProcess = if (canProcessInBulk()) input.stack else ItemHandlerHelper.copyStackWithSize(input.stack, 1)
        val outputs = RecipeApplier.applyRecipeOn(toProcess, recipe)
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

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return if (isItemHandlerCap(cap)) {
            if (this.invHandler == null) this.initHandler()
            this.invHandler!!.cast()
        } else {
            super.getCapability(cap, side)
        }
    }

}