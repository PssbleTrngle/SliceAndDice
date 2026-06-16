package com.possible_triangle.sliceanddice.block.sprinkler

import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.index.SDBlockEntities
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.possible_triangle.sliceanddice.index.SDItems
import com.simibubi.create.content.equipment.wrench.IWrenchable
import com.simibubi.create.foundation.block.IBE
import com.simibubi.create.foundation.blockEntity.ComparatorUtil
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes

class SprinklerBlock(
    properties: Properties,
) : Block(properties),
    IWrenchable,
    IBE<SprinklerBlockEntity> {
    companion object {
        val TYPE = EnumProperty.create("type", SprinklerType::class.java)

        private val CEILING_SHAPE = box(2.0, 10.0, 2.0, 14.0, 16.0, 14.0)
        private val FLOOR_SHAPE =
            Shapes.join(
                box(3.0, 0.0, 3.0, 13.0, 13.0, 13.0),
                box(1.0, 8.0, 1.0, 15.0, 13.0, 15.0),
                BooleanOp.OR,
            )
    }

    private val SprinklerType.shape
        get() =
            when (this) {
                SprinklerType.CEILING -> CEILING_SHAPE
                SprinklerType.FLOOR -> FLOOR_SHAPE
            }

    override fun getBlockEntityClass() = SprinklerBlockEntity::class.java

    override fun getBlockEntityType() = SDBlockEntities.SPRINKLER.get()

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(TYPE)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ) = state.getValue(TYPE).shape

    override fun hasAnalogOutputSignal(state: BlockState) = true

    override fun getAnalogOutputSignal(
        state: BlockState,
        world: Level,
        pos: BlockPos,
    ): Int = ComparatorUtil.levelOfSmartFluidTank(world, pos)

    override fun onWrenched(
        state: BlockState,
        context: UseOnContext,
    ): InteractionResult {
        if (!context.level.isClientSide) {
            context.level.setBlockAndUpdate(context.clickedPos, state.cycle(TYPE))
        }
        return InteractionResult.SUCCESS
    }

    override fun getCloneItemStack(
        state: BlockState,
        target: HitResult,
        level: LevelReader,
        pos: BlockPos,
        player: Player,
    ): ItemStack {
        val block =
            when (state.getValue(TYPE)) {
                SprinklerType.CEILING -> SDBlocks.SPRINKLER.get()
                SprinklerType.FLOOR -> SDItems.FLOOR_SPRINKLER.get()
            }

        return ItemStack(block)
    }
}
