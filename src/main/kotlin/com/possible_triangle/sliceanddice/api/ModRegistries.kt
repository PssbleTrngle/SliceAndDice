package com.possible_triangle.sliceanddice.api

import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey.createRegistryKey

object ModRegistries {
    @JvmField
    val SPRINKLERS = createRegistryKey<Sprinkler>(SliceAndDice.modLoc("sprinkler"))

    @JvmField
    val SPRINKLER_ACTIONS = createRegistryKey<SprinkleAction>(SliceAndDice.modLoc("sprinkler_action"))

    @Suppress("ktlint:standard:property-naming")
    internal lateinit var SPRINKLER_ACTIONS_REGISTRY: Registry<SprinkleAction>
}
