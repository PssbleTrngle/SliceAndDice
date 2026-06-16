package com.possible_triangle.sliceanddice

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableMultimap
import com.possible_triangle.sliceanddice.compat.ModCompat.injectRecipes
import com.possible_triangle.sliceanddice.mixins.RecipeManagerAccessor
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType

object RecipeInjection {
    fun injectRecipes(manager: RecipeManagerAccessor) {
        val byNameBuilder = ImmutableMap.builder<ResourceLocation, RecipeHolder<*>>()
        byNameBuilder.putAll(manager.byName)

        val byTypeBuilder = ImmutableMultimap.Builder<RecipeType<*>, RecipeHolder<*>>()
        byTypeBuilder.putAll(manager.byType)

        LOGGER.debug("Recipes before: {}", manager.byName.size)

        injectRecipes(manager.byName.mapValues { it.value.value }) { id, recipe ->
            val holder = RecipeHolder(id, recipe)
            byNameBuilder.put(id, holder)
            byTypeBuilder.put(recipe.type, holder)
        }

        manager.byName = byNameBuilder.build()
        manager.byType = byTypeBuilder.build()

        LOGGER.debug("Recipes after: {}", manager.byName.size)
    }
}
