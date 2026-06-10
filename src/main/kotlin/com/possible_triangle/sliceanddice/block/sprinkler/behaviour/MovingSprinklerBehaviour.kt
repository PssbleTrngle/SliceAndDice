package com.possible_triangle.sliceanddice.block.sprinkler.behaviour

import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerRenderer
import com.simibubi.create.api.behaviour.movement.MovementBehaviour
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import com.simibubi.create.content.contraptions.render.ContraptionMatrices
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel

object MovingSprinklerBehaviour : MovementBehaviour {
    @Suppress("UNCHECKED_CAST")
    private var MovementContext.running
        get() = (temporaryData as Collection<Holder<Sprinkler<*>>>?) ?: emptyList()
        set(value) {
            temporaryData = value
        }

    @Suppress("UNCHECKED_CAST")
    private var MovementContext.behaviour
        get() = (temporaryData as Instance?)
        set(value) {
            temporaryData = value
        }

    override fun visitNewPosition(
        context: MovementContext,
        pos: BlockPos,
    ) {
        val level = context.world
        val tank = context.contraption.getStorage().fluids

        // TODO could happen in setter if level is part of Instance
        context.behaviour?.invalidate(level)
        context.behaviour = Instance(context)

        context.behaviour!!.check(tank, level)
    }

    override fun tick(context: MovementContext) {
        val behaviour = context.behaviour?.takeIf { it.active } ?: return
        val tank = context.contraption.getStorage().fluids
        val level = context.world

        behaviour.pos = context.position
        // TODO move to tick?
        behaviour.remainingTicks--

        val fluid = tank.getFluidInTank(0)
        // TODO move to tick?
        behaviour.spawnParticles(fluid, context.world)

        if (level is ServerLevel) {
            behaviour.tickSprinklers(level, fluid)
        }
    }

    override fun disableBlockEntityRendering() = true

    override fun renderInContraption(
        context: MovementContext,
        level: VirtualRenderWorld,
        matrices: ContraptionMatrices,
        buffer: MultiBufferSource,
    ) {
        val behaviour = context.behaviour ?: return
        SprinklerRenderer.renderInContraption(behaviour, level, matrices, buffer)
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
    }
}
