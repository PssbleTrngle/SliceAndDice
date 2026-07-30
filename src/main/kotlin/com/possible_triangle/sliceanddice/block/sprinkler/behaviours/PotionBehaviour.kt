package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.alchemy.PotionUtils
import kotlin.math.max

object PotionBehaviour : SprinkleBehaviour {
    override fun act(
        range: SprinkleBehaviour.Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
        val effects = PotionUtils.getAllEffects(fluidStack.orCreateTag)
        if (effects.isEmpty()) return

        range.getEntities(LivingEntity::class.java).forEach { entity ->
            effects.forEach {
                if (it.effect.isInstantenous) {
                    it.effect.applyInstantenousEffect(null, null, entity, it.amplifier, 0.5)
                } else {
                    entity.addEffect(
                        MobEffectInstance(
                            it.effect,
                            max(20 * 12, it.duration / 25),
                            it.amplifier,
                            it.isAmbient,
                            it.isVisible,
                            it.showIcon(),
                        ),
                    )
                }
            }
        }
    }
}
