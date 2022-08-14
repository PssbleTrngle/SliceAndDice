package com.possible_triangle.thermomix

import com.simibubi.create.content.contraptions.processing.BasinRecipe
import com.simibubi.create.content.contraptions.processing.HeatCondition
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe

object RecipeInjector {

    fun inject(recipes: Map<ResourceLocation, Recipe<*>>): Map<ResourceLocation, Recipe<*>> {
        val cookingRecipes = recipes
            .filterValues { it is CookingPotRecipe }
            .mapValues { it.value as CookingPotRecipe }

        val basinCookingRecipes = cookingRecipes
            .mapKeys { ResourceLocation(ThermomixMod.MOD_ID, "${it.key.namespace}_${it.key.path}") }
            .mapValues { (id, recipe) ->
                val builder = ProcessingRecipeBuilder(::BasinRecipe, id)
                builder.duration(recipe.cookTime)
                builder.requiresHeat(HeatCondition.HEATED)
                recipe.ingredients.forEach { builder.require(it) }
                builder.output(recipe.resultItem)
                builder.build()
            }

        return recipes + basinCookingRecipes
    }

}