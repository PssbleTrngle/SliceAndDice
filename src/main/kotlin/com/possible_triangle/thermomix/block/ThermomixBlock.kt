package com.possible_triangle.thermomix.block

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.simibubi.create.AllItems
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ThermomixBlock(properties: Properties) : MechanicalMixerBlock(properties) {

    override fun getTileEntityType() = Content.THERMOMIX_TILE.get()

    override fun use(
        state: BlockState, world: Level, pos: BlockPos, player: Player, hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        val held = player.getItemInHand(hand).copy()
        if (AllItems.WRENCH.isIn(held)) return InteractionResult.PASS
        //if (hit.direction != state.getValue(DirectionalKineticBlock.FACING)) return InteractionResult.PASS
        if (world.isClientSide) return InteractionResult.SUCCESS
        withTileEntityDo(world, pos) {
            if(it !is ThermomixTile) return@withTileEntityDo
            val heldByDeployer = it.heldItem.copy()
            if (heldByDeployer.isEmpty && held.isEmpty) return@withTileEntityDo
            player.setItemInHand(hand, heldByDeployer)
            it.heldItem = held
            it.sendData()
        }
        return InteractionResult.SUCCESS
    }

}