package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation
import com.simibubi.create.content.fluids.FluidFX
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import com.simibubi.create.foundation.utility.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class SprinklerTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : SmartBlockEntity(type, pos, state),
    IHaveGoggleInformation {

    private lateinit var tank: SmartFluidTankBehaviour
    private var processingTicks = -1

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        behaviours.add(SmartFluidTankBehaviour.single(this, Configs.SERVER.SPRINKLER_CAPACITY.get()).allowInsertion()
            .also { tank = it })
    }

    override fun tick() {
        val world = level ?: return

        val below = world.getBlockState(blockPos.below())
        if (below.isFaceSturdy(world, blockPos.below(), Direction.UP)) return

        if (processingTicks >= 0) {
            processingTicks--
        } else tank.capability.ifPresent {
            val used = Configs.SERVER.SPRINKLER_USAGE.get()
            val fluid = it.drain(used, IFluidHandler.FluidAction.SIMULATE)
            if (fluid.amount >= used) {
                it.drain(used, IFluidHandler.FluidAction.EXECUTE)
                processingTicks = 20 * 10
                notifyUpdate()
            }
        }

        if (processingTicks >= 8) {
            if (world.isClientSide && !isVirtual) spawnProcessingParticles(tank.primaryTank.renderedFluid)
            if (world is ServerLevel) SprinkleBehaviour.actAt(
                blockPos, world, tank.primaryHandler.fluid, world.random,
            )
        }
    }

    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return if (cap === ForgeCapabilities.FLUID_HANDLER && side === Direction.UP) tank.capability.cast()
        else super.getCapability(cap, side)
    }

    private fun spawnProcessingParticles(fluid: FluidStack) {
        val world = level ?: return

        val particle = FluidFX.getFluidParticle(fluid)
        val x = world.random.nextDouble() * 2 - 1
        val z = world.random.nextDouble() * 2 - 1

        val vec = VecHelper.getCenterOf(blockPos).add(0.0, 2.0 / 16, 0.0).add(x * 0.3, 0.0, z * 0.3)

        world.addParticle(particle, vec.x, vec.y, vec.z, x * 0.2, -0.1, z * 0.2)
    }

    override fun writeSafe(tag: CompoundTag) {
        super.writeSafe(tag)
        tag.putInt("ProcessingTicks", processingTicks)
    }

    override fun read(tag: CompoundTag, clientPacket: Boolean) {
        super.read(tag, clientPacket)
        processingTicks = tag.getInt("ProcessingTicks")
    }

    override fun write(compound: CompoundTag, client: Boolean) {
        super.write(compound, client)
        compound.putInt("ProcessingTicks", processingTicks)
    }

    override fun addToGoggleTooltip(tooltip: MutableList<Component>, sneaking: Boolean): Boolean {
        return containedFluidTooltip(
            tooltip, sneaking, getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP)
        )
    }

}