package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.blaze3d.vertex.PoseStack
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
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
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

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
            SprinklerType.FLOOR -> be.behaviour.floorRender(level, ms, buffer, light)
            SprinklerType.CEILING -> be.behaviour.ceilingRender(ms, buffer, light)
        }
    }

    companion object {
        fun renderInContraption(
            behaviour: SprinklerBehaviour,
            context: MovementContext,
            level: VirtualRenderWorld,
            matrices: ContraptionMatrices,
            buffer: MultiBufferSource,
        ) {
            val light = LevelRenderer.getLightColor(level, context.localPos)
            when (behaviour.type) {
                SprinklerType.FLOOR -> {
                    behaviour.floorRender(
                        level,
                        matrices.viewProjection,
                        buffer,
                        light,
                    ) {
                        transform(matrices.model)
                        useLevelLight<SuperByteBuffer>(context.world, matrices.world)
                    }
                }

                SprinklerType.CEILING -> {
                    behaviour.ceilingRender(
                        matrices.modelViewProjection,
                        buffer,
                        light,
                        Vec3.atLowerCornerOf(context.localPos),
                    )
                }
            }
        }

        private fun SprinklerBehaviour.floorRender(
            level: Level,
            ms: PoseStack,
            buffer: MultiBufferSource,
            light: Int,
            block: SuperByteBuffer.() -> Unit = {},
        ) {
            if (VisualizationManager.supportsVisualization(level)) return

            val vb = buffer.getBuffer(RenderType.solid())

            val time = AnimationTickHolder.getRenderTime(level)
            val angle = time * rotationSpeed / 20F % 360 / 180 * Math.PI.toFloat()

            // needs specific blockState?
            val headRender = CachedBuffers.partial(SDPartials.FLOOR_SPRINKLER_HEAD, SDBlocks.SPRINKLER.defaultState)
            headRender
                .apply(block)
                .rotateCentered(angle, Direction.UP)
                .light<SuperByteBuffer>(light)
                .renderInto(ms, vb)
        }

        private fun SprinklerBehaviour.ceilingRender(
            ms: PoseStack,
            buffer: MultiBufferSource,
            light: Int,
            offset: Position? = null,
        ) {
            if (renderedFluid.isEmpty) return

            ms.pushPose()

            offset?.let {
                ms.translate(it.x(), it.y(), it.z())
            }

            val from = 4 / 16F
            val to = 12 / 16F
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                renderedFluid,
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
