package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.Content.WET_AIR
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.AirBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CropBlock
import net.minecraft.world.level.block.state.BlockState

class WetAir(
    properties: Properties,
) : AirBlock(properties) {
    companion object {
        @JvmStatic
        fun check(
            level: Level,
            pos: BlockPos,
        ): Boolean {
            val mutable = pos.mutable()
            var above = 1
            while (true) {
                mutable.y = pos.y + above
                val state = level.getBlockState(mutable)

                if (state.`is`(WET_AIR.get())) return true

                if (above > 2 && state.block !is CropBlock) break
                above++
            }

            return false
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun tick(
        state: BlockState,
        world: ServerLevel,
        pos: BlockPos,
        random: RandomSource,
    ) {
        dry(world, pos)
    }

    private fun dry(
        world: Level,
        pos: BlockPos,
    ) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())
        world.neighborChanged(pos, Blocks.AIR, pos)
    }
}
