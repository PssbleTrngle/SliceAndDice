package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.api.ModRegistries
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction
import net.minecraft.core.Holder
import net.minecraft.resources.RegistryFileCodec
import net.neoforged.neoforge.fluids.crafting.FluidIngredient

data class Sprinkler(
    val fluid: FluidIngredient,
    val action: Holder<SprinkleAction>,
    val rangeBonus: Int = 0,
) {
    companion object {
        @JvmField
        val CODEC: Codec<Sprinkler> =
            RecordCodecBuilder.create { builder ->
                builder
                    .group(
                        FluidIngredient.CODEC.fieldOf("fluid").forGetter { it.fluid },
                        SprinkleAction.CODEC.fieldOf("action").forGetter { it.action },
                        Codec.INT.optionalFieldOf("rangeBonus", 0).forGetter { it.rangeBonus },
                    ).apply(builder, ::Sprinkler)
            }

        @JvmField
        val HOLDER_CODEC = RegistryFileCodec.create(ModRegistries.SPRINKLERS, CODEC, false)
    }
}
