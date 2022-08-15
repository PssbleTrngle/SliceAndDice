package com.possible_triangle.thermomix.block.tile

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*

fun interface SprinkleBehaviour {

    companion object : SprinkleBehaviour {
        private val BEHAVIOURS = arrayListOf<Pair<(FluidStack) -> Boolean, SprinkleBehaviour>>()

        fun register(tag: TagKey<Fluid>, behaviour: SprinkleBehaviour) {
            BEHAVIOURS.add(Pair({ it.fluid.`is`(tag) }, behaviour))
        }

        override fun act(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random) {
            BEHAVIOURS.filter { it.first(fluid) }.forEach {
                it.second.act(pos, world, fluid, random)
            }
        }

    }

    fun act(pos: BlockPos, world: ServerLevel, fluid: FluidStack, random: Random)

}