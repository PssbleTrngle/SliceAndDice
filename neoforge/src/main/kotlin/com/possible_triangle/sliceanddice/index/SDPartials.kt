package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.modLoc
import dev.engine_room.flywheel.lib.model.baked.PartialModel

object SDPartials {
    @JvmField
    val SLICER_HEAD = PartialModel.of(modLoc("block/slicer/head"))

    @JvmField
    val FLOOR_SPRINKLER_HEAD = PartialModel.of(modLoc("block/sprinkler/floor/head"))
}
