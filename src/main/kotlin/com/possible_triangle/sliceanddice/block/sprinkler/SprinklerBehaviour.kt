package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.serialization.Codec
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleAction.Range
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.fluids.FluidFX
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import net.createmod.catnip.math.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class SprinklerBehaviour(
    be: SmartBlockEntity,
    private val tank: SmartFluidTankBehaviour,
    private val type: SprinklerBlock.Type,
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

    private var startedAfterLoad = false
    private var running: Collection<Holder<Sprinkler>> = emptyList()

    private var processingTicks = -1

    override fun getType() = TYPE

    private fun recheck(level: Level) {
        val matches = SprinkleAction.findMatching(level.registryAccess(), tank.primaryHandler.fluid)
        val stopped = running.filterNot { matches.contains(it) }
        val started =
            if (startedAfterLoad) {
                matches.filterNot { running.contains(it) }
            } else {
                matches
            }

        stopped.actEach(SprinkleAction::stop)
        started.actEach(SprinkleAction::start)

        startedAfterLoad = true
        running = matches
    }

    private fun Collection<Holder<Sprinkler>>.actEach(
        block: SprinkleAction.(range: Range, level: ServerLevel, fluid: FluidStack, random: RandomSource) -> Unit,
    ) {
        val level = blockEntity.level ?: return
        if (level !is ServerLevel) return

        val radius = Configs.SERVER.sprinklerRange.get()
        val fluid = tank.primaryHandler.fluid

        map { it.value() }.forEach {
            val area = Vec3i(radius + it.rangeBonus, 7, radius + it.rangeBonus)
            val range = Range(area, pos, level, type)
            it.action.value().block(range, level, fluid, level.random)
        }
    }

    override fun tick() {
        val level = blockEntity.level ?: return
        val pos = blockEntity.blockPos

        val attachedPos = pos.relative(type.input.opposite)
        val attached = level.getBlockState(attachedPos)
        if (attached.isFaceSturdy(level, attachedPos, type.input)) return

        if (processingTicks >= 0) {
            processingTicks--
        } else {
            if (!level.isClientSide) {
                recheck(level)
            }

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

        running.actEach(SprinkleAction::act)
    }

    override fun write(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        super.write(nbt, registries, clientPacket)
        nbt.putInt("ProcessingTicks", processingTicks)

        if (!clientPacket) {
            val ops = RegistryOps.create(NbtOps.INSTANCE, registries)
            val encodedSprinklers =
                Codec.list(Sprinkler.HOLDER_CODEC).encodeStart(
                    ops,
                    running.toList(),
                )
            encodedSprinklers.ifSuccess {
                nbt.put("RunningSprinklers", it)
            }
        }
    }

    override fun read(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        processingTicks = nbt.getInt("ProcessingTicks")

        if (!clientPacket && nbt.contains("RunningSprinklers")) {
            val tag = nbt.get("RunningSprinklers")
            val ops = RegistryOps.create(NbtOps.INSTANCE, registries)
            val decodedSprinklers = Codec.list(Sprinkler.HOLDER_CODEC).parse(ops, tag)
            decodedSprinklers.ifSuccess {
                running = it
            }
        }
    }
}
