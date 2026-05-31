package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.neoforged.neoforge.fluids.FluidStack
import kotlin.math.max

object PotionAction : SprinkleAction {
    override fun act(
        range: SprinkleAction.Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
        val effects = fluidStack.get(DataComponents.POTION_CONTENTS)?.allEffects ?: return
        range.getEntities(LivingEntity::class.java).forEach { entity ->
            effects.forEach {
                if (it.effect.value().isInstantenous) {
                    it.effect.value().applyInstantenousEffect(null, null, entity, it.amplifier, 0.5)
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
