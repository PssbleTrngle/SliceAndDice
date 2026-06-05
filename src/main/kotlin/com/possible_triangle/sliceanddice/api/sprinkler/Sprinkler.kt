package com.possible_triangle.sliceanddice.api.sprinkler

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.LOGGER
import com.possible_triangle.sliceanddice.api.SDRegistries
import net.minecraft.core.Holder
import net.minecraft.core.RegistryAccess
import net.minecraft.resources.RegistryFileCodec
import net.minecraft.util.ExtraCodecs
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.crafting.FluidIngredient

data class Sprinkler(
    val fluid: FluidIngredient,
    val action: Holder<SprinkleAction>,
    val rangeBonus: Int = 0,
    val tickRate: Int = 0,
) {
    companion object {
        @JvmField
        val CODEC: Codec<Sprinkler> =
            RecordCodecBuilder.create { builder ->
                builder
                    .group(
                        FluidIngredient.CODEC.fieldOf("fluid").forGetter { it.fluid },
                        SprinkleAction.CODEC.fieldOf("action").forGetter { it.action },
                        ExtraCodecs.POSITIVE_INT.optionalFieldOf("rangeBonus", 0).forGetter { it.rangeBonus },
                        ExtraCodecs.POSITIVE_INT.optionalFieldOf("tickRate", 0).forGetter { it.tickRate },
                    ).apply(builder, ::Sprinkler)
            }

        @JvmField
        val HOLDER_CODEC = RegistryFileCodec.create(SDRegistries.SPRINKLERS, CODEC, false)

        fun findMatching(
            registries: RegistryAccess,
            fluid: FluidStack,
        ): Collection<Holder<Sprinkler>> {
            val sprinklers = registries.lookup(SDRegistries.SPRINKLERS)
            if (sprinklers.isEmpty) {
                LOGGER.warn("unable to find sprinklers registry")
                return emptyList()
            }
            return sprinklers
                .get()
                .listElements()
                .filter { it.value().fluid.test(fluid) }
                .toList()
        }
    }
}
