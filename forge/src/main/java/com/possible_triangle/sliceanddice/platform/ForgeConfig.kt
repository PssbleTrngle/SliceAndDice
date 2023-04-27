package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.platform.services.IConfig
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

class ForgeConfig : IConfig {

    companion object {
        fun init() {
            LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
            LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)
        }
    }

}