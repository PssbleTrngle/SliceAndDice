package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Constants
import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.platform.Services
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.Mode
import com.simibubi.create.content.contraptions.components.press.PressingBehaviour.PressingBehaviourSpecifics
import com.simibubi.create.content.contraptions.processing.BasinTileEntity
import com.simibubi.create.content.contraptions.processing.InWorldProcessing
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack
import com.simibubi.create.foundation.item.TooltipHelper
import com.simibubi.create.foundation.tileEntity.behaviour.simple.DeferralBehaviour
import com.simibubi.create.foundation.utility.Lang
import com.simibubi.create.foundation.utility.VecHelper
import com.simibubi.create.foundation.utility.recipe.RecipeFinder
import net.minecraft.ChatFormatting
import net.minecraft.client.resources.language.I18n
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

//abstract class AbstractSlicerTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
//    BasinOperatingTileEntity(type, pos, state), PressingBehaviourSpecifics {


interface AbstractSlicerTile : PressingBehaviourSpecifics {

    companion object {
        private val inWorldCacheKey = Any()
        val cacheKey = Any()
    }

    fun getSpeed(): Float
    fun isRunning(): Boolean
    fun isSpeedRequirementFulfilled(): Boolean

    val correctDirection get() = Services.CONFIG.IGNORE_ROTATION || getSpeed() < 0
    val canProcess get() = correctDirection && isSpeedRequirementFulfilled()

    val cuttingBehaviour: PressingBehaviour
    var currentRecipe: Recipe<*>

    var heldItem: ItemStack

    fun getLevel(): Level?
    fun getBlockPos(): BlockPos

    val basin: BasinTileEntity?
    val basinChecker: DeferralBehaviour

    fun applyBasinRecipe()
    fun startProcessingBasin()
    fun sendData()

    fun testBasinRecipe(recipe: Recipe<*>): Boolean

    fun addSlicerTooltip(tooltip: MutableList<Component>, blockState: BlockState): Boolean {
        if (!correctDirection && getSpeed() != 0F) {
            Lang.builder(Constants.MOD_ID)
                .translate("tooltip.rotationDirection")
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip)
            val hint = Lang.builder(Constants.MOD_ID)
                .translate("gui.contraptions.wrong_direction", I18n.get(blockState.block.descriptionId))
                .component()
            val cutString = TooltipHelper.cutTextComponent(hint, ChatFormatting.GRAY, ChatFormatting.WHITE)
            for (i in cutString.indices) {
                Lang.builder().add(cutString[i].copy()).forGoggles(tooltip)
            }
            return true
        }
        return false
    }

    fun filterRecipes(recipes: Collection<Recipe<*>>): MutableList<Recipe<*>> {
        if (!heldItem.`is`(Content.ALLOWED_TOOLS)) return mutableListOf()
        return recipes.mapNotNull {
            it.takeIf { hasRequiredTool(it) }
        }.toMutableList()
    }

    fun applyRecipe() {
        val world = getLevel() ?: return
        if (world is ServerLevel && Services.CONFIG.CONSUME_DURABILTY) {
            if (heldItem.hurt(1, world.random, null)) {
                heldItem = ItemStack.EMPTY
                sendData()
            }
        }
    }

    fun readAdditional(compound: CompoundTag, clientPacket: Boolean) {
        heldItem = compound.get("HeldItem").let {
            val decoded = ItemStack.CODEC.parse(NbtOps.INSTANCE, it).result()
            decoded.orElse(ItemStack.EMPTY)
        }

        if (clientPacket && cuttingBehaviour.mode != Mode.BASIN && compound.contains("ParticleItems", 9)) {
            val particles = compound.getList("ParticleItems", 10)
            if (particles.isNotEmpty()) cuttingParticles()
        }
    }

    fun writeAdditional(compound: CompoundTag, clientPacket: Boolean) {
        if (!heldItem.isEmpty) {
            val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, heldItem).result()
            encoded.ifPresent { compound.put("HeldItem", it) }
        }
    }

    fun cuttingParticles() {
        val world = getLevel() ?: return
        val center: Vec3 = VecHelper.getCenterOf(getBlockPos().below(2))
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
        val modeOffset = when (cuttingBehaviour.mode) {
            Mode.BASIN -> 0.8F
            Mode.BELT -> 0.4F
            else -> 1.0F
        }
        val base = cuttingBehaviour.getRenderedHeadOffset(partialTicks)
        return base * modeOffset + 0.4F
    }

    private fun recipeFor(stack: ItemStack): CuttingProcessingRecipe? {
        val recipes = RecipeFinder.get(inWorldCacheKey, getLevel()) {
            if (it !is CuttingProcessingRecipe) false
            else it.ingredients.size == 1 && it.fluidIngredients.isEmpty() && it.tool != null
        } as List<CuttingProcessingRecipe>
        return recipes.firstOrNull { it.ingredients[0].test(stack) && it.tool!!.test(heldItem) }
    }

    override fun tryProcessInBasin(simulate: Boolean): Boolean {
        if (!canProcess) return false
        applyBasinRecipe()

        basin?.let {
            val inputs = it.getInputInventory()
            for (slot in 0 until inputs.slots) {
                val stackInSlot = inputs.getItem(slot)
                if (stackInSlot.isEmpty) continue
                cuttingBehaviour.particleItems.add(stackInSlot)
            }
        }

        return true
    }

    override fun tryProcessOnBelt(
        input: TransportedItemStack,
        outputList: MutableList<ItemStack>?,
        simulate: Boolean,
    ): Boolean {
        if (!canProcess) return false
        val recipe = recipeFor(input.stack) ?: return false
        if (simulate) return true

        cuttingBehaviour.particleItems.add(input.stack)

        val toProcess = if (canProcessInBulk()) input.stack else input.stack.copy().apply { count = 1 }
        val outputs = InWorldProcessing.applyRecipeOn(toProcess, recipe)
        outputList?.addAll(outputs)
        return true
    }

    override fun tryProcessInWorld(itemEntity: ItemEntity, simulate: Boolean): Boolean {
        return false
    }

    override fun canProcessInBulk() = false

    private fun hasRequiredTool(recipe: Recipe<*>): Boolean {
        return recipe !is CuttingProcessingRecipe || recipe.tool?.test(heldItem) == true
    }

    override fun onPressingCompleted() {
        val canContinue = cuttingBehaviour.onBasin()
                && testBasinRecipe(currentRecipe)
                && hasRequiredTool(currentRecipe)
                && basin?.canContinueProcessing() == true

        if (canContinue) startProcessingBasin()
        else basinChecker.scheduleUpdate()
    }

    override fun getParticleAmount() = 10

    override fun getKineticSpeed() = getSpeed()

    fun onBasinRemoved() {
        cuttingBehaviour.particleItems.clear()
        cuttingBehaviour.running = false
        cuttingBehaviour.runningTicks = 0
        sendData()
    }

    fun getRenderedHeadRotationSpeed(): Float {
        val speed = getSpeed()
        return if (isRunning()) {
            if (cuttingBehaviour.runningTicks <= 20) {
                speed * 2
            } else speed
        } else {
            speed / 2
        }
    }

}