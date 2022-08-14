package com.possible_triangle.thermomix

import com.possible_triangle.thermomix.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe
import com.simibubi.create.content.contraptions.processing.EmptyingRecipe
import com.simibubi.create.content.contraptions.processing.HeatCondition
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder
import com.simibubi.create.foundation.fluid.FluidIngredient
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe

private fun CuttingBoardRecipe.toBasin(): CuttingProcessingRecipe {
    val basinId = ResourceLocation(ThermomixMod.MOD_ID, "${id.namespace}_${id.path}")
    val builder = ProcessingRecipeBuilder(::CuttingProcessingRecipe, basinId)
    ingredients.forEach { builder.require(it) }
    rollableResults.forEach { builder.output(it.chance, it.stack) }
    return builder.build().copy(tool = tool)
}

object RecipeInjector {

    fun inject(recipes: Map<ResourceLocation, Recipe<*>>): Map<ResourceLocation, Recipe<*>> {
        return recipes + basinCookingRecipes(recipes) + processingCutting(recipes)
    }

    private fun processingCutting(recipes: Map<ResourceLocation, Recipe<*>>): Map<ResourceLocation, CuttingProcessingRecipe> {
        val cuttingRecipes = recipes
            .filterValues { it is CuttingBoardRecipe }
            .mapValues { it.value as CuttingBoardRecipe }

        return cuttingRecipes
            .mapKeys { ResourceLocation(ThermomixMod.MOD_ID, "cutting/${it.key.namespace}/${it.key.path}") }
            .mapValues { it.value.toBasin() }
    }

    private fun basinCookingRecipes(recipes: Map<ResourceLocation, Recipe<*>>): Map<ResourceLocation, MixingRecipe> {
        val emptyingRecipes = recipes.values.filterIsInstance<EmptyingRecipe>()
        val cookingRecipes = recipes
            .filterValues { it is CookingPotRecipe }
            .mapValues { it.value as CookingPotRecipe }

        return cookingRecipes
            .mapKeys { ResourceLocation(ThermomixMod.MOD_ID, "cooking/${it.key.namespace}/${it.key.path}") }
            .mapValues { (id, recipe) ->
                val builder = ProcessingRecipeBuilder(::MixingRecipe, id)
                builder.duration(recipe.cookTime)
                builder.requiresHeat(HeatCondition.HEATED)

                recipe.ingredients.map { Ingredient.fromJson(it.toJson()) }.forEach { ingredient ->
                    val fluids = ingredient.items.mapNotNull { stack ->
                        emptyingRecipes.find {
                            val required = it.ingredients[0]
                            required.test(stack)
                        }?.resultingFluid
                    }

                    val fluid = fluids.minByOrNull { it.amount }

                    if (fluid != null) builder.require(FluidIngredient.fromFluidStack(fluid))
                    else builder.require(ingredient)
                }

                if (recipe.outputContainer != null && !recipe.outputContainer.isEmpty) {
                    builder.require(Ingredient.of(recipe.outputContainer))
                }

                builder.output(recipe.resultItem)
                builder.build()
            }
    }

}