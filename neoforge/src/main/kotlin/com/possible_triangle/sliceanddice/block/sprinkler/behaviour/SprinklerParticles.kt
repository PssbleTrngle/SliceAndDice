package com.possible_triangle.sliceanddice.block.sprinkler.behaviour

import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.simibubi.create.content.fluids.FluidFX
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

internal fun SprinklerBehaviour.spawnParticles(level: Level) {
    val renderer = Minecraft.getInstance().levelRenderer
    val timer = Minecraft.getInstance().timer
    val renderTicks = renderer.ticks + timer.getGameTimeDeltaPartialTick(false)
    val renderSeconds = renderTicks / 20
    val progress = (renderSeconds * rotationSpeed * Math.PI) / 180

    if (renderedFluid.isEmpty) return

    val particle = FluidFX.getFluidParticle(renderedFluid)
    val vec = Vec3(pos.x(), pos.y(), pos.z())

    when (type) {
        SprinklerType.CEILING -> sprinkleDown(particle, level, vec)
        SprinklerType.FLOOR -> sprinkleUp(particle, level, vec, progress.toFloat())
    }
}

internal fun sprinkleDown(
    particle: ParticleOptions,
    level: Level,
    pos: Vec3,
) {
    val x = level.random.nextDouble() * 2 - 1
    val z = level.random.nextDouble() * 2 - 1

    val vec = pos.add(0.0, 2.0 / 16, 0.0).add(x * 0.3, 0.0, z * 0.3)

    level.addParticle(particle, vec.x, vec.y, vec.z, x * 0.2, -0.1, z * 0.2)
}

internal fun sprinkleUp(
    particle: ParticleOptions,
    level: Level,
    pos: Vec3,
    progress: Float,
) {
    sequenceOf(0F, 0.25F, 0.5F, 0.75F).forEach { offset ->
        val radians = progress + (offset * Math.PI * 2)
        val x = sin(radians)
        val z = cos(radians)

        val vec = pos.add(0.0, 2.0 / 16, 0.0).add(x * 0.5, 0.0, z * 0.5)

        val strength = level.random.nextDouble() * 0.05 + 0.15
        level.addParticle(particle, vec.x, vec.y, vec.z, x * strength, 0.2, z * strength)
    }
}
