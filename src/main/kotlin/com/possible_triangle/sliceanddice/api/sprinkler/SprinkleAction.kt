package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.serialization.Codec
import com.possible_triangle.sliceanddice.api.ModRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.RegistryAccess
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.neoforged.neoforge.fluids.FluidStack
import kotlin.math.ceil
import kotlin.math.floor

interface SprinkleAction {
    companion object {
        @JvmField
        val CODEC: Codec<Holder<SprinkleAction>> =
            Codec.lazyInitialized {
                ModRegistries.SPRINKLER_ACTIONS_REGISTRY.holderByNameCodec()
            }

        fun findMatching(
            registries: RegistryAccess,
            fluid: FluidStack,
        ): Collection<Holder<Sprinkler>> {
            val sprinklers = registries.lookupOrThrow(ModRegistries.SPRINKLERS)
            return sprinklers
                .listElements()
                .filter { it.value().fluid.test(fluid) }
                .toList()
        }
    }

    fun act(
        range: Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
    }

    fun start(
        range: Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
    }

    fun stop(
        range: Range,
        world: ServerLevel,
        fluidStack: FluidStack,
        random: RandomSource,
    ) {
    }

    class Range(
        size: Vec3i,
        val origin: BlockPos,
        private val world: ServerLevel,
    ) {
        val aabb =
            AABB(
                origin.x - size.x / 2.0,
                origin.y - size.y.toDouble(),
                origin.z - size.z / 2.0,
                origin.x + size.x / 2.0,
                origin.y - 1.0,
                origin.z + size.z / 2.0,
            )

        fun <T : Entity> getEntities(
            clazz: Class<T>,
            predicate: (T) -> Boolean = {
                true
            },
        ): List<T> = world.getEntities(EntityTypeTest.forClass(clazz), aabb, predicate)

        fun forEachBlock(consumer: (BlockPos) -> Unit) {
            for (block in BlockPos.betweenClosed(
                ceil(aabb.minX).toInt(),
                ceil(aabb.minY).toInt(),
                ceil(aabb.minZ).toInt(),
                floor(aabb.maxX).toInt(),
                floor(aabb.maxY).toInt(),
                floor(aabb.maxZ).toInt(),
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

                        if (y == minY) {
                            consumer(pos)
                            continue@horiz
                        }
                        if (state.isAir || shape.isEmpty) {
                            continue@vert
                        }
                        if (shape.equals(Shapes.block())) {
                            consumer(pos)
                            continue@horiz
                        }
                    }
                }
            }
        }
    }
}
