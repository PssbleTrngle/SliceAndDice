package com.possible_triangle.sliceanddice.block.sprinkler

import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import java.util.function.Consumer

class CeilingSprinklerVisual(
    ctx: VisualizationContext,
    blockEntity: SprinklerBlockEntity,
    partialTick: Float,
) : AbstractBlockEntityVisual<SprinklerBlockEntity>(ctx, blockEntity, partialTick) {
    override fun _delete() {
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {
    }

    override fun updateLight(partialTick: Float) {
    }
}
