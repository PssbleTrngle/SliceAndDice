package com.possible_triangle.sliceanddice.recipe

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.SliceAndDice
import com.simibubi.create.content.contraptions.processing.BasinRecipe
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder.ProcessingRecipeParams
import com.simibubi.create.foundation.item.SmartInventory
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

data class CuttingProcessingRecipe(val params: ProcessingRecipeParams, val tool: Ingredient? = null) :
    BasinRecipe(CuttingProcessingRecipe, params) {

    companion object : IRecipeTypeInfo {
        override fun getId() = ResourceLocation(SliceAndDice.MOD_ID, "cutting")

        override fun <T : RecipeSerializer<*>?> getSerializer() = Content.CUTTING_SERIALIZER.get() as T

        override fun <T : RecipeType<*>?> getType() = Content.CUTTING_RECIPE_TYPE.get() as T
    }

    override fun matches(inv: SmartInventory, world: Level) = true

}