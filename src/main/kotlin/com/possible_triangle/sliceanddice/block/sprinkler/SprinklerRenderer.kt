package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.sliceanddice.block.sprinkler.behaviour.SprinklerBehaviour
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.possible_triangle.sliceanddice.index.SDPartials
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import com.simibubi.create.content.contraptions.render.ContraptionMatrices
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld
import dev.engine_room.flywheel.api.visualization.VisualizationManager
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.platform.NeoForgeCatnipServices
import net.createmod.catnip.render.CachedBuffers
import net.createmod.catnip.render.SuperByteBuffer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack

class SprinklerRenderer(
    context: BlockEntityRendererProvider.Context,
) : SafeBlockEntityRenderer<SprinklerBlockEntity>() {
    override fun renderSafe(
        be: SprinklerBlockEntity,
        partialTicks: Float,
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        val level = be.level ?: return
        when (be.type) {
            SprinklerBlock.Type.FLOOR -> floorRender(level, ms, buffer, light)
            SprinklerBlock.Type.CEILING -> ceilingRender(be.tank.primaryTank.renderedFluid, ms, buffer, light)
        }
    }

    companion object {
        fun renderInContraption(
            behaviour: SprinklerBehaviour,
            level: VirtualRenderWorld,
            matrices: ContraptionMatrices,
            buffer: MultiBufferSource,
        ) {
            val ms = matrices.viewProjection
            val light = LevelRenderer.getLightColor(level, BlockPos.containing(behaviour.pos))
            when (behaviour.type) {
                SprinklerBlock.Type.FLOOR -> floorRender(level, ms, buffer, light)
                SprinklerBlock.Type.CEILING -> ceilingRender(level, ms, buffer, light)
            }
        }

        private fun floorRender(
            level: Level,
            ms: PoseStack,
            buffer: MultiBufferSource,
            light: Int,
        ) {
            if (VisualizationManager.supportsVisualization(level)) return

            val vb = buffer.getBuffer(RenderType.solid())

            val time = AnimationTickHolder.getRenderTime(level)
            val angle = time * rotationSpeed / 20F % 360 / 180 * Math.PI.toFloat()

            // needs specific blockState?
            val headRender = CachedBuffers.partial(SDPartials.FLOOR_SPRINKLER_HEAD, SDBlocks.SPRINKLER.defaultState)
            headRender
                .rotateCentered(angle, Direction.UP)
                .light<SuperByteBuffer>(light)
                .renderInto(ms, vb)
        }

        private fun ceilingRender(
            fluid: FluidStack,
            ms: PoseStack,
            buffer: MultiBufferSource,
            light: Int,
        ) {
            if (fluid.isEmpty) return

            ms.pushPose()
            ms.translate(0f, 0F, 0f)

            val from = 4 / 16F
            val to = 12 / 16F
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                fluid,
                from,
                11 / 16F,
                from,
                to,
                12 / 16F,
                to,
                buffer,
                ms,
                light,
                true,
                false,
            )

            ms.popPose()
        }
    }
}
