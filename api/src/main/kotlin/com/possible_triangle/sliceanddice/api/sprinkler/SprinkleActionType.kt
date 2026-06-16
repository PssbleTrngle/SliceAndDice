package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.possible_triangle.sliceanddice.api.SDRegistries

interface SprinkleActionType<TConfig : SprinkleAction> {
    companion object {
        @JvmField
        val CODEC: Codec<SprinkleActionType<*>> =
            Codec.lazyInitialized {
                SDRegistries.SPRINKLER_ACTIONS_REGISTRY.byNameCodec()
            }
    }

    fun codec(): MapCodec<TConfig>

    fun consume(
        context: SprinkleContext,
        config: TConfig,
    ) {
    }

    fun tick(
        context: SprinkleContext,
        config: TConfig,
    ) {
    }

    fun start(
        context: SprinkleContext,
        config: TConfig,
    ) {
    }

    fun stop(
        context: SprinkleContext,
        config: TConfig,
    ) {
    }
}
