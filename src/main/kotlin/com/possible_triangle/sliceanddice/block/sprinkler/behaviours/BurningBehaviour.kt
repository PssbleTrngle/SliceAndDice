package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.fluids.FluidStack
import java.util.*

object BurningBehaviour : SprinkleBehaviour {

    override fun act(range: SprinkleBehaviour.Range, world: ServerLevel, fluidStack: FluidStack, random: Random) {
        range.getEntities(LivingEntity::class.java) {
            !it.fireImmune()
        }.forEach {
            it.hurt(DamageSource.IN_FIRE, 0.5F)
        }
    }
}