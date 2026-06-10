package com.possible_triangle.sliceanddice.block.sprinkler.behaviour

import com.mojang.serialization.Codec
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ServerLevel

class StaticSprinklerBehaviour(
    private val sprinkler: SprinklerBlockEntity,
    private val tank: SmartFluidTankBehaviour,
) : BlockEntityBehaviour(sprinkler) {
    companion object {
        val TYPE = BehaviourType<StaticSprinklerBehaviour>()
    }

    private val instance = object : SprinklerBehaviour {
        override var running: Collection<Holder<Sprinkler<*>>> = emptyList()
        override var remainingTicks = 0
        override val contraption = null
        override val type = sprinkler.type
        override val pos get() = this@StaticSprinklerBehaviour.pos.center
        override val cooldown = 40

        override fun notifyUpdate() {
            blockEntity.notifyUpdate()
        }
    }

    val active get() = instance.active

    override fun getType() = TYPE

    override fun tick() {
        val level = blockEntity.level ?: return

        val attachedPos = blockEntity.blockPos.relative(sprinkler.type.input.opposite)
        val attached = level.getBlockState(attachedPos)
        if (attached.isFaceSturdy(level, attachedPos, sprinkler.type.input)) return

        if (instance.remainingTicks > 1) {
            // TODO move to tick?
            instance.remainingTicks--
        } else {
            // TODO do not check every time, still count down somehow
            instance.check(tank.capability, level)
        }

        if (instance.active) {
            if (level is ServerLevel) {
                instance.tickSprinklers(level, tank.primaryTank.renderedFluid)
            } else if (!blockEntity.isVirtual) {
                instance.spawnParticles(
                    tank.primaryTank.renderedFluid,
                    level,
                )
            }
        }
    }

    override fun write(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        super.write(nbt, registries, clientPacket)
        nbt.putInt("ProcessingTicks", instance.remainingTicks)

        if (!clientPacket) {
            val ops = RegistryOps.create(NbtOps.INSTANCE, registries)
            val encodedSprinklers =
                Codec.list(Sprinkler.HOLDER_CODEC).encodeStart(
                    ops,
                    instance.running.toList(),
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
        instance.remainingTicks = nbt.getInt("ProcessingTicks")

        /* disabled because currently these have to start every time the world loads
        if (!clientPacket && nbt.contains("RunningSprinklers")) {
            val tag = nbt.get("RunningSprinklers")
            val ops = RegistryOps.create(NbtOps.INSTANCE, registries)
            val decodedSprinklers = Codec.list(Sprinkler.HOLDER_CODEC).parse(ops, tag)
            decodedSprinklers.ifSuccess {
                running = it
            }
        }
         */
    }
}
