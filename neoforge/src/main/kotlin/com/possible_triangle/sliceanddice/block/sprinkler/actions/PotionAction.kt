package com.possible_triangle.sliceanddice.block.sprinkler.actions

import com.possible_triangle.sliceanddice.api.sprinkler.SimpleSprinkleActionType
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import net.minecraft.core.component.DataComponents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import kotlin.math.max

object PotionAction : SprinkleAction {
    override fun type() = Type

    object Type : SimpleSprinkleActionType<FertilizerAction>(FertilizerAction) {
        override fun tick(
            context: SprinkleContext,
            config: FertilizerAction,
        ) {
            val effects = context.fluidStack.get(DataComponents.POTION_CONTENTS)?.allEffects ?: return
            context.getEntities(LivingEntity::class.java).forEach { entity ->
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
}
