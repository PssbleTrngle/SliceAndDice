package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.atmosphere.api.v1.AbstractWeatherProvider
import com.possible_triangle.atmosphere.api.v1.ProviderHeartbeat
import com.possible_triangle.atmosphere.api.v1.WeatherAPI
import com.possible_triangle.atmosphere.api.v1.WeatherCondition
import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack

object MoistAction : SprinkleAction {
    private fun SprinkleAction.Range.createId() =
        with(origin) {
            SliceAndDice.modLoc("sprinkler_${x}_${y}_$z")
        }

    override fun start(
        range: SprinkleAction.Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
        val weather = WeatherAPI.INSTANCE.getWeather(world)
        weather.addLocal(range.createId(), SprinkleProvider(), range.aabb, SprinklerHeartbeat(range.origin))
    }

    override fun stop(
        range: SprinkleAction.Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
        val weather = WeatherAPI.INSTANCE.getWeather(world)
        weather.removeLocal(range.createId())
    }

    private class SprinkleProvider : AbstractWeatherProvider() {
        override fun conditionKeyAt(
            level: Level,
            pos: BlockPos,
        ) = WeatherCondition.RAIN
    }

    private class SprinklerHeartbeat(
        val pos: BlockPos,
    ) : ProviderHeartbeat {
        override fun validate(level: ServerLevel): Boolean {
            val be = level.getBlockEntity(pos)
            return be is SprinklerBlockEntity
        }
    }
}
