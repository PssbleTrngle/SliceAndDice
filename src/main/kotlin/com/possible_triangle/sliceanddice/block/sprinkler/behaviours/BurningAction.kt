package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.api.sprinkler.SprinkeContext
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import net.minecraft.world.entity.LivingEntity

object BurningAction : SprinkleAction {
    override fun tick(context: SprinkeContext) {
        context
            .getEntities(LivingEntity::class.java) {
                !it.fireImmune()
            }.forEach {
                it.hurt(context.level.damageSources().inFire(), 0.5F)
            }
    }
}
