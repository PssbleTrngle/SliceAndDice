package com.possible_triangle.sliceanddice.block.sprinkler

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.AABB
import net.minecraftforge.fluids.FluidStack
import kotlin.math.ceil
import kotlin.math.floor

private data class RegisteredBehaviour(
    val predicate: (FluidStack) -> Boolean,
    val behaviour: SprinkleBehaviour,
    val range: Vec3i,
)


fun interface SprinkleBehaviour {

    class Range(size: Vec3i, origin: BlockPos, private val world: ServerLevel) {

        val aabb = AABB(
            origin.x - size.x / 2.0,
            origin.y - size.y.toDouble(),
            origin.z - size.z / 2.0,
            origin.x + size.x / 2.0,
            origin.y - 1.0,
            origin.z + size.z / 2.0
        )

        fun <T : Entity> getEntities(clazz: Class<T>, predicate: (T) -> Boolean = { true }): List<T> {
            return world.getEntities(EntityTypeTest.forClass(clazz), aabb, predicate)
        }

        fun forEachBlock(consumer: (BlockPos) -> Unit) {
            for (block in BlockPos.betweenClosed(
                ceil(aabb.minX).toInt(), ceil(aabb.minY).toInt(), ceil(aabb.minZ).toInt(),
                floor(aabb.maxX).toInt(), floor(aabb.maxY).toInt(), floor(aabb.maxZ).toInt(),
            )) {
                consumer(block)
            }
        }

    }

    fun act(range: Range, world: ServerLevel, fluidStack: FluidStack, random: RandomSource)

    companion object {
        private val BEHAVIOURS = arrayListOf<RegisteredBehaviour>()

        private val DEFAULT_RANGE = Vec3i(5, 7, 5)

        fun register(tag: TagKey<Fluid>, behaviour: SprinkleBehaviour, range: Vec3i = DEFAULT_RANGE) {
            register({ it.fluid.`is`(tag) }, behaviour, range)
        }

        fun register(predicate: (FluidStack) -> Boolean, behaviour: SprinkleBehaviour, range: Vec3i = DEFAULT_RANGE) {
            BEHAVIOURS.add(RegisteredBehaviour(predicate, behaviour, range))
        }

        fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: RandomSource) {
            BEHAVIOURS.filter { it.predicate(fluid) }.forEach {
                val range = Range(it.range, pos, world)
                it.behaviour.act(range, world, fluid, random)
            }
        }

    }
}