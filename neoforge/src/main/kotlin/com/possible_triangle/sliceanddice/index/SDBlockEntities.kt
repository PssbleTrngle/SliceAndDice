package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlockEntity
import com.possible_triangle.sliceanddice.block.slicer.SlicerRenderer
import com.possible_triangle.sliceanddice.block.slicer.SlicerVisual
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlockEntity
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerRenderer
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerVisualFactory
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory
import com.tterrag.registrate.util.nullness.NonNullFunction
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer

object SDBlockEntities {
    @JvmField
    val SLICER =
        REGISTRATE
            .blockEntity("slicer", BlockEntityFactory(::SlicerBlockEntity))
            .visual { SimpleBlockEntityVisualizer.Factory(::SlicerVisual) }
            .renderer { NonNullFunction { SlicerRenderer(it) } }
            .validBlock(SDBlocks.SLICER)
            .register()

    @JvmField
    val SPRINKLER =
        REGISTRATE
            .blockEntity("sprinkler", BlockEntityFactory(::SprinklerBlockEntity))
            .visual { SprinklerVisualFactory }
            .renderer { NonNullFunction { SprinklerRenderer(it) } }
            .validBlock(SDBlocks.SPRINKLER)
            .register()
}
