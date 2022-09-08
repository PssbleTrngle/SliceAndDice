package com.possible_triangle.sliceanddice.block.sprinkler.behaviours

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraftforge.fluids.FluidStack
import java.util.*

object BurningBehaviour : SprinkleBehaviour {

    override fun act(range: AABB, world: ServerLevel, fluid: FluidStack, random: Random) {
        world.getEntities(EntityTypeTest.forClass(LivingEntity::class.java), range) {
            !it.fireImmune()
        }.forEach {
            it.hurt(DamageSource.IN_FIRE, 0.5F)
        }
    }

    override fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random) {
        // logic already implemented in `act`
    }
}