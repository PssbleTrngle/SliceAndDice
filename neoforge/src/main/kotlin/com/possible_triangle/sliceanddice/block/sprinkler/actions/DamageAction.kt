package com.possible_triangle.sliceanddice.block.sprinkler.actions

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleActionType
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import net.minecraft.core.Holder
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.LivingEntity

data class DamageAction(
    val type: Holder<DamageType>,
    val amount: Float,
) : SprinkleAction {
    companion object {
        val CODEC: MapCodec<DamageAction> =
            RecordCodecBuilder.mapCodec { builder ->
                builder
                    .group(
                        DamageType.CODEC.fieldOf("damage_type").forGetter { it.type },
                        ExtraCodecs.POSITIVE_FLOAT.fieldOf("amount").forGetter { it.amount },
                    ).apply(builder, ::DamageAction)
            }
    }

    override fun type(): SprinkleActionType<*> = Type

    object Type : SprinkleActionType<DamageAction> {
        override fun tick(
            context: SprinkleContext,
            config: DamageAction,
        ) {
            context
                .getEntities(LivingEntity::class.java) {
                    !it.fireImmune()
                }.forEach {
                    val source = DamageSource(config.type)
                    it.hurt(source, config.amount)
                }
        }

        override fun codec() = CODEC
    }
}
