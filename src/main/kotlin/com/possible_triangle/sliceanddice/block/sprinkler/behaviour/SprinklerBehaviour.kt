package com.possible_triangle.sliceanddice.block.sprinkler.behaviour

import com.possible_triangle.sliceanddice.api.sprinkler.SprinkeContext
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.contraptions.Contraption
import net.minecraft.core.Holder
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

interface SprinklerBehaviour {

    val type: SprinklerBlock.Type
    val contraption: Contraption?
    var running: Collection<Holder<Sprinkler<*>>>

    var remainingTicks: Int
    val cooldown: Int
    val active get() = remainingTicks > 0

    val pos: Position

    fun notifyUpdate() {}

    private fun Collection<Holder<Sprinkler<*>>>.actEach(
        level: ServerLevel,
        fluid: FluidStack,
        consumer: Sprinkler<*>.(context: SprinkeContext) -> Unit,
    ) {
        val radius = Configs.SERVER.sprinklerRange.get()

        map { it.value() }.forEach {
            val size = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
            val context = SprinkeContext(pos, size, level, fluid, type, contraption)

            it.consumer(context)
        }
    }

    fun check(
        tank: IFluidHandler,
        level: Level,
    ) {
        val used = Configs.SERVER.sprinklerUsage.get()
        val fluid = tank.drain(used, IFluidHandler.FluidAction.SIMULATE)
        val active = fluid.amount >= used

        if (active) {
            val drained = tank.drain(used, IFluidHandler.FluidAction.EXECUTE)
            val matches = Sprinkler.findMatching(level.registryAccess(), fluid)
            val stopped = running.filterNot { matches.contains(it) }
            val started = matches.filterNot { running.contains(it) }

            if (level is ServerLevel) {
                stopped.actEach(level, FluidStack.EMPTY, Sprinkler<*>::stop)
                started.actEach(level, fluid, Sprinkler<*>::start)

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

    fun tickSprinklers(
        level: ServerLevel,
        fluid: FluidStack,
    ) {
        running.actEach(level, fluid, Sprinkler<*>::tick)
    }

    fun invalidate(level: Level) {
        if (level is ServerLevel) {
            running.actEach(level, FluidStack.EMPTY, Sprinkler<*>::stop)
        }
    }
}
