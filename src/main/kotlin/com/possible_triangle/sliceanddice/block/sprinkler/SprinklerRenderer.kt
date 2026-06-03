package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.sliceanddice.index.SDPartials
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer
import dev.engine_room.flywheel.api.visualization.VisualizationManager
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.platform.NeoForgeCatnipServices
import net.createmod.catnip.render.CachedBuffers
import net.createmod.catnip.render.SuperByteBuffer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction

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
        when (be.type) {
            SprinklerBlock.Type.FLOOR -> be.floorRender(ms, buffer, light)
            SprinklerBlock.Type.CEILING -> be.ceilingRender(ms, buffer, light)
        }
    }

    private fun SprinklerBlockEntity.floorRender(
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
    ) {
        if (VisualizationManager.supportsVisualization(level!!)) return

        val vb = buffer.getBuffer(RenderType.solid())

        val time = AnimationTickHolder.getRenderTime(level!!)
        val angle = time * rotationSpeed / 20F % 360 / 180 * Math.PI.toFloat()

        val headRender = CachedBuffers.partial(SDPartials.FLOOR_SPRINKLER_HEAD, blockState)
        headRender
            .rotateCentered(angle, Direction.UP)
            .light<SuperByteBuffer>(light)
            .renderInto(ms, vb)
    }

    private fun SprinklerBlockEntity.ceilingRender(
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
    ) {
        if (tank.isEmpty) return

        ms.pushPose()
        ms.translate(0f, 0F, 0f)

        val from = 4 / 16F
        val to = 12 / 16F
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
            tank.primaryTank.renderedFluid,
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
