package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.Content
import com.simibubi.create.content.equipment.wrench.IWrenchable
import com.simibubi.create.foundation.block.IBE
import com.simibubi.create.foundation.blockEntity.ComparatorUtil
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class SprinklerBlock(properties: Properties) : Block(properties), IWrenchable, IBE<SprinklerTile> {

    override fun getBlockEntityClass() = SprinklerTile::class.java

    override fun getBlockEntityType() = Content.SPRINKLER_TILE.get()

    companion object {
        val SHAPE: VoxelShape = box(2.0, 10.0, 2.0, 14.0, 16.0, 14.0)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ) = SHAPE

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(state: BlockState, world: Level, pos: BlockPos): Int {
        return ComparatorUtil.levelOfSmartFluidTank(world, pos)
    }

}