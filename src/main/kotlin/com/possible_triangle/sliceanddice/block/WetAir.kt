package com.possible_triangle.sliceanddice.block

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.AirBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class WetAir(properties: Properties) : AirBlock(properties) {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: Random) {
        dry(world, pos)
    }

    private fun dry(world: Level, pos: BlockPos) {
        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())
        world.neighborChanged(pos, Blocks.AIR, pos)
    }

}