@file:JvmName("SprinklerHelper")

package com.possible_triangle.sliceanddice.api.sprinkler

import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.config.Configs
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack

fun Collection<Holder<Sprinkler>>.start(
    origin: BlockPos,
    level: Level,
    fluid: FluidStack,
    type: SprinklerBlock.Type,
) {
    actEach(origin, level, fluid, type) {
        action.value().start(it)
    }
}

fun Collection<Holder<Sprinkler>>.stop(
    origin: BlockPos,
    level: Level,
    fluid: FluidStack,
    type: SprinklerBlock.Type,
) {
    actEach(origin, level, fluid, type) {
        action.value().stop(it)
    }
}

fun Collection<Holder<Sprinkler>>.tick(
    origin: BlockPos,
    level: Level,
    fluid: FluidStack,
    type: SprinklerBlock.Type,
    applyTickRate: Boolean = true,
) {
    actEach(origin, level, fluid, type) {
        if (!applyTickRate || tickRate == 0 || level.gameTime % tickRate == 0L) {
            action.value().tick(it)
        }
    }
}

private fun Collection<Holder<Sprinkler>>.actEach(
    origin: BlockPos,
    level: Level,
    fluid: FluidStack,
    type: SprinklerBlock.Type,
    action: Sprinkler.(SprinkeContext) -> Unit,
) {
    if (level !is ServerLevel) return

    val radius = Configs.SERVER.sprinklerRange.get()

    map { it.value() }.forEach {
        val size = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
        val context = SprinkeContext(origin, size, level, fluid, type)
        it.action(context)
    }
}
