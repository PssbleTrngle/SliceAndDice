package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.Codec

interface SprinkleAction {
    companion object {
        @JvmField
        val CODEC: Codec<SprinkleAction> =
            SprinkleActionType.CODEC.dispatch(
                SprinkleAction::type,
                { it.codec() },
            )
    }

    fun type(): SprinkleActionType<*>
}
