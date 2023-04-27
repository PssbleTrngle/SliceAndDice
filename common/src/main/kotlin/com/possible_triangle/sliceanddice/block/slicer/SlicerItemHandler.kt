package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Content
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable

class SlicerItemHandler(private val tile: SlicerTile) : IItemHandlerModifiable {

    override fun getSlots() = 1

    override fun getStackInSlot(slot: Int): ItemStack = when (slot) {
        0 -> tile.heldItem
        else -> ItemStack.EMPTY
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if(!isItemValid(slot, stack)) return stack
        return if (slot == 0 && tile.heldItem.isEmpty) {
            if (!simulate) tile.heldItem = stack
            ItemStack.EMPTY
        } else {
            stack
        }
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        return if (slot == 0) {
            val copy = tile.heldItem.copy()
            if (!simulate) tile.heldItem = ItemStack.EMPTY
            copy
        } else {
            ItemStack.EMPTY
        }
    }

    override fun getSlotLimit(slot: Int) = 1

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        return !stack.isEmpty && stack.`is`(Content.ALLOWED_TOOLS)
    }

    override fun setStackInSlot(slot: Int, stack: ItemStack) {
        if(!isItemValid(slot, stack)) return
        if (slot == 0) tile.heldItem = stack
    }
}