package com.possible_triangle.sliceanddice.block.sprinkler.actions

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkeContext
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerActionType
import net.minecraft.core.BlockPos
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper

data class ExperienceAction(
    val amount: Float,
) : SprinkleAction {
    companion object {
        val CODEC: MapCodec<ExperienceAction> =
            RecordCodecBuilder.mapCodec { builder ->
                builder
                    .group(
                        ExtraCodecs.POSITIVE_FLOAT.fieldOf("amount").forGetter { it.amount },
                    ).apply(builder, ::ExperienceAction)
            }
    }

    override fun type() = Type

    object Type : SprinklerActionType<ExperienceAction> {
        override fun codec() = CODEC

        override fun consume(
            context: SprinkeContext,
            config: ExperienceAction,
        ) {
            val players = context.getEntities(Player::class.java)

            // TODO pass actual drained fluid
            val totalAmount = ExperienceHelper.getExperienceFromFluid(context.fluidStack)

            if (players.isEmpty()) {
                val blocks = ArrayList<BlockPos>()
                context.forEachGroundBlock { pos ->
                    if (context.random.nextFloat() <= 0.25) {
                        blocks.add(pos)
                    }
                }
                val xp = totalAmount / blocks.size.toFloat()
                blocks.forEach { pos ->
                    val drop = ExperienceOrb(context.level, pos.center.x(), pos.center.y(), pos.center.z(), xp.toInt())
                    drop.setPos(pos.center)
                    context.level.addFreshEntity(drop)
                }
            } else {
                val xp = totalAmount / players.size.toFloat()
                players.forEach { player ->
                    ExperienceOrb.award(context.level, player.position(), xp.toInt())
                }
            }
        }
    }
}
