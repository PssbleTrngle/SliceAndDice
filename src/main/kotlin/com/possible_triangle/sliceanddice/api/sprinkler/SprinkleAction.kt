package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.serialization.Codec
import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import dev.ryanhcode.sable.companion.SableCompanion
import net.createmod.catnip.outliner.Outliner
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.Position
import net.minecraft.core.RegistryAccess
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
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
                SDRegistries.SPRINKLER_ACTIONS_REGISTRY.holderByNameCodec()
            }

        fun findMatching(
            registries: RegistryAccess,
            fluid: FluidStack,
        ): Collection<Holder<Sprinkler>> {
            val sprinklers = registries.lookupOrThrow(SDRegistries.SPRINKLERS)
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
        private val level: ServerLevel,
        type: SprinklerBlock.Type,
    ) {
        val subLevel = SableCompanion.INSTANCE.getContaining(level, origin)

        val aabb =
            Vec3.atBottomCenterOf(origin).let {
                val yOffset =
                    when (type) {
                        SprinklerBlock.Type.FLOOR -> 3
                        SprinklerBlock.Type.CEILING -> 0
                    }
                AABB(
                    it.x - size.x / 2.0,
                    it.y - size.y.toDouble() + yOffset,
                    it.z - size.z / 2.0,
                    it.x + size.x / 2.0,
                    it.y + yOffset,
                    it.z + size.z / 2.0,
                )
            }

        fun <T : Entity> getEntities(
            clazz: Class<T>,
            predicate: (T) -> Boolean = {
                true
            },
        ): List<T> = level.getEntities(EntityTypeTest.forClass(clazz), aabb, predicate)

        private fun execute(
            pos: BlockPos,
            consumer: (BlockPos) -> Unit,
        ) {
            val outside = subLevel?.logicalPose()?.transformPosition(pos.center)?.let(BlockPos::containing) ?: pos

            consumer(outside)
            Outliner
                .getInstance()
                .chaseAABB(this, aabb)

            SableCompanion.INSTANCE.runIncludingSubLevels(level, outside.center as Position, true, subLevel) { _, it ->
                consumer(it)
                null
            }
        }

        fun forEachBlock(consumer: (BlockPos) -> Unit) {
            for (pos in BlockPos.betweenClosed(
                ceil(aabb.minX).toInt(),
                ceil(aabb.minY).toInt(),
                ceil(aabb.minZ).toInt(),
                floor(aabb.maxX).toInt(),
                floor(aabb.maxY).toInt(),
                floor(aabb.maxZ).toInt(),
            )) {
                execute(pos, consumer)
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
                        val state = level.getBlockState(pos)
                        val shape = state.getCollisionShape(level, pos, CollisionContext.empty())

                        if (y == minY) {
                            execute(pos, consumer)
                            continue@horiz
                        }
                        if (state.isAir || shape.isEmpty) {
                            continue@vert
                        }
                        if (shape.equals(Shapes.block())) {
                            execute(pos, consumer)
                            continue@horiz
                        }
                    }
                }
            }
        }
    }
}
