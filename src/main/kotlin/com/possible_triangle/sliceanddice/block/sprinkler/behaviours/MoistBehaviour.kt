package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.fluids.FluidStack
import java.util.*

object MoistBehaviour : SprinkleBehaviour {

    override fun act(range: SprinkleBehaviour.Range, world: ServerLevel, fluidStack: FluidStack, random: Random) {
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