package com.possible_triangle.sliceanddice.config

import net.minecraftforge.common.ForgeConfigSpec
import java.util.function.Supplier

class ServerConfig(builder: ForgeConfigSpec.Builder) {

    val CONSUME_DURABILTY = builder.define("slicer.consume_tool_durability", true)
    val IGNORE_ROTATION = builder.define("slicer.ignore_rotation", false)

    val BASIN_COOKING = builder.define("basin_cooking.enabled", true)
    //val REPLACE_FLUID_CONTAINERS = builder.define("basin_cooking.replace_fluid_containers", true)
    val REPLACE_FLUID_CONTAINERS = Supplier { true }

    val SPRINKLER_CAPACITY = builder.defineInRange("sprinkler.fluid_capacity", 300, 10, 2048)
    val SPRINKLER_USAGE = builder.defineInRange("sprinkler.fluid_per_use", 100, 0, 2048)

}