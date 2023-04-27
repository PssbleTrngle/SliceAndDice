package com.possible_triangle.sliceanddice.platform.services

interface IPlatformHelper {

    fun isLoaded(mod: String): Boolean

    val isDevelopmentEnvironment: Boolean

}