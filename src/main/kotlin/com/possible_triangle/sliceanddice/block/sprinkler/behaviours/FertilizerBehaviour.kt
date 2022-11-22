package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraftforge.fluids.FluidStack

object FertilizerBehaviour : SprinkleBehaviour {

    override fun act(range: SprinkleBehaviour.Range, world: ServerLevel, fluidStack: FluidStack, random: RandomSource) {
        range.forEachBlock { pos ->
            val state = world.getBlockState(pos)
            if (state.`is`(Content.FERTILIZER_BLACKLIST)) return@forEachBlock
            val block = state.block

            if (block !is BonemealableBlock) return@forEachBlock
            if (!block.isValidBonemealTarget(world, pos, state, false)) return@forEachBlock
            if (world.gameTime % 20 != 0L || random.nextInt(30) < 26) return@forEachBlock
            if (!block.isBonemealSuccess(world, random, pos, state)) return@forEachBlock

            block.performBonemeal(world, random, pos, state)
        }
    }
}