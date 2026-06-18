package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleContext
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.modLoc
import dev.ryanhcode.sable.companion.SableCompanion
import net.minecraft.core.BlockPos
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.neoforged.neoforge.fluids.FluidStack
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor

data class SprinkleContextImpl(
    override val pos: Position,
    override val size: Vec3i,
    override val level: ServerLevel,
    override val fluidStack: FluidStack,
    override val type: SprinklerType,
    override val contraption: UUID?,
    val suffix: String,
) : SprinkleContext {
    private val subLevel = SableCompanion.INSTANCE.getContaining(level, pos)
    override val blockPos = BlockPos.containing(pos)
    override val random: RandomSource = level.random

    override val area =
        pos.let {
            val yOffset =
                when (type) {
                    SprinklerType.FLOOR -> 3
                    SprinklerType.CEILING -> 0
                }
            AABB(
                it.x() - size.x / 2.0,
                it.y() - size.y.toDouble() + yOffset,
                it.z() - size.z / 2.0,
                it.x() + size.x / 2.0,
                it.y() + yOffset,
                it.z() + size.z / 2.0,
            )
        }

    override val id = modLoc(suffix)

    override fun <T : Entity> getEntities(
        clazz: Class<T>,
        predicate: (T) -> Boolean,
    ): List<T> = level.getEntities(EntityTypeTest.forClass(clazz), area, predicate)

    private fun execute(
        pos: BlockPos,
        consumer: (BlockPos) -> Unit,
    ) {
        val outside = subLevel?.logicalPose()?.transformPosition(pos.center)?.let(BlockPos::containing) ?: pos

        consumer(outside)

        SableCompanion.INSTANCE.runIncludingSubLevels(level, outside.center as Position, true, subLevel) { _, it ->
            consumer(it)
            null
        }
    }

    override fun forEachBlock(consumer: (BlockPos) -> Unit) {
        for (pos in BlockPos.betweenClosed(
            ceil(area.minX).toInt(),
            ceil(area.minY).toInt(),
            ceil(area.minZ).toInt(),
            floor(area.maxX).toInt(),
            floor(area.maxY).toInt(),
            floor(area.maxZ).toInt(),
        )) {
            execute(pos, consumer)
        }
    }

    override fun forEachGroundBlock(consumer: (BlockPos) -> Unit) {
        val minX = ceil(area.minX).toInt()
        val maxX = floor(area.maxX).toInt()
        val minZ = ceil(area.minZ).toInt()
        val maxZ = floor(area.maxZ).toInt()
        val minY = ceil(area.minY).toInt()
        val maxY = floor(area.maxY).toInt()

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
