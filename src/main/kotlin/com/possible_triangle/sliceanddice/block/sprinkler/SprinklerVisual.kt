package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.math.Axis
import com.possible_triangle.sliceanddice.index.SDPartials
import com.simibubi.create.foundation.render.AllInstanceTypes
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.model.Models
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import net.minecraft.core.Direction
import java.util.function.Consumer

class SprinklerVisual(
    context: VisualizationContext,
    blockEntity: SprinklerBlockEntity,
    partialTick: Float,
) : AbstractBlockEntityVisual<SprinklerBlockEntity>(context, blockEntity, partialTick), SimpleDynamicVisual {
    private val sprinklerHead =
        instancerProvider()
            .instancer(AllInstanceTypes.ROTATING, Models.partial(SDPartials.FLOOR_SPRINKLER_HEAD))
            .createInstance()

    init {
        sprinklerHead.setRotationAxis(Direction.Axis.Y)
        animate(partialTick)
    }

    override fun beginFrame(ctx: DynamicVisual.Context) {
        animate(ctx.partialTick())
    }

    private fun animate(partialTick: Float) {
        if (blockEntity.active) {
            sprinklerHead.setRotationalSpeed(-176F)
        } else {
            sprinklerHead
                .setRotationalSpeed(0F)
                .setRotationOffset(0F)
        }

        sprinklerHead
            .setPosition(visualPosition)
            .setChanged()
    }

    override fun _delete() {
        sprinklerHead.delete()
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {
        consumer.accept(sprinklerHead)
    }

    override fun updateLight(partialTick: Float) {
        relight(sprinklerHead)
    }

}
