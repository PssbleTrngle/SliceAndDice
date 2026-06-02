package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.modLoc
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

object SDTags {
    val ALLOWED_TOOLS = TagKey.create(Registries.ITEM, modLoc("allowed_tools"))

    val FERTILIZER_BLACKLIST = TagKey.create(Registries.BLOCK, modLoc("fertilizer_blacklist"))

    val WET_FLUIDS = TagKey.create(Registries.FLUID, modLoc("moisturizing"))
    val HOT_FLUIDS = TagKey.create(Registries.FLUID, modLoc("burning"))
    val FERTILIZER_FLUIDS = TagKey.create(Registries.FLUID, modLoc("fertilizer"))
}
