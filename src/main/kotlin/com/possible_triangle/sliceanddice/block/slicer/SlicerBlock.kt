package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Content
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllItems
import com.simibubi.create.AllShapes
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel
import com.simibubi.create.content.contraptions.base.KineticBlock
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel
import com.simibubi.create.foundation.block.ITE
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.EntityCollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class SlicerBlock(properties: Properties) : KineticBlock(properties), ITE<SlicerTile>, ICogWheel {

    override fun getTileEntityClass() = SlicerTile::class.java

    override fun getTileEntityType() = Content.SLICER_TILE.get()

    override fun use(
        state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        val held = player.getItemInHand(hand).copy()

        if (AllItems.WRENCH.isIn(held)) return InteractionResult.PASS
        if (!held.`is`(Content.ALLOWED_TOOLS) && !held.isEmpty) return InteractionResult.PASS

        if (!world.isClientSide) withTileEntityDo(world, pos) {
            if (it !is SlicerTile) return@withTileEntityDo
            val heldByDeployer = it.heldItem.copy()
            if (heldByDeployer.isEmpty && held.isEmpty) return@withTileEntityDo
            player.setItemInHand(hand, heldByDeployer)
            it.heldItem = held
            it.sendData()
        }

        return InteractionResult.SUCCESS
    }

    override fun canSurvive(state: BlockState, worldIn: LevelReader, pos: BlockPos): Boolean {
        return !AllBlocks.BASIN.has(worldIn.getBlockState(pos.below()))
    }

    override fun getShape(
        state: BlockState,
        worldIn: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        return if (context is EntityCollisionContext
            && context.entity is Player
        ) AllShapes.CASING_14PX[Direction.DOWN] else AllShapes.MECHANICAL_PROCESSOR_SHAPE
    }


    override fun getRotationAxis(state: BlockState) = Direction.Axis.Y

    override fun hasShaftTowards(world: LevelReader, pos: BlockPos, state: BlockState, face: Direction) = false

    override fun getParticleTargetRadius(): Float {
        return 0.85F
    }

    override fun getParticleInitialRadius(): Float {
        return 0.75F
    }

    override fun getMinimumRequiredSpeedLevel(): SpeedLevel? {
        return SpeedLevel.MEDIUM
    }

    override fun isPathfindable(
        state: BlockState,
        reader: BlockGetter,
        pos: BlockPos,
        type: PathComputationType,
    ) = false

    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (state.hasBlockEntity() && state.block !== newState.block) {
            withTileEntityDo(world, pos) { te ->
                if (isMoving) return@withTileEntityDo
                val item = ItemEntity(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), te.heldItem)
                item.setDefaultPickUpDelay()
                world.addFreshEntity(item)
            }
        }

        super.onRemove(state, world, pos, newState, isMoving)
    }

}