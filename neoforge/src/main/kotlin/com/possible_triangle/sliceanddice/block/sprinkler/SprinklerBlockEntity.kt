package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.block.sprinkler.behaviour.StaticSprinklerBehaviour
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.index.SDBlockEntities
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import kotlin.jvm.optionals.getOrElse

class SprinklerBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : SmartBlockEntity(type, pos, state),
    IHaveGoggleInformation {
    val type
        get() =
            blockState.getOptionalValue(SprinklerBlock.TYPE).getOrElse { SprinklerType.CEILING }

    companion object {
        fun registerCapabilities(event: RegisterCapabilitiesEvent) {
            event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                SDBlockEntities.SPRINKLER.get(),
            ) { it, direction ->
                if (it.type.input != direction) {
                    null
                } else {
                    it.tank.capability
                }
            }
        }
    }

    internal lateinit var tank: SmartFluidTankBehaviour
    internal lateinit var behaviour: StaticSprinklerBehaviour

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        tank =
            SmartFluidTankBehaviour
                .single(this, Configs.SERVER.sprinklerCapacity.get())
                .allowInsertion()
                .whenFluidUpdates(::notifyUpdate)
                .also(behaviours::add)
        behaviour =
            StaticSprinklerBehaviour(this, tank)
                .also(behaviours::add)
    }

    override fun addToGoggleTooltip(
        tooltip: MutableList<Component>,
        sneaking: Boolean,
    ): Boolean = containedFluidTooltip(tooltip, sneaking, tank.capability)
}
