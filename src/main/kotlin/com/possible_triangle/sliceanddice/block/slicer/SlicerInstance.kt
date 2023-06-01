package com.possible_triangle.sliceanddice.block.slicer

import com.jozufozu.flywheel.api.Instancer
import com.jozufozu.flywheel.api.MaterialManager
import com.jozufozu.flywheel.api.instance.DynamicInstance
import com.possible_triangle.sliceanddice.SlicerPartials
import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogInstance
import com.simibubi.create.foundation.render.AllMaterialSpecs
import com.simibubi.create.foundation.utility.AnimationTickHolder
import net.minecraft.core.Direction

class SlicerInstance(modelManager: MaterialManager, private val mixer: SlicerTile) :
    EncasedCogInstance(modelManager, mixer, false), DynamicInstance {

    private val mixerPole = orientedMaterial
        .getModel(AllPartialModels.MECHANICAL_MIXER_POLE, blockState)
        .createInstance()

    private val mixerHead = rotatingMaterial.getModel(SlicerPartials.SLICER_HEAD, blockState)
        .createInstance()
        .setRotationAxis(Direction.Axis.Y)

    init {
        val renderedHeadOffset: Float = getRenderedHeadOffset()
        transformPole(renderedHeadOffset)
        transformHead(renderedHeadOffset)
    }

    override fun getCogModel(): Instancer<RotatingData> {
        return materialManager.defaultSolid()
            .material(AllMaterialSpecs.ROTATING)
            .getModel(AllPartialModels.SHAFTLESS_COGWHEEL, blockEntity.blockState)
    }

    override fun beginFrame() {
        val renderedHeadOffset: Float = getRenderedHeadOffset()
        transformPole(renderedHeadOffset)
        transformHead(renderedHeadOffset)
    }

    private fun transformHead(renderedHeadOffset: Float) {
        val speed: Float = mixer.getRenderedHeadRotationSpeed()
        mixerHead.setPosition(instancePosition)
            .nudge(0f, -renderedHeadOffset, 0f)
            .setRotationalSpeed(speed * 2)
    }

    private fun transformPole(renderedHeadOffset: Float) {
        mixerPole.setPosition(instancePosition)
            .nudge(0f, -renderedHeadOffset, 0f)
    }

    private fun getRenderedHeadOffset(): Float {
        return mixer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks())
    }

    override fun updateLight() {
        super.updateLight()
        relight(pos.below(), mixerHead)
        relight(pos, mixerPole)
    }

    override fun remove() {
        super.remove()
        mixerHead.delete()
        mixerPole.delete()
    }

}