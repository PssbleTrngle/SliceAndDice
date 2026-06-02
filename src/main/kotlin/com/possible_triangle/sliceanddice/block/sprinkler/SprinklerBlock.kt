package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.index.SDBlockEntities
import com.simibubi.create.content.equipment.wrench.IWrenchable
import com.simibubi.create.foundation.block.IBE
import com.simibubi.create.foundation.blockEntity.ComparatorUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext

class SprinklerBlock private constructor(
    properties: Properties,
    val type: Type,
) : Block(properties),
    IWrenchable,
    IBE<SprinklerBlockEntity> {
    companion object {
        private val CEILING_SHAPE = box(2.0, 10.0, 2.0, 14.0, 16.0, 14.0)
        private val FLOOR_SHAPE = box(3.0, 0.0, 3.0, 12.0, 13.0, 12.0)

        fun ceiling(properties: Properties) = SprinklerBlock(properties, Type.CEILING)

        fun floor(properties: Properties) = SprinklerBlock(properties, Type.FLOOR)
    }

    private val shape =
        when (type) {
            Type.CEILING -> CEILING_SHAPE
            Type.FLOOR -> FLOOR_SHAPE
        }

    override fun getBlockEntityClass() = SprinklerBlockEntity::class.java

    override fun getBlockEntityType() = SDBlockEntities.SPRINKLER.get()

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ) = shape

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(
        state: BlockState,
        world: Level,
        pos: BlockPos,
    ): Int = ComparatorUtil.levelOfSmartFluidTank(world, pos)

    enum class Type(
        val input: Direction,
    ) {
        CEILING(Direction.UP),
        FLOOR(Direction.DOWN),
    }
}
