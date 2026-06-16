package com.possible_triangle.sliceanddice.block.sprinkler.actions

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.atmosphere.api.v1.AtmosphereRegistries
import com.possible_triangle.atmosphere.api.v1.ConstantWeatherProvider
import com.possible_triangle.atmosphere.api.v1.ProviderHeartbeat
import com.possible_triangle.atmosphere.api.v1.WeatherAPI
import com.possible_triangle.atmosphere.api.v1.WeatherCondition
import com.possible_triangle.atmosphere.api.v1.area.Box
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleActionType
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import com.possible_triangle.sliceanddice.index.SDBlockEntities
import net.minecraft.resources.ResourceKey

data class WeatherAction(
    val condition: ResourceKey<WeatherCondition>,
) : SprinkleAction {
    companion object {
        private val CONDITION_CODEC = ResourceKey.codec(AtmosphereRegistries.WEATHER_CONDITION)

        val CODEC: MapCodec<WeatherAction> =
            RecordCodecBuilder.mapCodec { builder ->
                builder
                    .group(
                        CONDITION_CODEC.fieldOf("condition").forGetter { it.condition },
                    ).apply(builder, ::WeatherAction)
            }
    }

    override fun type() = Type

    object Type : SprinkleActionType<WeatherAction> {
        override fun start(
            context: SprinkleContext,
            config: WeatherAction,
        ) {
            val weather = WeatherAPI.INSTANCE.getWeather(context.level)

            val heartbeat =
                context.contraption?.let {
                    ProviderHeartbeat.hasEntity(it)
                } ?: run {
                    ProviderHeartbeat.hasBlockEntity(SDBlockEntities.SPRINKLER, context.blockPos)
                }

            weather.addLocal(
                context.id,
                ConstantWeatherProvider(config.condition),
                Box.from(context.area),
                heartbeat,
            )
        }

        override fun stop(
            context: SprinkleContext,
            config: WeatherAction,
        ) {
            val weather = WeatherAPI.INSTANCE.getWeather(context.level)
            weather.removeLocal(context.id)
        }

        override fun codec() = CODEC
    }
}
