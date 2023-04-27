package com.possible_triangle.sliceanddice.config

import net.minecraftforge.common.ForgeConfigSpec

object Configs {

    var SERVER_SPEC: ForgeConfigSpec
        private set
    var SERVER: ServerConfig
        private set

    init {
        with(ForgeConfigSpec.Builder().configure { ServerConfig(it) }) {
            SERVER = left
            SERVER_SPEC = right
        }
    }

}