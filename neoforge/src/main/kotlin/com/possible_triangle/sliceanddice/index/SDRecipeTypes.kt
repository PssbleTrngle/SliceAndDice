package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.tterrag.registrate.AbstractRegistrate
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation.fromNamespaceAndPath
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType

object SDRecipeTypes {
    @JvmField
    val CUTTING_RECIPE_TYPE =
        REGISTRATE
            .recipeType<CuttingProcessingRecipe>("cutting")
            .register()

    @JvmField
    val CUTTING_SERIALIZER =
        REGISTRATE
            .`object`("cutting")
            .generic(Registries.RECIPE_SERIALIZER) { CuttingProcessingRecipe.Serializer }
            .register()

    private fun <T : Recipe<*>> AbstractRegistrate<*>.recipeType(name: String) =
        generic(name, Registries.RECIPE_TYPE) {
            object : RecipeType<T> {
                override fun toString() = fromNamespaceAndPath(modid, name).toString()
            }
        }
}
