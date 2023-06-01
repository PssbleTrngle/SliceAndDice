package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Content
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object SlicerArmInteractionType : ArmInteractionPointType(Content.modLoc("slicer")) {

    override fun canCreatePoint(level: Level, pos: BlockPos, state: BlockState): Boolean {
        return state.block == Content.SLICER_BLOCK.get()
    }

    override fun createPoint(level: Level, pos: BlockPos, state: BlockState): ArmInteractionPoint {
        return ArmInteractionPoint(this, level, pos, state)
    }

}