package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.MapCodec

abstract class SimpleSprinklerActionType<T : SprinkleAction>(
    instance: T,
) : SprinklerActionType<T> {
    private val configCodec: MapCodec<T> = MapCodec.unit { instance }

    override fun codec() = configCodec
}
