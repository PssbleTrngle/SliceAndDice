package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation
import com.simibubi.create.content.fluids.FluidFX
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import net.createmod.catnip.math.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class SprinklerBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    SmartBlockEntity(type, pos, state),
    IHaveGoggleInformation {

    companion object {
        fun registerCapabilities(event: RegisterCapabilitiesEvent) {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Content.SPRINKLER_BLOCK_ENTITY.get(), { it, _ ->
                it.tank.capability
            })
        }
    }

    private lateinit var tank: SmartFluidTankBehaviour
    private var processingTicks = -1

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        behaviours.add(
            SmartFluidTankBehaviour
                .single(this, Configs.SERVER.SPRINKLER_CAPACITY.get())
                .allowInsertion()
                .whenFluidUpdates(::notifyUpdate)
                .also { tank = it }
        )
    }

    override fun tick() {
        super.tick()

        val world = level ?: return

        val below = world.getBlockState(blockPos.below())
        if (below.isFaceSturdy(world, blockPos.below(), Direction.UP)) return

        if (processingTicks >= 0) {
            processingTicks--
        } else run {
            val used = Configs.SERVER.SPRINKLER_USAGE.get()
            val fluid = tank.capability.drain(used, IFluidHandler.FluidAction.SIMULATE)
            if (fluid.amount >= used) {
                tank.capability.drain(used, IFluidHandler.FluidAction.EXECUTE)
                processingTicks = 20
            }
        }

        if (processingTicks >= 8) {
            if (world.isClientSide && !isVirtual) spawnProcessingParticles(tank.primaryTank.renderedFluid)
            if (world is ServerLevel) SprinkleBehaviour.actAt(
                blockPos, world, tank.primaryHandler.fluid, world.random,
            )
        }
    }

    private fun spawnProcessingParticles(fluid: FluidStack) {
        if (fluid.isEmpty) return
        val world = level ?: return

        val particle = FluidFX.getFluidParticle(fluid)
        val x = world.random.nextDouble() * 2 - 1
        val z = world.random.nextDouble() * 2 - 1

        val vec = VecHelper.getCenterOf(blockPos).add(0.0, 2.0 / 16, 0.0).add(x * 0.3, 0.0, z * 0.3)

        world.addParticle(particle, vec.x, vec.y, vec.z, x * 0.2, -0.1, z * 0.2)
    }

    override fun writeSafe(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.writeSafe(tag, registries)
        tag.putInt("ProcessingTicks", processingTicks)
    }

    override fun read(tag: CompoundTag, registries: HolderLookup.Provider, clientPacket: Boolean) {
        super.read(tag, registries, clientPacket)
        processingTicks = tag.getInt("ProcessingTicks")
    }

    override fun write(compound: CompoundTag, registries: HolderLookup.Provider, client: Boolean) {
        super.write(compound, registries, client)
        compound.putInt("ProcessingTicks", processingTicks)
    }

    override fun addToGoggleTooltip(tooltip: MutableList<Component>, sneaking: Boolean): Boolean {
        return containedFluidTooltip(tooltip, sneaking, tank.capability)
    }

}