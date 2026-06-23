package com.possible_triangle.sliceanddice

import com.possible_triangle.sliceanddice.block.slicer.SlicerBlock
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlockEntity
import com.possible_triangle.sliceanddice.index.SDBlockEntities
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.tterrag.registrate.util.entry.BlockEntityEntry
import com.tterrag.registrate.util.entry.BlockEntry

/**
 * Binary compatibility for integrations compiled against older Slice & Dice versions.
 */
@Deprecated("Use SDBlocks and SDBlockEntities instead")
@Suppress("unused")
object Content {
    val SLICER_BLOCK: BlockEntry<SlicerBlock>
        get() = SDBlocks.SLICER

    val SLICER_BLOCK_ENTITY: BlockEntityEntry<SlicerBlockEntity>
        get() = SDBlockEntities.SLICER
}
