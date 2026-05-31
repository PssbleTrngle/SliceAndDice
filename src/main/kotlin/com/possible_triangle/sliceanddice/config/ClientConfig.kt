package com.possible_triangle.sliceanddice.config

import net.neoforged.neoforge.common.ModConfigSpec
import java.util.*

class ClientConfig(
    builder: ModConfigSpec.Builder,
) {
    companion object {
        private val calendar = Calendar.getInstance()
        private val isHalloween = (calendar.get(2) + 1 == 10) && (calendar.get(5) == 31)
    }

    private val seasonalEffects = builder.define("effects.seasonal", true)

    val spawnBloodParticles
        get() = isHalloween && seasonalEffects.get()
}
