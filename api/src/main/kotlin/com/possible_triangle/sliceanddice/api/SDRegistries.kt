package com.possible_triangle.sliceanddice.api

import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleActionType
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.modLoc
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey.createRegistryKey
import org.jetbrains.annotations.ApiStatus

object SDRegistries {
    @JvmField
    val SPRINKLERS = createRegistryKey<Sprinkler<*>>(modLoc("sprinkler"))

    @JvmField
    val SPRINKLER_ACTIONS = createRegistryKey<SprinkleActionType<*>>(modLoc("sprinkle_action"))

    @Suppress("ktlint:standard:property-naming")
    @ApiStatus.Internal
    lateinit var SPRINKLER_ACTIONS_REGISTRY: Registry<SprinkleActionType<*>>
}
