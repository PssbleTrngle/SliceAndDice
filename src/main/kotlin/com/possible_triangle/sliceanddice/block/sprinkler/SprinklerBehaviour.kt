package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour.Range
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.fluids.FluidFX
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import net.createmod.catnip.math.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class SprinklerBehaviour(
    be: SmartBlockEntity,
    private val tank: SmartFluidTankBehaviour,
) : BlockEntityBehaviour(be) {
    companion object {
        val TYPE = BehaviourType<SprinklerBehaviour>()

        private fun spawnProcessingParticles(
            fluid: FluidStack,
            level: Level,
            pos: BlockPos,
        ) {
            if (fluid.isEmpty) return

            val particle = FluidFX.getFluidParticle(fluid)
            val x = level.random.nextDouble() * 2 - 1
            val z = level.random.nextDouble() * 2 - 1

            val vec = VecHelper.getCenterOf(pos).add(0.0, 2.0 / 16, 0.0).add(x * 0.3, 0.0, z * 0.3)

            level.addParticle(particle, vec.x, vec.y, vec.z, x * 0.2, -0.1, z * 0.2)
        }
    }

    private var running: Collection<RegisteredBehaviour> = emptyList()
    private var processingTicks = -1

    override fun getType() = TYPE

    private fun recheck() {
        val matches = SprinkleBehaviour.findMatching(tank.primaryHandler.fluid)
        val stopped = running.filterNot { matches.contains(it) }
        val started = matches.filterNot { running.contains(it) }

        stopped.actEach(SprinkleBehaviour::stop)
        started.actEach(SprinkleBehaviour::start)

        running = matches
    }

    private fun Collection<RegisteredBehaviour>.actEach(
        block: SprinkleBehaviour.(range: Range, level: ServerLevel, fluid: FluidStack, random: RandomSource) -> Unit,
    ) {
        val level = blockEntity.level ?: return
        if (level !is ServerLevel) return

        val radius = Configs.SERVER.sprinklerRange.get()
        val fluid = tank.primaryHandler.fluid

        forEach {
            val area = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
            val range = Range(area, pos, level)
            it.behaviour.block(range, level, fluid, level.random)
        }
    }

    override fun tick() {
        val level = blockEntity.level ?: return
        val pos = blockEntity.blockPos

        val below = level.getBlockState(pos.below())
        if (below.isFaceSturdy(level, pos.below(), Direction.UP)) return

        if (processingTicks >= 0) {
            processingTicks--
        } else {
            recheck()

            val used = Configs.SERVER.sprinklerUsage.get()
            val fluid = tank.capability.drain(used, IFluidHandler.FluidAction.SIMULATE)
            if (fluid.amount >= used) {
                tank.capability.drain(used, IFluidHandler.FluidAction.EXECUTE)
                processingTicks = 20
            }
        }

        if (level.isClientSide && !blockEntity.isVirtual) {
            spawnProcessingParticles(tank.primaryTank.renderedFluid, level, pos)
        }

        running.actEach(SprinkleBehaviour::act)
    }

    override fun write(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        super.write(nbt, registries, clientPacket)
        nbt.putInt("ProcessingTicks", processingTicks)
    }

    override fun read(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        processingTicks = nbt.getInt("ProcessingTicks")
    }
}
