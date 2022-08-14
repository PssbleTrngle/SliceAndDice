package com.possible_triangle.thermomix.block.tile

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.ThermomixMod
import com.possible_triangle.thermomix.config.Configs
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity
import com.simibubi.create.content.contraptions.processing.BasinRecipe
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe

private fun CuttingBoardRecipe.toBasin(): BasinRecipe {
    val basinId = ResourceLocation(ThermomixMod.MOD_ID, "${id.namespace}_${id.path}")
    val builder = ProcessingRecipeBuilder(::BasinRecipe, basinId)
    ingredients.forEach { builder.require(it) }
    rollableResults.forEach { builder.output(it.chance, it.stack) }
    return builder.build()
}

class ThermomixTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    MechanicalMixerTileEntity(type, pos, state) {

    var heldItem = ItemStack.EMPTY

    override fun getRecipeCacheKey(): Any? {
        return null
    }

    override fun getMatchingRecipes(): MutableList<Recipe<*>> {
        val recipes = super.getMatchingRecipes()
        return recipes.mapNotNull {
            if (it is CuttingBoardRecipe) it.takeIf {
                it.tool.test(heldItem)
            }?.toBasin()
            else it
        }.toMutableList()
    }

    override fun applyBasinRecipe() {
        super.applyBasinRecipe()
        if (Configs.SERVER.CONSUME_DURABILTY.get()) {
            if (heldItem.hurt(1, level!!.random, null)) {
                heldItem = ItemStack.EMPTY
                sendData()
            }
        }
    }

    override fun <C : Container> matchStaticFilters(recipe: Recipe<C>): Boolean {
        if (recipe !is CuttingBoardRecipe) return false
        return recipe.tool.items.any { it.`is`(Content.ALLOWED_TOOLS) }
    }

    override fun read(compound: CompoundTag, clientPacket: Boolean) {
        super.read(compound, clientPacket)
        if (!heldItem.isEmpty) {
            val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, heldItem).result()
            encoded.ifPresent { compound.put("HeldItem", it) }
        }
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        super.write(compound, clientPacket)
        heldItem = compound.get("HeldItem").let {
            val decoded = ItemStack.CODEC.parse(NbtOps.INSTANCE, it).result()
            decoded.orElse(ItemStack.EMPTY)
        }
    }

    /*
    override fun tick() {
        super.tick()
        if (runningTicks >= 40) {
            running = false
            runningTicks = 0
            basinChecker.scheduleUpdate()
        } else {
            if (running) runningLogic()
        }
    }

    private fun runningLogic() {
        val world = level ?: return
        val speed = abs(getSpeed())

        if (runningTicks != 20) {
            ++runningTicks
            return
        }

        if (world.isClientSide) this.particles()

        if (!world.isClientSide || this.isVirtual) {
            if (this.processingTicks < 0) {
                val recipeSpeed = 1.0F

                this.processingTicks =
                    Mth.clamp(Mth.log2((512.0F / speed).toInt()) * Mth.ceil(recipeSpeed * 15.0F) + 1, 1, 512)
                basin.ifPresent { basin ->
                    val tanks = basin.tanks
                    val hasFluid = listOf(tanks.first, tanks.second).any { !it.isEmpty }
                    if (hasFluid) {
                        world.playSound(
                            null,
                            blockPos,
                            SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                            SoundSource.BLOCKS,
                            0.75f,
                            if (speed < 65.0f) 0.75f else 1.5f
                        )
                    }
                }
            } else {
                --this.processingTicks
                if (this.processingTicks == 0) {
                    ++runningTicks
                    this.processingTicks = -1
                    applyBasinRecipe()
                    sendData()
                }
            }
        }
    }

    private fun particles() {}

    override fun read(compound: CompoundTag, clientPacket: Boolean) {
        super.read(compound, clientPacket)
        running = compound.getBoolean("Running")
        runningTicks = compound.getInt("Ticks")
        //if (clientPacket && hasLevel()) {
        //    this.basin.ifPresent {
        //        it.setAreFluidsMoving(
        //            running && this.runningTicks <= 20
        //        )
        //    }
        //}
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        compound.putBoolean("Running", running)
        compound.putInt("Ticks", runningTicks)
        super.write(compound, clientPacket)
    }
    */
}