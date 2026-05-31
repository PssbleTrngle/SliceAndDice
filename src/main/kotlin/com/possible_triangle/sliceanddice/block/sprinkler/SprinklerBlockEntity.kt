package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.config.Configs
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

class SprinklerBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : SmartBlockEntity(type, pos, state),
    IHaveGoggleInformation {
    companion object {
        fun registerCapabilities(event: RegisterCapabilitiesEvent) {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Content.SPRINKLER_BLOCK_ENTITY.get(), { it, _ ->
                it.tank.capability
            })
        }
    }

    private lateinit var tank: SmartFluidTankBehaviour

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        tank =
            SmartFluidTankBehaviour
                .single(this, Configs.SERVER.sprinklerCapacity.get())
                .allowInsertion()
                .whenFluidUpdates(::notifyUpdate)
        behaviours.add(tank)
        behaviours.add(SprinklerBehaviour(this, tank))
    }

    override fun addToGoggleTooltip(
        tooltip: MutableList<Component>,
        sneaking: Boolean,
    ): Boolean = containedFluidTooltip(tooltip, sneaking, tank.capability)
}
