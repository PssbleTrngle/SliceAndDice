package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.BurningAction
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.FertilizerAction
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.MoistAction
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.PotionAction

object SDSprinklerActions {
    val MOIST_ACTION =
        REGISTRATE
            .generic("moist", SDRegistries.SPRINKLER_ACTIONS) { MoistAction }
            .register()

    val BURNING_ACTION =
        REGISTRATE
            .generic("burning", SDRegistries.SPRINKLER_ACTIONS) { BurningAction }
            .register()

    val FERTILIZER_ACTION =
        REGISTRATE
            .generic("fertilizer", SDRegistries.SPRINKLER_ACTIONS) { FertilizerAction }
            .register()

    val POTION_ACTION =
        REGISTRATE
            .generic("potion", SDRegistries.SPRINKLER_ACTIONS) { PotionAction }
            .register()
}
