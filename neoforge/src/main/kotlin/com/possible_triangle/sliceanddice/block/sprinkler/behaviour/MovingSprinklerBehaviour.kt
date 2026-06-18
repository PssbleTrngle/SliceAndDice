package com.possible_triangle.sliceanddice.block.sprinkler.behaviour

import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.block.sprinkler.FloorSprinklerActorVisual
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerRenderer
import com.simibubi.create.api.behaviour.movement.MovementBehaviour
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import com.simibubi.create.content.contraptions.render.ActorVisual
import com.simibubi.create.content.contraptions.render.ContraptionMatrices
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.neoforged.neoforge.fluids.FluidStack

object MovingSprinklerBehaviour : MovementBehaviour {
    @Suppress("UNCHECKED_CAST")
    private var MovementContext.behaviour: Instance
        get() {
            val cached = (temporaryData as Instance?)
            if (cached != null) return cached
            val instance = Instance(this)
            temporaryData = instance
            return instance
        }
        set(value) {
            temporaryData = value
        }

    override fun visitNewPosition(
        context: MovementContext,
        pos: BlockPos,
    ) {
        val level = context.world

        context.behaviour.invalidate(level)

        context.behaviour.check(level)
    }

    override fun tick(context: MovementContext) {
        if (!context.behaviour.active) return
        val level = context.world

        context.behaviour.pos = context.position
        context.behaviour.remainingTicks--

        context.behaviour.spawnParticles(context.world)

        if (level is ServerLevel) {
            context.behaviour.tickSprinklers(level)
        }
    }

    override fun disableBlockEntityRendering() = true

    override fun renderInContraption(
        context: MovementContext,
        level: VirtualRenderWorld,
        matrices: ContraptionMatrices,
        buffer: MultiBufferSource,
    ) {
        SprinklerRenderer.renderInContraption(context.behaviour, context, level, matrices, buffer)
    }

    override fun createVisual(
        visualizationContext: VisualizationContext,
        level: VirtualRenderWorld,
        context: MovementContext,
    ): ActorVisual? =
        when (context.behaviour.type) {
            SprinklerType.FLOOR -> FloorSprinklerActorVisual(visualizationContext, level, context, context.behaviour)
            SprinklerType.CEILING -> null
        }

    private class Instance(
        context: MovementContext,
    ) : SprinklerBehaviour {
        override val type = context.state.getValue(SprinklerBlock.TYPE)
        override val contraption = context.contraption
        override var running: Collection<Holder<Sprinkler<*>>> = emptyList()
        override var pos = context.position
        override var remainingTicks = 0
        override val cooldown = 80
        override val tank
            get() = contraption!!.storage.fluids
        override val renderedFluid: FluidStack
            get() = tank.getFluidInTank(0)
        override val id: String = "moving_${contraption.entity.uuid}_${context.localPos.encode()}"
    }
}
