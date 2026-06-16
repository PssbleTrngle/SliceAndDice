package com.possible_triangle.sliceanddice.api.sprinkler

import net.minecraft.core.BlockPos
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.neoforged.neoforge.fluids.FluidStack
import java.util.UUID

interface SprinkleContext {
    val pos: Position
    val size: Vec3i
    val level: ServerLevel
    val fluidStack: FluidStack
    val type: SprinklerType
    val contraption: UUID?
    val random: RandomSource
    val blockPos: BlockPos
    val id: ResourceLocation
    val area: AABB

    fun <T : Entity> getEntities(
        clazz: Class<T>,
        predicate: (T) -> Boolean = { true },
    ): List<T>

    fun forEachBlock(consumer: (BlockPos) -> Unit)

    fun forEachGroundBlock(consumer: (BlockPos) -> Unit)
}
