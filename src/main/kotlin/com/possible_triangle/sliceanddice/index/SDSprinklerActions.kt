package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.block.sprinkler.actions.DamageAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.ExperienceAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.FertilizerAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.PotionAction
import com.possible_triangle.sliceanddice.block.sprinkler.actions.WeatherAction

object SDSprinklerActions {
    @JvmField
    val MOIST =
        REGISTRATE
            .generic("weather", SDRegistries.SPRINKLER_ACTIONS) { WeatherAction.Type }
            .register()

    @JvmField
    val DAMAGE =
        REGISTRATE
            .generic("damage", SDRegistries.SPRINKLER_ACTIONS) { DamageAction.Type }
            .register()

    @JvmField
    val FERTILIZER =
        REGISTRATE
            .generic("fertilizer", SDRegistries.SPRINKLER_ACTIONS) { FertilizerAction.Type }
            .register()

    @JvmField
    val POTION =
        REGISTRATE
            .generic("potion", SDRegistries.SPRINKLER_ACTIONS) { PotionAction.Type }
            .register()

    @JvmField
    val EXPERIENCE =
        REGISTRATE
            .generic("experience", SDRegistries.SPRINKLER_ACTIONS) { ExperienceAction.Type }
            .register()
}
