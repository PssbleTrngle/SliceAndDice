package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.config.Configs
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraftforge.fluids.FluidStack
import kotlin.math.ceil
import kotlin.math.floor

private data class RegisteredBehaviour(
    val predicate: (FluidStack) -> Boolean,
    val behaviour: SprinkleBehaviour,
    val rangeBonus: Int,
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

        fun forEachGroundBlock(consumer: (BlockPos) -> Unit) {
            val minX = ceil(aabb.minX).toInt()
            val maxX = floor(aabb.maxX).toInt()
            val minZ = ceil(aabb.minZ).toInt()
            val maxZ = floor(aabb.maxZ).toInt()
            val minY = ceil(aabb.minY).toInt()
            val maxY = floor(aabb.maxY).toInt()

            for (x in minX..maxX) {
                horiz@
                for (z in minZ..maxZ) {
                    var y = maxY + 1
                    vert@
                    while (y > minY) {
                        y--
                        val pos = BlockPos(x, y, z)
                        val state = world.getBlockState(pos)
                        val shape = state.getCollisionShape(world, pos, CollisionContext.empty())

                        if (y == minY) { consumer(pos); continue@horiz }
                        if (state.isAir || shape.isEmpty) { continue@vert }
                        if (shape.equals(Shapes.block())) { consumer(pos); continue@horiz }
                    }
                }
            }
        }

    }

    fun act(range: Range, world: ServerLevel, fluidStack: FluidStack, random: RandomSource)

    companion object {
        private val BEHAVIOURS = arrayListOf<RegisteredBehaviour>()

        fun register(tag: TagKey<Fluid>, behaviour: SprinkleBehaviour, rangeBonus: Int = 0) {
            register({ it.fluid.`is`(tag) }, behaviour, rangeBonus)
        }

        fun register(predicate: (FluidStack) -> Boolean, behaviour: SprinkleBehaviour, rangeBonus: Int = 0) {
            BEHAVIOURS.add(RegisteredBehaviour(predicate, behaviour, rangeBonus))
        }

        fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: RandomSource) {
            BEHAVIOURS.filter { it.predicate(fluid) }.forEach {
                val radius = Configs.SERVER.SPRINKLER_RANGE.get()
                val area = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
                val range = Range(area, pos, world)
                it.behaviour.act(range, world, fluid, random)
            }
        }

    }
}