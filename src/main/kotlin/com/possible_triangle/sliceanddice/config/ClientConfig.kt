package com.possible_triangle.sliceanddice.config

import net.minecraftforge.common.ForgeConfigSpec
import java.util.*

class ClientConfig(builder: ForgeConfigSpec.Builder) {

    companion object {
        private val calendar = Calendar.getInstance()
        private val isHalloween = (calendar.get(2) + 1 == 10) && (calendar.get(5) == 31)
    }

    private val SEASONAL_EFFECTS = builder.define("effects.seasonal", true)

    val spawnBloodParticles
        get() = isHalloween && SEASONAL_EFFECTS.get()

}