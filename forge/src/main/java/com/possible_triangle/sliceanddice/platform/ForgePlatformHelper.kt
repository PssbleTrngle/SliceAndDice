package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.platform.services.IPlatformHelper
import net.minecraftforge.fml.loading.FMLLoader

class ForgePlatformHelper : IPlatformHelper {

    override val isDevelopmentEnvironment: Boolean
        get() = !FMLLoader.isProduction()

}