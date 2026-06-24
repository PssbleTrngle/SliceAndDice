package com.possible_triangle.sliceanddice.block.slicer

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object SlicerArmInteractionType : ArmInteractionPointType() {
    override fun canCreatePoint(
        level: Level,
        pos: BlockPos,
        state: BlockState,
    ): Boolean = state.block is SlicerBlock

    override fun createPoint(
        level: Level,
        pos: BlockPos,
        state: BlockState,
    ): ArmInteractionPoint = ArmInteractionPoint(this, level, pos, state)
}
