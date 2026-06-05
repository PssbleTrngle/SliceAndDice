package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.atmosphere.api.v1.ConstantWeatherProvider
import com.possible_triangle.atmosphere.api.v1.ProviderHeartbeat
import com.possible_triangle.atmosphere.api.v1.WeatherAPI
import com.possible_triangle.atmosphere.api.v1.WeatherCondition
import com.possible_triangle.atmosphere.api.v1.area.Box
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkeContext
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

object MoistAction : SprinkleAction {
    override fun start(context: SprinkeContext) {
        val weather = WeatherAPI.INSTANCE.getWeather(context.level)
        weather.addLocal(
            context.id,
            ConstantWeatherProvider(WeatherCondition.RAIN),
            Box.from(context.area),
            SprinklerHeartbeat(context.origin),
        )
    }

    override fun stop(context: SprinkeContext) {
        val weather = WeatherAPI.INSTANCE.getWeather(context.level)
        weather.removeLocal(context.id)
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
