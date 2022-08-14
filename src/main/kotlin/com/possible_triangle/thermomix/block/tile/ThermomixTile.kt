package com.possible_triangle.thermomix.block.tile

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.ThermomixMod
import com.possible_triangle.thermomix.config.Configs
import com.simibubi.create.content.contraptions.components.mixer.MechanicalMixerTileEntity
import com.simibubi.create.content.contraptions.processing.BasinRecipe
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder
import com.simibubi.create.foundation.utility.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe

private fun CuttingBoardRecipe.toBasin(): BasinRecipe {
    val basinId = ResourceLocation(ThermomixMod.MOD_ID, "${id.namespace}_${id.path}")
    val builder = ProcessingRecipeBuilder(::BasinRecipe, basinId)
    ingredients.forEach { builder.require(it) }
    rollableResults.forEach { builder.output(it.chance, it.stack) }
    return builder.build()
}

class ThermomixTile(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    MechanicalMixerTileEntity(type, pos, state) {

    var heldItem = ItemStack.EMPTY

    override fun getMatchingRecipes(): MutableList<Recipe<*>> {
        val recipes = super.getMatchingRecipes()
        return recipes.mapNotNull {
            if (it is CuttingBoardRecipe) it.takeIf {
                it.tool.test(heldItem)
            }?.toBasin()
            else it
        }.toMutableList()
    }

    override fun applyBasinRecipe() {
        super.applyBasinRecipe()
        val world = level ?: return
        if (world is ServerLevel && Configs.SERVER.CONSUME_DURABILTY.get()) {
            if (heldItem.hurt(1, level!!.random, null)) {
                heldItem = ItemStack.EMPTY
                sendData()
            }
        }
    }

    override fun <C : Container> matchStaticFilters(recipe: Recipe<C>): Boolean {
        if (recipe !is CuttingBoardRecipe) return false
        return recipe.tool.items.any { it.`is`(Content.ALLOWED_TOOLS) }
    }

    override fun read(compound: CompoundTag, clientPacket: Boolean) {
        super.read(compound, clientPacket)
        heldItem = compound.get("HeldItem").let {
            val decoded = ItemStack.CODEC.parse(NbtOps.INSTANCE, it).result()
            decoded.orElse(ItemStack.EMPTY)
        }
    }

    override fun write(compound: CompoundTag, clientPacket: Boolean) {
        super.write(compound, clientPacket)
        if (!heldItem.isEmpty) {
            val encoded = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, heldItem).result()
            encoded.ifPresent { compound.put("HeldItem", it) }
        }
    }

    override fun renderParticles() {
        super.renderParticles()
        val world = level ?: return

        val center: Vec3 = VecHelper.getCenterOf(worldPosition.below())
        world.addParticle(
            ParticleTypes.SWEEP_ATTACK,
            center.x, center.y, center.z,
            0.0, 0.0, 0.0
        )
    }

}