package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.fluids.FluidStack
import java.util.*

object MoistBehaviour : SprinkleBehaviour {

    override fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random) {
        val wetAir = Content.WET_AIR.defaultState
        val state = world.getBlockState(pos)
        if (state.isAir) {
            world.setBlockAndUpdate(pos, wetAir)
            world.scheduleTick(pos, Content.WET_AIR.get(), random.nextInt(60, 120))
        }
        // Not needed because wet air blocks already simulate rain
        //} else if (state.hasProperty(MOISTURE) && state.getValue(MOISTURE) < 7) {
        //    world.setBlockAndUpdate(it, state.setValue(MOISTURE, 7))
        //}
    }
}