package com.possible_triangle.thermomix.block.instance

import com.jozufozu.flywheel.api.Instancer
import com.jozufozu.flywheel.api.MaterialManager
import com.jozufozu.flywheel.api.instance.DynamicInstance
import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.simibubi.create.AllBlockPartials
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData
import com.simibubi.create.content.contraptions.relays.encased.EncasedCogInstance
import com.simibubi.create.foundation.render.AllMaterialSpecs
import com.simibubi.create.foundation.utility.AnimationTickHolder
import net.minecraft.core.Direction

class ThermomixInstance(modelManager: MaterialManager, private val mixer: ThermomixTile) :
    EncasedCogInstance(modelManager, mixer, false), DynamicInstance {

    private val mixerPole = orientedMaterial
        .getModel(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState)
        .createInstance()

    private val mixerHead = rotatingMaterial.getModel(Content.THERMOMIX_HEAD, blockState)
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
            .getModel(AllBlockPartials.SHAFTLESS_COGWHEEL, blockEntity.blockState)
    }

    override fun beginFrame() {
        val renderedHeadOffset: Float = getRenderedHeadOffset()
        transformPole(renderedHeadOffset)
        transformHead(renderedHeadOffset)
    }

    private fun transformHead(renderedHeadOffset: Float) {
        val speed: Float = mixer.getRenderedHeadRotationSpeed(AnimationTickHolder.getPartialTicks())
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