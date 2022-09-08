package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraftforge.fluids.FluidStack
import java.util.*

object FertilizerBehaviour : SprinkleBehaviour {

    override fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random) {
        val state = world.getBlockState(pos)
        val block = state.block

        if (block !is BonemealableBlock) return
        if (!block.isValidBonemealTarget(world, pos, state, false)) return
        if (world.gameTime % 20 != 0L || random.nextInt(30) < 26) return;
        if (!block.isBonemealSuccess(world, random, pos, state)) return

        block.performBonemeal(world, random, pos, state)
    }
}