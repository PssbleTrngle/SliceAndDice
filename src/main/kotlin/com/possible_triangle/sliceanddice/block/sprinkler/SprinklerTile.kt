package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.FabricConstants.FLUID_MULTIPLIER
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation
import com.simibubi.create.content.fluids.FluidFX
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil
import net.createmod.catnip.math.VecHelper
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class SprinklerTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : SmartBlockEntity(type, pos, state),
    IHaveGoggleInformation, SidedStorageBlockEntity {

    private lateinit var tank: SmartFluidTankBehaviour
    private var processingTicks = -1

    companion object {
    }

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        behaviours.add(
            SmartFluidTankBehaviour
                .single(this, Configs.SERVER.SPRINKLER_CAPACITY.get() * FLUID_MULTIPLIER)
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
        } else {
            val fluid = FluidVariant.of(tank.primaryHandler.fluid.fluid)
            if (!fluid.isBlank) TransferUtil.getTransaction().use { ctx ->
                val used = Configs.SERVER.SPRINKLER_USAGE.get() * FLUID_MULTIPLIER
                val amountExtracted = tank.capability.simulateExtract(fluid, used, ctx)
                if (amountExtracted >= used) {
                    tank.capability.extract(fluid, amountExtracted, ctx)
                    processingTicks = 20
                    notifyUpdate()
                }
            }
        }

        if (processingTicks >= 8) {
            if (world.isClientSide && !isVirtual) spawnProcessingParticles(tank.primaryTank.renderedFluid)
            if (world is ServerLevel) SprinkleBehaviour.actAt(
                blockPos, world, tank.primaryHandler.fluid, world.random,
            )
        }
    }

    override fun getFluidStorage(face: Direction?): Storage<FluidVariant>? {
        return if (face != Direction.DOWN) tank.capability
        else null
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
            tooltip, sneaking, getFluidStorage(Direction.UP)
        )
    }

}