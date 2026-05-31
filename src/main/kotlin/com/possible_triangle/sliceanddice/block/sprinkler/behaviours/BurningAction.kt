package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.neoforged.neoforge.fluids.FluidStack

object BurningAction : SprinkleAction {
    override fun act(
        range: SprinkleAction.Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
        range
            .getEntities(LivingEntity::class.java) {
                !it.fireImmune()
            }.forEach {
                it.hurt(world.damageSources().inFire(), 0.5F)
            }
    }
}
