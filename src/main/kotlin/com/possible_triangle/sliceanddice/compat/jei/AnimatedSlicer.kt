package com.possible_triangle.sliceanddice.compat.jei

import com.mojang.math.Axis
import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.SlicerPartials
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllPartialModels
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics
import net.createmod.catnip.animation.AnimationTickHolder
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.Mth

class AnimatedSlicer(private val basin: Boolean) : AnimatedKinetics() {

    override fun draw(graphics: GuiGraphics, x: Int, y: Int) {
        val matrixStack = graphics.pose()
        matrixStack.pushPose()
        matrixStack.translate(x.toFloat(), y.toFloat(), 200.0f)
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f))
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f))

        val scale = if(basin) 23 else 24

        blockElement(cogwheel()).rotateBlock(0.0, (getCurrentAngle() * 2.0f).toDouble(), 0.0)
            .atLocal(0.0, 0.0, 0.0).scale(scale.toDouble()).render(graphics)
        blockElement(Content.SLICER_BLOCK.defaultState).atLocal(0.0, 0.0, 0.0).scale(scale.toDouble())
            .render(graphics)

        val animation = (Mth.sin(AnimationTickHolder.getRenderTime() / 4.0f) + 1.0f) / 5.0f + 0.5f

        blockElement(AllPartialModels.MECHANICAL_MIXER_POLE).atLocal(0.0, animation.toDouble(), 0.0)
            .scale(scale.toDouble()).render(graphics)

        blockElement(SlicerPartials.SLICER_HEAD)
            .rotateBlock(0.0, (getCurrentAngle() * 4.0f).toDouble(), 0.0).atLocal(0.0, animation.toDouble(), 0.0)
            .scale(scale.toDouble()).render(graphics)

        if(basin) {
            blockElement(AllBlocks.BASIN.defaultState).atLocal(0.0, 1.65, 0.0).scale(scale.toDouble())
                .render(graphics)
        }

        matrixStack.popPose()
    }

}