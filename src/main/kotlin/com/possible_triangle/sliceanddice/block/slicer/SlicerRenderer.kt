package com.possible_triangle.sliceanddice.block.slicer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.possible_triangle.sliceanddice.SlicerPartials
import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
import dev.engine_room.flywheel.api.visualization.VisualizationManager
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.render.CachedBuffers
import net.createmod.catnip.render.SuperByteBuffer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext

class SlicerRenderer(context: BlockEntityRendererProvider.Context) : KineticBlockEntityRenderer<SlicerBlockEntity>(context) {

    override fun shouldRenderOffScreen(te: SlicerBlockEntity) = true

    private fun renderTool(
        tile: SlicerBlockEntity, partialTicks: Float, ms: PoseStack, buffer: MultiBufferSource,
        light: Int, overlay: Int,
    ) {
        if (tile.heldItem.isEmpty) return
        ms.pushPose()

        val offset = 0.4
        val scale = 0.7F

        val renderedHeadOffset = tile.getRenderedHeadOffset(partialTicks).toDouble()
        ms.translate(0.5, -renderedHeadOffset, 0.5)
        ms.scale(scale, scale, scale)

        val speed: Float = tile.getRenderedHeadRotationSpeed()
        val time = AnimationTickHolder.getRenderTime(tile.level!!)
        val angle = time * speed * 6 / 10f % 360 / 180 * Math.PI.toFloat()

        for (i in 0..3) {
            ms.pushPose()
            ms.mulPose(Axis.YP.rotationDegrees(90F * i))
            ms.mulPose(Axis.YP.rotation(angle))
            ms.mulPose(Axis.ZN.rotationDegrees(200F))
            ms.translate(0.0, 0.0, offset)

            val renderer = Minecraft.getInstance().itemRenderer
            renderer.renderStatic(tile.heldItem, ItemDisplayContext.FIXED, light, overlay, ms, buffer, tile.level, 0)
            ms.popPose()
        }

        ms.popPose()
    }

    override fun renderSafe(
        te: SlicerBlockEntity,
        partialTicks: Float,
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        renderTool(te, partialTicks, ms, buffer, light, overlay)

        if (VisualizationManager.supportsVisualization(te.level!!)) return

        val blockState = te.blockState

        val vb = buffer.getBuffer(RenderType.solid())

        val superBuffer = CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL, blockState)
        standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb)

        val renderedHeadOffset = te.getRenderedHeadOffset(partialTicks)
        val speed = te.getRenderedHeadRotationSpeed()
        val time = AnimationTickHolder.getRenderTime(te.level!!)
        val angle = time * speed * 6 / 10f % 360 / 180 * Math.PI.toFloat()

        val poleRender = CachedBuffers.partial(AllPartialModels.MECHANICAL_MIXER_POLE, blockState)
        poleRender.translate(0.0, -renderedHeadOffset.toDouble(), 0.0)
            .light<SuperByteBuffer>(light)
            .renderInto(ms, vb)

        val headRender = CachedBuffers.partial(SlicerPartials.SLICER_HEAD, blockState)
        headRender.rotateCentered(angle, Direction.UP)
            .translate(0.0, -renderedHeadOffset.toDouble(), 0.0)
            .light<SuperByteBuffer>(light)
            .renderInto(ms, vb)
    }

}