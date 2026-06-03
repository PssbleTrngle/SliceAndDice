package com.possible_triangle.sliceanddice.compat.jei

import com.mojang.math.Axis
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.possible_triangle.sliceanddice.index.SDPartials
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllPartialModels
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics
import com.simibubi.create.content.processing.recipe.ProcessingRecipe
import net.createmod.catnip.animation.AnimationTickHolder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class AnimatedSlicer(
    private val basin: Boolean,
) : AnimatedKinetics() {
    private var held: ItemStack = ItemStack.EMPTY

    fun setRecipe(recipe: ProcessingRecipe<*, *>) {
        if (recipe is CuttingProcessingRecipe) {
            val stack =
                recipe.params.tool
                    ?.items
                    ?.firstOrNull()
            held = stack ?: ItemStack.EMPTY
        }
    }

    override fun draw(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
    ) {
        val matrixStack = graphics.pose()
        matrixStack.pushPose()
        matrixStack.translate(x.toFloat(), y.toFloat(), 200.0f)
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f))
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f))

        val scale = if (basin) 23 else 24

        blockElement(cogwheel())
            .rotateBlock(0.0, (getCurrentAngle() * 2.0f).toDouble(), 0.0)
            .atLocal(0.0, 0.0, 0.0)
            .scale(scale.toDouble())
            .render(graphics)
        blockElement(SDBlocks.SLICER.defaultState)
            .atLocal(0.0, 0.0, 0.0)
            .scale(scale.toDouble())
            .render(graphics)

        val animation = (Mth.sin(AnimationTickHolder.getRenderTime() / 4.0f) + 1.0f) / 5.0f + 0.5f

        blockElement(AllPartialModels.MECHANICAL_MIXER_POLE)
            .atLocal(0.0, animation.toDouble(), 0.0)
            .scale(scale.toDouble())
            .render(graphics)

        val angle = getCurrentAngle() * 4.0F

        blockElement(SDPartials.SLICER_HEAD)
            .rotateBlock(0.0, angle.toDouble(), 0.0)
            .atLocal(0.0, animation.toDouble(), 0.0)
            .scale(scale.toDouble())
            .render(graphics)

        if (basin) {
            blockElement(AllBlocks.BASIN.defaultState)
                .atLocal(0.0, 1.65, 0.0)
                .scale(scale.toDouble())
                .render(graphics)
        }

        if (!held.isEmpty) {
            matrixStack.pushPose()
            val buffer = Minecraft.getInstance().renderBuffers().bufferSource()
            val toolScale = scale * 0.7F
            matrixStack.scale(toolScale, toolScale, toolScale)
            matrixStack.translate(1F, animation + 0.4F, 0F)

            for (i in 0..3) {
                matrixStack.pushPose()
                matrixStack.mulPose(Axis.YP.rotationDegrees(90F * i))
                matrixStack.mulPose(Axis.YP.rotationDegrees(angle))
                matrixStack.mulPose(Axis.ZN.rotationDegrees(20F))
                matrixStack.translate(0.0, 0.0, 0.4)

                val renderer = Minecraft.getInstance().itemRenderer
                renderer.renderStatic(
                    held,
                    ItemDisplayContext.FIXED,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    matrixStack,
                    buffer,
                    null,
                    0,
                )
                matrixStack.popPose()
            }
            matrixStack.popPose()
        }

        matrixStack.popPose()
    }
}
