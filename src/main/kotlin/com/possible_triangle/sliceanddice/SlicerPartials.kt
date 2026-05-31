package com.possible_triangle.sliceanddice

import dev.engine_room.flywheel.lib.model.baked.PartialModel

object SlicerPartials {
    fun load() {
        // Only here so the class gets loaded
    }

    val SLICER_HEAD = PartialModel.of(SliceAndDice.modLoc("block/slicer/head"))
}
