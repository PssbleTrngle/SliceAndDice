package com.possible_triangle.thermomix.block.renderer

import com.jozufozu.flywheel.backend.Backend
import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.simibubi.create.AllBlockPartials
import com.simibubi.create.content.contraptions.base.KineticTileEntity
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerRenderer
import com.simibubi.create.foundation.render.CachedBufferer
import com.simibubi.create.foundation.utility.AnimationTickHolder
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction

class ThermomixRenderer(context: BlockEntityRendererProvider.Context?) : MechanicalMixerRenderer(context) {

    private fun renderTool(
        tile: ThermomixTile, partialTicks: Float, ms: PoseStack, buffer: MultiBufferSource,
        light: Int, overlay: Int,
    ) {
        if (tile.heldItem.isEmpty) return
        ms.pushPose()

        val renderedHeadOffset = tile.getRenderedHeadOffset(partialTicks).toDouble()
        ms.translate(0.5, renderedHeadOffset, 0.5)

        val renderer = Minecraft.getInstance().itemRenderer
        renderer.renderStatic(tile.heldItem, TransformType.FIXED, light, overlay, ms, buffer, 0)
        ms.popPose()
    }

    override fun renderSafe(
        te: KineticTileEntity,
        partialTicks: Float,
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        if (te !is ThermomixTile) return
        renderTool(te, partialTicks, ms, buffer, light, overlay)

        if (Backend.canUseInstancing(te.level)) return

        val blockState = te.blockState

        val vb = buffer.getBuffer(RenderType.solid())

        val superBuffer = CachedBufferer.partial(AllBlockPartials.SHAFTLESS_COGWHEEL, blockState)
        standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb)

        val renderedHeadOffset = te.getRenderedHeadOffset(partialTicks)
        val speed = te.getRenderedHeadRotationSpeed(partialTicks)
        val time = AnimationTickHolder.getRenderTime(te.getLevel())
        val angle = time * speed * 6 / 10f % 360 / 180 * Math.PI.toFloat()

        val poleRender = CachedBufferer.partial(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState)
        poleRender.translate(0.0, -renderedHeadOffset.toDouble(), 0.0)
            .light(light)
            .renderInto(ms, vb)

        val headRender = CachedBufferer.partial(Content.THERMOMIX_HEAD, blockState)
        headRender.rotateCentered(Direction.UP, angle)
            .translate(0.0, -renderedHeadOffset.toDouble(), 0.0)
            .light(light)
            .renderInto(ms, vb)
    }

}