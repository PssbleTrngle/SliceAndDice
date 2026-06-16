package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.index.SDBlockEntities
import com.possible_triangle.sliceanddice.index.SDTags
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllItems
import com.simibubi.create.AllShapes
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel
import com.simibubi.create.content.kinetics.base.KineticBlock
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel
import com.simibubi.create.foundation.block.IBE
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class SlicerBlock(
    properties: Properties,
) : KineticBlock(properties),
    IBE<SlicerBlockEntity>,
    ICogWheel {
    override fun getBlockEntityClass() = SlicerBlockEntity::class.java

    override fun getBlockEntityType() = SDBlockEntities.SLICER.get()

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): ItemInteractionResult {
        val held = player.getItemInHand(hand).copy()

        if (held.isEmpty) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        if (AllItems.WRENCH.isIn(held)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
        if (!held.`is`(SDTags.ALLOWED_TOOLS)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION

        if (!level.isClientSide) {
            withBlockEntityDo(level, pos) {
                val heldByDeployer = it.heldItem.copy()
                player.setItemInHand(hand, heldByDeployer)
                it.heldItem = held
            }
        }

        return ItemInteractionResult.SUCCESS
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult,
    ): InteractionResult {
        if (!level.isClientSide) {
            withBlockEntityDo(level, pos) {
                val heldByDeployer = it.heldItem.copy()
                player.setItemInHand(player.usedItemHand, heldByDeployer)
                it.heldItem = ItemStack.EMPTY
            }
        }

        return InteractionResult.SUCCESS
    }

    override fun canSurvive(
        state: BlockState,
        worldIn: LevelReader,
        pos: BlockPos,
    ): Boolean = !AllBlocks.BASIN.has(worldIn.getBlockState(pos.below()))

    override fun getShape(
        state: BlockState,
        worldIn: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape =
        if (context is EntityCollisionContext &&
            context.entity is Player
        ) {
            AllShapes.CASING_14PX[Direction.DOWN]
        } else {
            AllShapes.MECHANICAL_PROCESSOR_SHAPE
        }

    override fun getRotationAxis(state: BlockState) = Direction.Axis.Y

    override fun hasShaftTowards(
        world: LevelReader,
        pos: BlockPos,
        state: BlockState,
        face: Direction,
    ) = false

    override fun getParticleTargetRadius(): Float = 0.85F

    override fun getParticleInitialRadius(): Float = 0.75F

    override fun getMinimumRequiredSpeedLevel(): SpeedLevel = SpeedLevel.MEDIUM

    override fun isPathfindable(
        state: BlockState,
        type: PathComputationType,
    ) = false

    override fun onRemove(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        newState: BlockState,
        isMoving: Boolean,
    ) {
        if (state.hasBlockEntity() && state.block !== newState.block) {
            withBlockEntityDo(world, pos) { te ->
                if (isMoving) return@withBlockEntityDo
                val item = ItemEntity(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), te.heldItem)
                item.setDefaultPickUpDelay()
                world.addFreshEntity(item)
            }
        }

        super.onRemove(state, world, pos, newState, isMoving)
    }
}
