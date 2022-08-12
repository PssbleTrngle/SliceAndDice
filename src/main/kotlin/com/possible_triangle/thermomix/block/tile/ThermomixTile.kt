package com.possible_triangle.thermomix.block.tile

import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.crafting.IShapedRecipe

class ThermomixTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    MechanicalMixerTileEntity(type, pos, state) {

    override fun <C : Container> matchStaticFilters(recipe: Recipe<C>): Boolean {
        return recipe is IShapedRecipe
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