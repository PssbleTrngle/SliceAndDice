package com.possible_triangle.sliceanddice.config

import net.neoforged.neoforge.common.ModConfigSpec

@Suppress("ktlint:standard:property-naming")
object Configs {
    @JvmStatic
    var SERVER: ServerConfig
        private set
    var SERVER_SPEC: ModConfigSpec
        private set

    @JvmStatic
    var CLIENT: ClientConfig
        private set
    var CLIENT_SPEC: ModConfigSpec
        private set

    init {
        with(ModConfigSpec.Builder().configure { ServerConfig(it) }) {
            SERVER = left
            SERVER_SPEC = right
        }

        with(ModConfigSpec.Builder().configure { ClientConfig(it) }) {
            CLIENT = left
            CLIENT_SPEC = right
        }
    }
}
