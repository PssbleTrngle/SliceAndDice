package com.possible_triangle.sliceanddice.block.sprinkler

import com.mojang.serialization.Codec
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.api.sprinkler.start
import com.possible_triangle.sliceanddice.api.sprinkler.stop
import com.possible_triangle.sliceanddice.api.sprinkler.tick
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import net.createmod.catnip.math.VecHelper
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.world.level.Level
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class SprinklerBehaviour(
    private val sprinkler: SprinklerBlockEntity,
    private val tank: SmartFluidTankBehaviour,
) : BlockEntityBehaviour(sprinkler) {
    companion object {
        val TYPE = BehaviourType<SprinklerBehaviour>()

        private const val PROGRESS_DURATION = 40
    }

    private var startedAfterLoad = false
    private var running: Collection<Holder<Sprinkler>> = emptyList()

    private var processingTicks = PROGRESS_DURATION

    var active: Boolean = false
        private set

    override fun getType() = TYPE

    private fun recheck(level: Level) {
        val fluid = tank.primaryHandler.fluid
        val matches = Sprinkler.findMatching(level.registryAccess(), fluid)
        val stopped = running.filterNot { matches.contains(it) }
        val started =
            if (startedAfterLoad) {
                matches.filterNot { running.contains(it) }
            } else {
                matches
            }

        stopped.stop(pos, level, fluid, sprinkler.type)
        started.start(pos, level, fluid, sprinkler.type)

        startedAfterLoad = true
        running = matches
        if (stopped.isNotEmpty() || started.isNotEmpty()) {
            blockEntity.notifyUpdate()
        }
    }

    override fun tick() {
        val level = blockEntity.level ?: return

        val attachedPos = pos.relative(sprinkler.type.input.opposite)
        val attached = level.getBlockState(attachedPos)
        if (attached.isFaceSturdy(level, attachedPos, sprinkler.type.input)) return

        if (processingTicks > 0) {
            processingTicks--
        } else {
            if (!level.isClientSide) {
                recheck(level)
            }

            val used = Configs.SERVER.sprinklerUsage.get()
            val fluid = tank.capability.drain(used, IFluidHandler.FluidAction.SIMULATE)
            active = fluid.amount >= used
            if (active) {
                tank.capability.drain(used, IFluidHandler.FluidAction.EXECUTE)
                processingTicks = PROGRESS_DURATION
            }
        }

        if (level.isClientSide && !blockEntity.isVirtual && active) {
            spawnSprinklerParticles(tank.primaryTank.renderedFluid, level, VecHelper.getCenterOf(pos), sprinkler.type, sprinkler.rotationSpeed)
        }

        running.tick(pos, level, tank.primaryTank.renderedFluid, sprinkler.type)
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
