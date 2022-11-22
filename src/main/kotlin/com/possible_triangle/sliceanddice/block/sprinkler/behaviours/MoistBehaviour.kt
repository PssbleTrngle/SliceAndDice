package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraftforge.fluids.FluidStack

object MoistBehaviour : SprinkleBehaviour {

    override fun act(range: SprinkleBehaviour.Range, world: ServerLevel, fluidStack: FluidStack, random: RandomSource) {
        val wetAir = Content.WET_AIR.defaultState
        range.forEachBlock { pos ->
            val state = world.getBlockState(pos)
            if (state.isAir) {
                world.setBlockAndUpdate(pos, wetAir)
                world.scheduleTick(pos, Content.WET_AIR.get(), random.nextInt(60, 120))
            }
        }
    }

}