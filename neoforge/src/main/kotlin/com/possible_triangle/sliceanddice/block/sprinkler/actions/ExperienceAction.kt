package com.possible_triangle.sliceanddice.block.sprinkler.actions

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleActionType
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import net.minecraft.core.BlockPos
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.ExperienceOrb
import net.minecraft.world.entity.player.Player

data class ExperienceAction(
    val factor: Float,
) : SprinkleAction {
    companion object {
        val CODEC: MapCodec<ExperienceAction> =
            RecordCodecBuilder.mapCodec { builder ->
                builder
                    .group(
                        ExtraCodecs.POSITIVE_FLOAT.fieldOf("factor").forGetter { it.factor },
                    ).apply(builder, ::ExperienceAction)
            }
    }

    override fun type() = Type

    object Type : SprinkleActionType<ExperienceAction> {
        override fun codec() = CODEC

        override fun consume(
            context: SprinkleContext,
            config: ExperienceAction,
        ) {
            val players = context.getEntities(Player::class.java)

            val totalAmount = context.fluidStack.amount * config.factor

            if (players.isEmpty()) {
                dropOnFloor(totalAmount, context)
            } else {
                giveToPlayers(totalAmount, players, context)
            }
        }

        private fun giveToPlayers(
            totalAmount: Float,
            players: List<Player>,
            context: SprinkleContext,
        ) {
            val xp = totalAmount / players.size.toFloat()
            players.forEach { player ->
                ExperienceOrb.award(context.level, player.position(), xp.toInt())
            }
        }

        private fun dropOnFloor(
            totalAmount: Float,
            context: SprinkleContext,
        ) {
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
        }
    }
}
