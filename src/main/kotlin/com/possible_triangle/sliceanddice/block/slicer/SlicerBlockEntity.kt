package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.compat.ModCompat
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.kinetics.press.PressingBehaviour
import com.simibubi.create.content.kinetics.press.PressingBehaviour.Mode
import com.simibubi.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics
import com.simibubi.create.content.processing.basin.BasinOperatingBlockEntity
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.item.TooltipHelper
import com.simibubi.create.foundation.recipe.RecipeApplier
import com.simibubi.create.foundation.recipe.RecipeFinder
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper
import net.createmod.catnip.lang.FontHelper
import net.createmod.catnip.lang.Lang
import net.createmod.catnip.math.VecHelper
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.language.I18n
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Container
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.streams.asSequence

class SlicerBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    BasinOperatingBlockEntity(type, pos, state), PressingBehaviourSpecifics, SidedStorageBlockEntity {

    companion object {
        private val inWorldCacheKey = Any()
        private val basinCacheKey = Any()
    }

    override fun getRecipeCacheKey() = basinCacheKey

    val correctDirection get() = Configs.SERVER.IGNORE_ROTATION.get() || getSpeed() < 0
    val canProcess get() = correctDirection && isSpeedRequirementFulfilled

    private lateinit var behaviour: PressingBehaviour
    val cuttingBehaviour get() = behaviour

    private var _heldItem = ItemStack.EMPTY
    var heldItem: ItemStack
        get() = _heldItem
        set(value) {
            _heldItem = value
            basinChecker.scheduleUpdate()
            itemHandler.update()
            sendData()
        }
    private val itemHandler = SlicerItemHandler(this)

    private var playSound = false

    override fun updateBasin(): Boolean {
        return !correctDirection || super.updateBasin()
    }

    override fun addToTooltip(tooltip: MutableList<Component>, isPlayerSneaking: Boolean): Boolean {
        if (super.addToTooltip(tooltip, isPlayerSneaking)) return true
        if (!correctDirection && speed != 0F) {
            Lang.builder(SliceAndDice.MOD_ID)
                .translate("tooltip.rotationDirection")
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip)
            val hint = Lang.builder(SliceAndDice.MOD_ID)
                .translate("gui.contraptions.wrong_direction", I18n.get(blockState.block.descriptionId))
                .component()
            val cutString = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY)
            for (i in cutString.indices) {
                Lang.builder(SliceAndDice.MOD_ID).add(cutString[i].copy()).forGoggles(tooltip)
            }
            return true
        }
        return false
    }

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

    private fun consumeDurability() {
        val world = level ?: return
        if (world is ServerLevel && Configs.SERVER.CONSUME_DURABILTY.get()) {
            if (_heldItem.hurt(1, level!!.random, null)) {
                _heldItem = ItemStack.EMPTY
                sendData()
            }
        }
    }

    override fun applyBasinRecipe() {
        super.applyBasinRecipe()
        consumeDurability()
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

        if (clientPacket) {
            compound.handleParticles()
            if (compound.getBoolean("PlaySound")) playSound()
        }

        super.read(compound, clientPacket)
    }

    private fun CompoundTag.handleParticles() {
        if (!contains("ParticleItems", 9)) return

        val particles = getList("ParticleItems", 10)
        if (particles.isEmpty()) {
            remove("ParticleItems")
        } else {
            if (behaviour.mode != Mode.BASIN) cuttingParticles()

            if (Configs.CLIENT.spawnBloodParticles) {
                particles.clear()
                particles.add(ItemStack(Items.REDSTONE).serializeNBT())
                particles.add(ItemStack(Items.RED_DYE).serializeNBT())
            }
        }
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        super.write(compound, clientPacket)
        if (!_heldItem.isEmpty) {
            val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, _heldItem).result()
            encoded.ifPresent { compound.put("HeldItem", it) }
        }
        if (clientPacket) {
            compound.putBoolean("PlaySound", playSound)
            playSound = false
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
        val assemblyRecipe = SequencedAssemblyRecipe.getRecipes(
            level,
            stack,
            CuttingProcessingRecipe.getType(),
            CuttingProcessingRecipe::class.java
        ).asSequence()
            .filter { it.tool?.test(_heldItem) == true }
            .firstOrNull()

        if (assemblyRecipe != null) return assemblyRecipe

        val recipes = RecipeFinder.get(inWorldCacheKey, level) {
            if (it !is CuttingProcessingRecipe) false
            else it.ingredients.size == 1 && it.fluidIngredients.isEmpty() && it.tool != null
        } as List<CuttingProcessingRecipe>
        return recipes.firstOrNull { it.ingredients[0].test(stack) && it.tool!!.test(_heldItem) }
    }

    private fun addToParticleItems(stack: ItemStack) {
        behaviour.particleItems.add(stack)
    }

    override fun tryProcessInBasin(simulate: Boolean): Boolean {
        if (!canProcess) return false
        applyBasinRecipe()

        basin.ifPresent {
            val inputs = it.getInputInventory()
            for (slot in 0 until inputs.slotCount) {
                val stackInSlot = inputs.getItem(slot)
                if (stackInSlot.isEmpty) continue
                addToParticleItems(stackInSlot)
            }
        }

        tryContinueWithPreviousRecipe()

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

        addToParticleItems(input.stack)

        val toProcess = if (canProcessInBulk()) input.stack else ItemHandlerHelper.copyStackWithSize(input.stack, 1)
        val world = this.level ?: return false
        val outputs = RecipeApplier.applyRecipeOn(world, toProcess, recipe)
        outputList?.addAll(outputs)
        consumeDurability()
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
        return recipe !is CuttingProcessingRecipe || recipe.tool?.test(_heldItem) == true
    }

    private fun tryContinueWithPreviousRecipe(): Boolean {
        return if (behaviour.onBasin()
            && matchBasinRecipe(currentRecipe)
            && basin.filter { it.canContinueProcessing() }.isPresent
        ) {
            continueWithPreviousRecipe()
        } else {
            false
        }
    }

    override fun continueWithPreviousRecipe(): Boolean {
        val canContinue = hasRequiredTool(currentRecipe)
        if (canContinue) {
            behaviour.runningTicks = 100
        }
        return canContinue
    }

    override fun onPressingCompleted() {
        basinChecker.scheduleUpdate()
    }

    override fun getParticleAmount(): Int {
        return if (Configs.CLIENT.spawnBloodParticles) 20
        else 10
    }

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

    override fun getItemStorage(side: Direction?): Storage<ItemVariant> {
        return itemHandler
    }

    fun playSound() {
        val world = this.level ?: return
        if (world.isClientSide) {
            val player = Minecraft.getInstance().player
            if (Configs.CLIENT.spawnBloodParticles && world.random.nextInt(5) == 0) {
                world.playSound(player, worldPosition, SoundEvents.GOAT_DEATH, SoundSource.BLOCKS, 0.5F, 1F)
            }

            world.playSound(
                player,
                worldPosition,
                ModCompat.cuttingSound,
                SoundSource.BLOCKS,
                1F,
                world.random.nextFloat() * 0.2F + 0.9F
            )
        } else {
            playSound = true
        }
    }

}
