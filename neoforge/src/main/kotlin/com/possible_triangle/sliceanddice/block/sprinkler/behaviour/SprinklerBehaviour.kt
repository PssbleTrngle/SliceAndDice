package com.possible_triangle.sliceanddice.block.sprinkler.behaviour

import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleContextImpl
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.contraptions.Contraption
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

internal fun BlockPos.encode() = "${x}_${y}_$z"

interface SprinklerBehaviour {
    val type: SprinklerType
    val contraption: Contraption?
    var running: Collection<Holder<Sprinkler<*>>>

    var remainingTicks: Int
    val cooldown: Int
    val active get() = remainingTicks > 0

    val pos: Position

    val tank: IFluidHandler
    val renderedFluid: FluidStack

    val rotationSpeed get() = if (active) 300F else 0F

    val id: String

    fun notifyUpdate() {}

    private fun Collection<Holder<Sprinkler<*>>>.actEach(
        level: ServerLevel,
        fluid: FluidStack,
        consumer: Sprinkler<*>.(context: SprinkleContext) -> Unit,
    ) {
        val radius = Configs.SERVER.sprinklerRange.get()

        map { it.value() }.forEach {
            val size = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
            val context = SprinkleContextImpl(pos, size, level, fluid, type, contraption?.entity?.uuid, id)

            it.consumer(context)
        }
    }

    fun check(level: Level) {
        val used = Configs.SERVER.sprinklerUsage.get()
        val simulated = tank.drain(used, IFluidHandler.FluidAction.SIMULATE)
        val active = simulated.amount >= used

        if (active) {
            val matches = Sprinkler.findMatching(level.registryAccess(), simulated)
            val stopped = running.filterNot { matches.contains(it) }
            val started = matches.filterNot { running.contains(it) }

            if (level is ServerLevel) {
                val drained = tank.drain(used, IFluidHandler.FluidAction.EXECUTE)
                stopped.actEach(level, FluidStack.EMPTY, Sprinkler<*>::stop)
                started.actEach(level, drained, Sprinkler<*>::start)

                matches.actEach(level, drained, Sprinkler<*>::consume)
            }

            running = matches

            if (stopped.isNotEmpty() || started.isNotEmpty()) {
                notifyUpdate()
            }

            remainingTicks = cooldown
        } else {
            remainingTicks = 0
        }
    }

    fun tickSprinklers(level: ServerLevel) {
        running.actEach(level, renderedFluid, Sprinkler<*>::tick)
    }

    fun invalidate(level: Level) {
        if (level is ServerLevel) {
            running.actEach(level, FluidStack.EMPTY, Sprinkler<*>::stop)
            running = emptyList()
        }
    }
}
