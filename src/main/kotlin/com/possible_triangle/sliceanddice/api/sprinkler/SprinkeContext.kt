package com.possible_triangle.sliceanddice.api.sprinkler

import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.modLoc
import dev.ryanhcode.sable.companion.SableCompanion
import net.createmod.catnip.outliner.Outliner
import net.minecraft.core.BlockPos
import net.minecraft.core.Position
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

data class SprinkeContext(
    val origin: BlockPos,
    val size: Vec3i,
    val level: ServerLevel,
    val fluidStack: FluidStack,
    val type: SprinklerBlock.Type,
    // val isContraption: Boolean,
) {
    private val subLevel = SableCompanion.INSTANCE.getContaining(level, origin)
    val random: RandomSource = level.random

    val area =
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

    val id
        get() =
            modLoc("sprinkler_${origin.x}_${origin.y}_${origin.z}")

    fun <T : Entity> getEntities(
        clazz: Class<T>,
        predicate: (T) -> Boolean = {
            true
        },
    ): List<T> = level.getEntities(EntityTypeTest.forClass(clazz), area, predicate)

    private fun execute(
        pos: BlockPos,
        consumer: (BlockPos) -> Unit,
    ) {
        val outside = subLevel?.logicalPose()?.transformPosition(pos.center)?.let(BlockPos::containing) ?: pos

        consumer(outside)
        Outliner
            .getInstance()
            .chaseAABB(this, area)

        SableCompanion.INSTANCE.runIncludingSubLevels(level, outside.center as Position, true, subLevel) { _, it ->
            consumer(it)
            null
        }
    }

    fun forEachBlock(consumer: (BlockPos) -> Unit) {
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

    fun forEachGroundBlock(consumer: (BlockPos) -> Unit) {
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
