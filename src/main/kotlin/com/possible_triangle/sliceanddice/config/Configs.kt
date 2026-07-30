package com.possible_triangle.sliceanddice.config

import net.minecraftforge.common.ForgeConfigSpec

object Configs {
    @Suppress("ktlint:standard:property-naming")
    var SERVER_SPEC: ForgeConfigSpec
        private set

    @Suppress("ktlint:standard:property-naming")
    var SERVER: ServerConfig
        private set

    @Suppress("ktlint:standard:property-naming")
    var CLIENT_SPEC: ForgeConfigSpec
        private set

    @Suppress("ktlint:standard:property-naming")
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
