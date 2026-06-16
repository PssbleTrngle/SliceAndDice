package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import dev.engine_room.flywheel.api.visual.BlockEntityVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer

object SprinklerVisualFactory : SimpleBlockEntityVisualizer.Factory<SprinklerBlockEntity> {
    override fun create(
        ctx: VisualizationContext,
        blockEntity: SprinklerBlockEntity,
        partialTick: Float,
    ): BlockEntityVisual<SprinklerBlockEntity> =
        when (blockEntity.type) {
            SprinklerType.CEILING -> CeilingSprinklerVisual(ctx, blockEntity, partialTick)
            SprinklerType.FLOOR -> FloorSprinklerVisual(ctx, blockEntity, partialTick)
        }
}
