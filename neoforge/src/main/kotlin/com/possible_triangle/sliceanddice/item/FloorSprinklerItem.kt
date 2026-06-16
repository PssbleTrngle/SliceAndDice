package com.possible_triangle.sliceanddice.item

import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.index.SDBlocks
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

class FloorSprinklerItem(
    properties: Properties,
) : BlockItem(SDBlocks.SPRINKLER.get(), properties) {
    override fun registerBlocks(
        blockToItemMap: Map<Block?, Item?>,
        item: Item,
    ) {
        // do nothing
    }

    override fun getPlacementState(context: BlockPlaceContext): BlockState? =
        super.getPlacementState(context)?.setValue(SprinklerBlock.TYPE, SprinklerType.FLOOR)
}
