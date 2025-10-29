package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe
import mezz.jei.api.registration.IRecipeCatalystRegistration
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe
import vectorwing.farmersdelight.integration.jei.FDRecipeTypes
import java.util.function.BiConsumer

private fun CuttingBoardRecipe.toBasin(id: ResourceLocation): CuttingProcessingRecipe {
    val builder = CuttingProcessingRecipe.Builder(id)
    ingredients.forEach { builder.require(it) }
    rollableResults.forEach { builder.output(it.chance, it.stack) }
    builder.tool(tool)
    builder.converted()
    return builder.build()
}

class FarmersDelightCompat private constructor() : IRecipeInjector {

    companion object {
        private val INSTANCE = FarmersDelightCompat()

        fun ifLoaded(runnable: FarmersDelightCompat.() -> Unit) {
            ModCompat.ifLoaded(ModCompat.FARMERS_DELIGHT) {
                runnable(INSTANCE)
            }
        }
    }

    fun addCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addRecipeCatalyst(ItemStack(Content.SLICER_BLOCK.get()), FDRecipeTypes.CUTTING)
    }

    override fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>
    ) {
        basinCookingRecipes(existing, add)
        processingCutting(existing, add)
    }

    private fun shouldConvert(key: ResourceLocation): Boolean {
        return !key.path.endsWith("_manual_only")
    }

    private fun processingCutting(
        recipes: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        val cuttingRecipes = recipes
            .filterKeys { shouldConvert(it) }
            .filterValues { it is CuttingBoardRecipe }
            .mapValues { it.value as CuttingBoardRecipe }

        SliceAndDice.LOGGER.debug("Found {} cutting recipes", cuttingRecipes.size)

        cuttingRecipes.forEach { (originalID, recipe) ->
            val id = SliceAndDice.modLoc("cutting/${originalID.namespace}/${originalID.path}")
            add.accept(id, recipe.toBasin(id))
        }
    }

    private fun basinCookingRecipes(
        recipes: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        if (!Configs.SERVER.BASIN_COOKING.get()) return

        val emptyingRecipes = recipes.values.filterIsInstance<EmptyingRecipe>()
        val cookingRecipes = recipes
            .filterKeys { shouldConvert(it) }
            .filterValues { it is CookingPotRecipe }
            .mapValues { it.value as CookingPotRecipe }

        SliceAndDice.LOGGER.debug("Found {} cooking recipes", cookingRecipes.size)
        val generator = MixingRecipeGenerator(emptyingRecipes)

        return cookingRecipes.forEach { (originalID, recipe) ->
            val id = SliceAndDice.modLoc("cooking/${originalID.namespace}/${originalID.path}")

            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            // Cooking recipes do not use the registryAccess
            val result = recipe.getResultItem(null)

            val initialIngredients = recipe.ingredients.toMutableList()
            @Suppress("SENSELESS_COMPARISON")
            if (recipe.outputContainer != null && !recipe.outputContainer.isEmpty) {
                initialIngredients.add(Ingredient.of(recipe.outputContainer))
            }

            val mixingRecipes = generator.resolveAll(initialIngredients, result, recipe.cookTime, id)
            mixingRecipes.forEachIndexed { i, recipe ->
                add.accept(id.withSuffix("_$i"), recipe)
            }
        }
    }

}