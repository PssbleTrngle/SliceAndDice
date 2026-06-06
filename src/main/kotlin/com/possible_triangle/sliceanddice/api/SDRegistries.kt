package com.possible_triangle.sliceanddice.api

import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerActionType
import com.possible_triangle.sliceanddice.modLoc
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey.createRegistryKey

object SDRegistries {
    @JvmField
    val SPRINKLERS = createRegistryKey<Sprinkler<*>>(modLoc("sprinkler"))

    @JvmField
    val SPRINKLER_ACTIONS = createRegistryKey<SprinklerActionType<*>>(modLoc("sprinkler_action"))

    @Suppress("ktlint:standard:property-naming")
    internal lateinit var SPRINKLER_ACTIONS_REGISTRY: Registry<SprinklerActionType<*>>
}
