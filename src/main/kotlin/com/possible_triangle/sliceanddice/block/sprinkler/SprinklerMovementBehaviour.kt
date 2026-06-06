package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.api.sprinkler.actEach
import com.possible_triangle.sliceanddice.api.sprinkler.consume
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlockEntity.Companion.ACTIVE_ROTATION_SPEED
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.api.behaviour.movement.MovementBehaviour
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.neoforged.neoforge.fluids.capability.IFluidHandler

object SprinklerMovementBehaviour : MovementBehaviour {
    override fun visitNewPosition(
        context: MovementContext,
        pos: BlockPos,
    ) {
        val tank = context.contraption.getStorage().fluids

        val used = Configs.SERVER.sprinklerUsage.get()
        val fluid = tank.drain(used, IFluidHandler.FluidAction.SIMULATE)

        val active = fluid.amount >= used
        context.data.putBoolean("Active", active)

        if (active) {
            val drained = tank.drain(used, IFluidHandler.FluidAction.EXECUTE)
            // processingTicks = PROGRESS_DURATION
            val matches = Sprinkler.findMatching(context.world.registryAccess(), drained)
            val type = context.state.getValue(SprinklerBlock.TYPE)

            if (context.world is ServerLevel) {
                matches.actEach(pos, context.world, drained, type) {
                    consume(it)
                }
            }
        }
    }

    override fun tick(context: MovementContext) {
        if (!context.data.getBoolean("Active")) return
        val tank = context.contraption.getStorage().fluids
        val type = context.state.getValue(SprinklerBlock.TYPE)

        spawnSprinklerParticles(tank.getFluidInTank(0), context.world, context.position, type, ACTIVE_ROTATION_SPEED)
    }
}
