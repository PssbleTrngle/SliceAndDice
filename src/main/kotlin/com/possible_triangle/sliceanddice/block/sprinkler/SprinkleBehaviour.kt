package com.possible_triangle.sliceanddice.block.sprinkler

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.AABB
import net.minecraftforge.fluids.FluidStack
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

private data class RegisteredBehaviour(
    val predicate: (FluidStack) -> Boolean,
    val behaviour: SprinkleBehaviour,
    val range: Vec3i,
)

private fun Vec3i.toSprinklerBox(origin: BlockPos): AABB {
    return AABB(
        origin.x - x / 2.0,
        origin.y - y.toDouble(),
        origin.z - z / 2.0,
        origin.x + x / 2.0,
        origin.y - 1.0,
        origin.z + z / 2.0
    )
}

fun interface SprinkleBehaviour {

    fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random)

    fun act(range: AABB, world: ServerLevel, fluid: FluidStack, random: Random) {
        for (block in BlockPos.betweenClosed(
            ceil(range.minX).toInt(), ceil(range.minY).toInt(), ceil(range.minZ).toInt(),
            floor(range.maxX).toInt(), floor(range.maxY).toInt(), floor(range.maxZ).toInt(),
        )) {
            actAt(block, world, fluid, random)
        }
    }

    companion object : SprinkleBehaviour {
        private val BEHAVIOURS = arrayListOf<RegisteredBehaviour>()

        val DEFAULT_RANGE = Vec3i(5, 7, 5)

        fun register(tag: TagKey<Fluid>, behaviour: SprinkleBehaviour, range: Vec3i) {
            BEHAVIOURS.add(RegisteredBehaviour({ it.fluid.`is`(tag) }, behaviour, range))
        }

        override fun actAt(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random) {
            BEHAVIOURS.filter { it.predicate(fluid) }.forEach {
                it.behaviour.act(it.range.toSprinklerBox(pos), world, fluid, random)
            }

        }

    }
}