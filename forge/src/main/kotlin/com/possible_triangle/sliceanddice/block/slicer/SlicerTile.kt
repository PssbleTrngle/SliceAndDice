package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour
import com.simibubi.create.content.contraptions.processing.BasinOperatingTileEntity
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour
import com.simibubi.create.foundation.tileEntity.behaviour.simple.DeferralBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import kotlin.jvm.optionals.getOrNull

class SlicerTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    BasinOperatingTileEntity(type, pos, state), AbstractSlicerTile {

    override fun getRecipeCacheKey() = AbstractSlicerTile.cacheKey

    override var heldItem: ItemStack = ItemStack.EMPTY
        set(value) {
            field = value
            sendData()
        }

    private lateinit var behaviour: PressingBehaviour
    override val cuttingBehaviour get() = behaviour

    override val basin get() = getBasin().getOrNull()
    override val basinChecker: DeferralBehaviour get() = super.basinChecker
    override var currentRecipe: Recipe<*> = super.currentRecipe

    override fun addBehaviours(behaviours: MutableList<TileEntityBehaviour>) {
        super.addBehaviours(behaviours)
        behaviour = PressingBehaviour(this)
        behaviours.add(behaviour)
    }

    override fun getMatchingRecipes() = filterRecipes(super.getMatchingRecipes())

    override fun testBasinRecipe(recipe: Recipe<*>) = matchBasinRecipe(recipe)

    override fun updateBasin(): Boolean {
        return !correctDirection || super.updateBasin()
    }

    override fun applyBasinRecipe() {
        super.applyBasinRecipe()
        applyRecipe()
    }

    override fun isRunning(): Boolean {
        return cuttingBehaviour.running
    }

    override fun startProcessingBasin() {
        cuttingBehaviour.start(PressingBehaviour.Mode.BASIN)
    }

    override fun onBasinRemoved() {
        super.onBasinRemoved()
    }

    override fun read(compound: CompoundTag, clientPacket: Boolean) {
        super.read(compound, clientPacket)
        readAdditional(compound, clientPacket)
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        super.write(compound, clientPacket)
        writeAdditional(compound, clientPacket)
    }

    override fun addToTooltip(tooltip: MutableList<Component>, isPlayerSneaking: Boolean): Boolean {
        if (super.addToTooltip(tooltip, isPlayerSneaking)) return true
        return addSlicerTooltip(tooltip, blockState)
    }

    override fun <C : Container> matchStaticFilters(recipe: Recipe<C>): Boolean {
        if (recipe !is CuttingProcessingRecipe) return false
        return recipe.tool != null
    }

    override fun initialize() {
        super.initialize()
        initHandler()
    }

    // Forge Capabilities

    private var invHandler: LazyOptional<SlicerItemHandler>? = null

    private fun initHandler() {
        if (invHandler == null) {
            invHandler = LazyOptional.of { SlicerItemHandler(this) }
        }
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (this.invHandler == null) this.initHandler()
            this.invHandler!!.cast()
        } else {
            super.getCapability(cap, side)
        }
    }

}