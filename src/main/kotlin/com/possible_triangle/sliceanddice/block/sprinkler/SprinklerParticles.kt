package com.possible_triangle.sliceanddice.block.sprinkler

import com.simibubi.create.content.fluids.FluidFX
import net.createmod.catnip.math.VecHelper
import net.minecraft.client.Minecraft
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
    speed: Float,
) {
    val renderer = Minecraft.getInstance().levelRenderer
    val timer = Minecraft.getInstance().timer
    val renderTicks = renderer.ticks + timer.getGameTimeDeltaPartialTick(false)
    val renderSeconds = renderTicks / 20
    val progress = (renderSeconds * speed * Math.PI) / 180

    if (fluid.isEmpty) return

    val particle = FluidFX.getFluidParticle(fluid)

    when (type) {
        SprinklerBlock.Type.CEILING -> sprinkleDown(particle, level, pos)
        SprinklerBlock.Type.FLOOR -> sprinkleUp(particle, level, pos, progress.toFloat())
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
    sequenceOf(0F, 0.25F, 0.5F, 0.75F).forEach { offset ->
        val radians = progress + (offset * Math.PI * 2)
        val x = sin(radians)
        val z = cos(radians)

        val vec = VecHelper.getCenterOf(pos).add(0.0, 2.0 / 16, 0.0).add(x * 0.5, 0.0, z * 0.5)

        val strength = level.random.nextDouble() * 0.05 + 0.15
        level.addParticle(particle, vec.x, vec.y, vec.z, x * strength, 0.2, z * strength)
    }
}
