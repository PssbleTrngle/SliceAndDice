package com.possible_triangle.sliceanddice.platform.services

interface IConfig {

    val CONSUME_DURABILTY: Boolean
    val IGNORE_ROTATION: Boolean

    val BASIN_COOKING: Boolean
    val REPLACE_FLUID_CONTAINERS: Boolean

    val SPRINKLER_CAPACITY: Int
    val SPRINKLER_USAGE: Int

}