package com.possible_triangle.sliceanddice

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import com.possible_triangle.sliceanddice.compat.ModCompat.injectRecipes
import com.possible_triangle.sliceanddice.mixins.RecipeManagerAccessor
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType

object RecipeInjection {

    fun injectRecipes(manager: RecipeManagerAccessor) {
        val byNameBuilder = ImmutableMap.builder<ResourceLocation, Recipe<*>>()
        byNameBuilder.putAll(manager.byName)

        val recipesBuilder: MutableMap<RecipeType<*>, ImmutableMap.Builder<ResourceLocation, Recipe<*>>> =
            Maps.newHashMap()

        SliceAndDice.LOGGER.debug("Recipes before: {}", manager.byName.size)

        injectRecipes(manager.byName, byNameBuilder::put)

        val newByName = byNameBuilder.build()
        newByName.forEach { (id: ResourceLocation, recipe: Recipe<*>) ->
            val type: RecipeType<*> = recipe.type
            recipesBuilder.computeIfAbsent(type) { ImmutableMap.builder() }.put(id, recipe)
        }

        SliceAndDice.LOGGER.debug("Recipes after: {}", newByName.size)

        manager.byName = newByName
        manager.setRecipes(recipesBuilder.mapValues { it.value.build() })

    }
}