package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.platform.services.IConfig
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

class ForgeConfig : IConfig {

    companion object {
        fun init() {
            LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        }
    }

    override val CONSUME_DURABILTY get() = Services.CONFIG.CONSUME_DURABILTY
    
    override val IGNORE_ROTATION get() = Services.CONFIG.IGNORE_ROTATION
    
    override val BASIN_COOKING get() = Services.CONFIG.BASIN_COOKING
    
    override val REPLACE_FLUID_CONTAINERS = false
    
    override val SPRINKLER_CAPACITY get() = Services.CONFIG.SPRINKLER_CAPACITY
    
    override val SPRINKLER_USAGE get() = Services.CONFIG.SPRINKLER_USAGE
    
}