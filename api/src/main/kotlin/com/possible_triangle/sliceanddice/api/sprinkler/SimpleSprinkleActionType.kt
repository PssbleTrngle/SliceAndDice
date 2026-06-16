package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.MapCodec

abstract class SimpleSprinkleActionType<T : SprinkleAction>(
    instance: T,
) : SprinkleActionType<T> {
    private val configCodec: MapCodec<T> = MapCodec.unit { instance }

    override fun codec() = configCodec
}
