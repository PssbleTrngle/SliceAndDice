package com.possible_triangle.thermomix.config

import net.minecraftforge.common.ForgeConfigSpec

object Configs {

    var SERVER_SPEC: ForgeConfigSpec
        private set
    var SERVER: ServerConfig
        private set

    var CLIENT_SPEC: ForgeConfigSpec
        private set
    var CLIENT: ClientConfig
        private set

    init {
        with(ForgeConfigSpec.Builder().configure { ServerConfig(it) }) {
            SERVER = left
            SERVER_SPEC = right
        }

        with(ForgeConfigSpec.Builder().configure { ClientConfig(it) }) {
            CLIENT = left
            CLIENT_SPEC = right
        }
    }

}