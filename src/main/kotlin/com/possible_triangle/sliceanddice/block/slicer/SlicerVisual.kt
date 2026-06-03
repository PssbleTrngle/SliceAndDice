package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.index.SDPartials
import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
import com.simibubi.create.foundation.render.AllInstanceTypes
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.model.Models
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import java.util.function.Consumer

class SlicerVisual(
    context: VisualizationContext,
    blockEntity: SlicerBlockEntity,
    partialTick: Float,
) : SingleAxisRotatingVisual<SlicerBlockEntity>(
        context,
        blockEntity,
        partialTick,
        Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL),
    ),
    SimpleDynamicVisual {
    private val mixerPole =
        instancerProvider()
            .instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_MIXER_POLE))
            .createInstance()

    private val mixerHead =
        instancerProvider()
            .instancer(AllInstanceTypes.ROTATING, Models.partial(SDPartials.SLICER_HEAD))
            .createInstance()

    init {
        animate(partialTick)
    }

    override fun beginFrame(ctx: DynamicVisual.Context) {
        animate(ctx.partialTick())
    }

    private fun animate(partialTick: Float) {
        val renderedHeadOffset = blockEntity.getRenderedHeadOffset(partialTick)
        transformPole(renderedHeadOffset)
        transformHead(renderedHeadOffset)
    }

    private fun transformHead(renderedHeadOffset: Float) {
        val speed = blockEntity.getRenderedHeadRotationSpeed()
        mixerHead
            .setPosition(visualPosition)
            .nudge(0.0f, -renderedHeadOffset, 0.0f)
            .setRotationalSpeed(speed * 2.0f * 6.0f)
            .setChanged()
    }

    private fun transformPole(renderedHeadOffset: Float) {
        mixerPole
            .position(visualPosition)
            .translatePosition(0.0f, -renderedHeadOffset, 0.0f)
            .setChanged()
    }

    override fun updateLight(partialTick: Float) {
        super.updateLight(partialTick)
        relight(pos.below(), mixerHead)
        relight(mixerPole)
    }

    override fun _delete() {
        super._delete()
        mixerHead.delete()
        mixerPole.delete()
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {
        super.collectCrumblingInstances(consumer)
        consumer.accept(mixerHead)
        consumer.accept(mixerPole)
    }
}
