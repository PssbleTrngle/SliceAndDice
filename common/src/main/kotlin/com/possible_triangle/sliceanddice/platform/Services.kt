package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.Constants
import com.possible_triangle.sliceanddice.platform.services.IConfig
import com.possible_triangle.sliceanddice.platform.services.IPlatformHelper
import com.possible_triangle.sliceanddice.platform.services.IRegistries
import java.util.*

object Services {

    val PLATFORM = load(IPlatformHelper::class.java)
    val CONFIG = load(IConfig::class.java)
    val REGISTRIES = load(IRegistries::class.java)

    private fun <T> load(clazz: Class<T>): T {
        val loadedService: T = ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow { NullPointerException("Failed to load service for ${clazz.name}") }
        Constants.LOGGER.debug("Loaded $loadedService for service $clazz")
        return loadedService
    }

}