package com.possible_triangle.thermomix.compat

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.ThermomixMod
import com.possible_triangle.thermomix.config.Configs
import com.possible_triangle.thermomix.recipe.CuttingProcessingRecipe
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe
import com.simibubi.create.content.contraptions.processing.EmptyingRecipe
import com.simibubi.create.content.contraptions.processing.HeatCondition
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeBuilder
import com.simibubi.create.foundation.fluid.FluidIngredient
import mezz.jei.api.registration.IRecipeCatalystRegistration
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraftforge.fluids.FluidStack
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe
import vectorwing.farmersdelight.integration.jei.FDRecipeTypes
import java.util.function.BiConsumer

private fun CuttingBoardRecipe.toBasin(): CuttingProcessingRecipe {
    val basinId = ResourceLocation(ThermomixMod.MOD_ID, "${id.namespace}_${id.path}")
    val builder = ProcessingRecipeBuilder(::CuttingProcessingRecipe, basinId)
    ingredients.forEach { builder.require(it) }
    rollableResults.forEach { builder.output(it.chance, it.stack) }
    return builder.build().copy(tool = tool)
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
        registration.addRecipeCatalyst(ItemStack(Content.THERMOMIX_BLOCK.get()), FDRecipeTypes.COOKING)
    }

    override fun injectRecipes(existing: Map<ResourceLocation, Recipe<*>>, add: BiConsumer<ResourceLocation, Recipe<*>>) {
        basinCookingRecipes(existing, add)
        processingCutting(existing, add)
    }

    private fun processingCutting(
        recipes: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        val cuttingRecipes = recipes
            .filterValues { it is CuttingBoardRecipe }
            .mapValues { it.value as CuttingBoardRecipe }

        cuttingRecipes.forEach { (originalID, recipe) ->
            val id = ResourceLocation(ThermomixMod.MOD_ID, "cutting/${originalID.namespace}/${originalID.path}")
            add.accept(id, recipe.toBasin())
        }
    }

    private fun basinCookingRecipes(
        recipes: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        if (!Configs.SERVER.BASIN_COOKING.get()) return

        val emptyingRecipes = recipes.values.filterIsInstance<EmptyingRecipe>()
        val cookingRecipes = recipes
            .filterValues { it is CookingPotRecipe }
            .mapValues { it.value as CookingPotRecipe }

        fun fluidOf(ingredient: Ingredient): FluidStack? {
            if (!Configs.SERVER.REPLACE_FLUID_CONTAINERS.get()) return null
            val cloned = Ingredient.fromJson(ingredient.toJson())
            val fluids = cloned.items.mapNotNull { stack ->
                emptyingRecipes.find {
                    val required = it.ingredients[0]
                    required.test(stack)
                }?.resultingFluid
            }

            return fluids.minByOrNull { it.amount }
        }

        return cookingRecipes.forEach { (originalID, recipe) ->
            val id = ResourceLocation(ThermomixMod.MOD_ID, "cooking/${originalID.namespace}/${originalID.path}")
            val builder = ProcessingRecipeBuilder(::MixingRecipe, id)
            builder.duration(recipe.cookTime)
            builder.requiresHeat(HeatCondition.HEATED)

            recipe.ingredients.forEach { ingredient ->
                val fluid = fluidOf(ingredient)
                if (fluid != null) builder.require(FluidIngredient.fromFluidStack(fluid))
                else builder.require(ingredient)
            }

            if (recipe.outputContainer != null && !recipe.outputContainer.isEmpty) {
                builder.require(Ingredient.of(recipe.outputContainer))
            }

            builder.output(recipe.resultItem)
            add.accept(id, builder.build())
        }
    }

}