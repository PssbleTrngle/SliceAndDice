package com.possible_triangle.thermomix.config

import net.minecraftforge.common.ForgeConfigSpec

class ServerConfig(builder: ForgeConfigSpec.Builder) {

    val CONSUME_DURABILTY = builder.define("thermomix.consume_tool_durability", true)

}