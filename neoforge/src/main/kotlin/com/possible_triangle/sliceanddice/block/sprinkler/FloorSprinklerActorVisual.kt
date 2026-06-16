package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.block.sprinkler.behaviour.SprinklerBehaviour
import com.possible_triangle.sliceanddice.index.SDPartials
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import com.simibubi.create.content.contraptions.render.ActorVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.model.Models
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.math.AngleHelper
import net.minecraft.world.level.BlockAndTintGetter

class FloorSprinklerActorVisual(
    visualizationContext: VisualizationContext,
    level: BlockAndTintGetter,
    context: MovementContext,
    private val behaviour: SprinklerBehaviour,
) : ActorVisual(visualizationContext, level, context) {
    private val sprinklerHead =
        instancerProvider
            .instancer(InstanceTypes.TRANSFORMED, Models.partial(SDPartials.FLOOR_SPRINKLER_HEAD))
            .createInstance()

    private var rotation = 0.0
    private var previousRotation = 0.0

    init {
        animate()
    }

    override fun tick() {
        previousRotation = rotation
        val deg = behaviour.rotationSpeed
        rotation += (deg / 20).toDouble()
        rotation %= 360.0
    }

    private fun animate() {
        val rotation =
            AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks().toDouble(), previousRotation, rotation)

        sprinklerHead
            .setIdentityTransform()
            .translate(context.localPos)
            .center()
            .rotateYDegrees(rotation)
            .uncenter()
            .light(localBlockLight(), 0)
            .setChanged()
    }

    override fun beginFrame() {
        animate()
    }

    override fun _delete() {
        sprinklerHead.delete()
    }
}
