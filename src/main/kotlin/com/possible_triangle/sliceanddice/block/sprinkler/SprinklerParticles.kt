package com.possible_triangle.sliceanddice.block.sprinkler

import com.simibubi.create.content.fluids.FluidFX
import net.createmod.catnip.math.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import kotlin.math.cos
import kotlin.math.sin

internal fun spawnSprinklerParticles(
    fluid: FluidStack,
    level: Level,
    pos: BlockPos,
    type: SprinklerBlock.Type,
    progress: Float,
) {
    if (fluid.isEmpty) return

    val particle = FluidFX.getFluidParticle(fluid)

    when (type) {
        SprinklerBlock.Type.CEILING -> sprinkleDown(particle, level, pos)
        SprinklerBlock.Type.FLOOR -> sprinkleUp(particle, level, pos, progress)
    }
}

private fun sprinkleDown(
    particle: ParticleOptions,
    level: Level,
    pos: BlockPos,
) {
    val x = level.random.nextDouble() * 2 - 1
    val z = level.random.nextDouble() * 2 - 1

    val vec = VecHelper.getCenterOf(pos).add(0.0, 2.0 / 16, 0.0).add(x * 0.3, 0.0, z * 0.3)

    level.addParticle(particle, vec.x, vec.y, vec.z, x * 0.2, -0.1, z * 0.2)
}

private fun sprinkleUp(
    particle: ParticleOptions,
    level: Level,
    pos: BlockPos,
    progress: Float,
) {
    val radians = progress * Math.PI * 2
    val x = sin(radians)
    val z = cos(radians)

    val vec = VecHelper.getCenterOf(pos).add(0.0, 2.0 / 16, 0.0).add(x * 0.5, 0.0, z * 0.5)

    level.addParticle(particle, vec.x, vec.y, vec.z, x * 0.15, 0.2, z * 0.15)
}
