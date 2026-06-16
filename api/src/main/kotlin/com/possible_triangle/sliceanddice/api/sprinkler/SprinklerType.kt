package com.possible_triangle.sliceanddice.api.sprinkler

import net.minecraft.core.Direction
import net.minecraft.util.StringRepresentable

enum class SprinklerType(
    val input: Direction,
) : StringRepresentable {
    CEILING(Direction.UP),
    FLOOR(Direction.DOWN), ;

    override fun getSerializedName() = name.lowercase()
}
