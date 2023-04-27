package com.possible_triangle.sliceanddice.platform.services

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import kotlin.properties.ReadOnlyProperty

interface IPlatformHelper {

    val isDevelopmentEnvironment: Boolean

}