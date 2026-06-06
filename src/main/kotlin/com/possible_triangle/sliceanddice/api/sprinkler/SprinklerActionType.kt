package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.possible_triangle.sliceanddice.api.SDRegistries

interface SprinklerActionType<TConfig : SprinkleAction> {
    companion object {
        @JvmField
        val CODEC: Codec<SprinklerActionType<*>> =
            Codec.lazyInitialized {
                SDRegistries.SPRINKLER_ACTIONS_REGISTRY.byNameCodec()
            }
    }

    fun codec(): MapCodec<TConfig>

    fun consume(
        context: SprinkeContext,
        config: TConfig,
    ) {
    }

    fun tick(
        context: SprinkeContext,
        config: TConfig,
    ) {
    }

    fun start(
        context: SprinkeContext,
        config: TConfig,
    ) {
    }

    fun stop(
        context: SprinkeContext,
        config: TConfig,
    ) {
    }
}
