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

fun <T : SprinkleAction> Sprinkler<T>.start(context: SprinkeContext) {
    val action = config.type() as SprinklerActionType<T>
    action.start(context, config)
}

fun <T : SprinkleAction> Sprinkler<T>.stop(context: SprinkeContext) {
    val action = config.type() as SprinklerActionType<T>
    action.stop(context, config)
}

fun <T : SprinkleAction> Sprinkler<T>.tick(context: SprinkeContext) {
    val action = config.type() as SprinklerActionType<T>
    action.tick(context, config)
}

fun <T : SprinkleAction> Sprinkler<T>.consume(context: SprinkeContext) {
    val action = config.type() as SprinklerActionType<T>
    action.consume(context, config)
}

fun Collection<Holder<Sprinkler<*>>>.actEach(
    origin: BlockPos,
    level: Level,
    fluid: FluidStack,
    type: SprinklerBlock.Type,
    action: Sprinkler<*>.(SprinkeContext) -> Unit,
) {
    if (level !is ServerLevel) return

    val radius = Configs.SERVER.sprinklerRange.get()

    map { it.value() }.forEach {
        val size = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
        val context = SprinkeContext(origin, size, level, fluid, type)
        it.action(context)
    }
}
