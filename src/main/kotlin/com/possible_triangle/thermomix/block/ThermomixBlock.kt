package com.possible_triangle.thermomix.block

import com.possible_triangle.thermomix.Content
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerBlock

class ThermomixBlock(properties: Properties) : MechanicalMixerBlock(properties) {

    override fun getTileEntityType() = Content.THERMOMIX_TILE.get()

}