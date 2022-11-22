package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraftforge.fluids.FluidStack
import kotlin.math.max

object PotionBehaviour : SprinkleBehaviour {

    override fun act(range: SprinkleBehaviour.Range, world: ServerLevel, fluidStack: FluidStack, random: RandomSource) {
        val effects = PotionUtils.getAllEffects(fluidStack.orCreateTag)
        if (effects.isEmpty()) return

        range.getEntities(LivingEntity::class.java).forEach { entity ->
            effects.forEach {
                val shorter = MobEffectInstance(
                    it.effect,
                    max(20 * 2, it.duration / 50),
                    it.amplifier,
                    it.isAmbient,
                    it.isVisible,
                    it.showIcon()
                )
                entity.addEffect(shorter)
            }
        }
    }
}