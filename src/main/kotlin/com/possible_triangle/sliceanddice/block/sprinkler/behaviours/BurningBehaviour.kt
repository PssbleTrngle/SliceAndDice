package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity

object BurningBehaviour : SprinkleBehaviour {

    override fun act(range: SprinkleBehaviour.Range, world: ServerLevel, fluidStack: FluidStack, random: RandomSource) {
        range.getEntities(LivingEntity::class.java) {
            !it.fireImmune()
        }.forEach {
            it.hurt(world.damageSources().inFire(), 0.5F)
        }
    }
}