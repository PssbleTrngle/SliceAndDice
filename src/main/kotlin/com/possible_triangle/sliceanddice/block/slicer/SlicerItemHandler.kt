package com.possible_triangle.sliceanddice.block.slicer

import com.possible_triangle.sliceanddice.Content
import com.simibubi.create.foundation.item.ItemHelper
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.world.item.ItemStack

class SlicerItemHandler(private val tile: SlicerBlockEntity) : SingleVariantStorage<ItemVariant>() {

    init {
        update()
    }

    fun update() {
        variant = ItemVariant.of(tile.heldItem)
        amount = tile.heldItem.count.toLong()
    }

    override fun insert(insertedVariant: ItemVariant, maxAmount: Long, transaction: TransactionContext): Long {
        if (!isItemValid(insertedVariant.toStack())) return 0L
        return super.insert(insertedVariant, maxAmount, transaction)
    }

    private fun isItemValid(stack: ItemStack): Boolean {
        return !stack.isEmpty && stack.`is`(Content.ALLOWED_TOOLS)
    }

    override fun onFinalCommit() {
        tile.heldItem = variant.toStack(ItemHelper.truncateLong(amount))
    }

    override fun getCapacity(variant: ItemVariant) = 1L

    override fun getBlankVariant() = ItemVariant.blank()

}