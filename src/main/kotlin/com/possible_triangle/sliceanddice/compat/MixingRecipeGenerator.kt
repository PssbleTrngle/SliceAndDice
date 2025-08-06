package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe
import com.simibubi.create.content.kinetics.mixer.MixingRecipe
import com.simibubi.create.content.processing.recipe.HeatCondition
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder
import com.simibubi.create.foundation.fluid.FluidIngredient
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.fluids.FluidStack


object MixingRecipeGenerator {

    private fun findFluids(
        ingredient: Ingredient, emptyingRecipes: Collection<EmptyingRecipe>
    ): Pair<Collection<FluidStack>, Ingredient> {
        if (!Configs.SERVER.REPLACE_FLUID_CONTAINERS.get()) return Pair(listOf(), Ingredient.EMPTY)

        val nonFluidIngredients = mutableListOf<ItemStack>()

        val fluids = ingredient.items.mapNotNull { stack ->
            emptyingRecipes.filter { it.ingredients.isNotEmpty() }.find {
                val required = it.ingredients[0]
                required.test(stack)
            }?.resultingFluid ?: null.also { nonFluidIngredients.add(stack) }
        }.groupBy { it.fluid.fluidType }.values.map { fluidStackList -> fluidStackList.minBy { it.amount } }

        return Pair(fluids, Ingredient.of(nonFluidIngredients.stream()))
    }


    fun resolveAll(
        initialIngredients: List<Ingredient>,
        output: ItemStack,
        cookTime: Int,
        id: ResourceLocation,
        emptyingRecipes: Collection<EmptyingRecipe>
    ) = mutableListOf(Ingredients(initialIngredients, listOf())).also { list ->

        for (ingredient in initialIngredients) {
            val (fluids, nonFluidIngredients) = findFluids(ingredient, emptyingRecipes)
            if (fluids.isEmpty()) continue

            val listIterator = list.listIterator()
            listIterator.forEach { ingredients ->

                val fluidIterator = fluids.iterator()

                listIterator.set(ingredients.replaceItemsWithFluid(ingredient, fluidIterator.next()))

                if (!nonFluidIngredients.isEmpty) {
                    listIterator.add(ingredients.replaceItems(ingredient, nonFluidIngredients))
                }
                fluidIterator.forEach { fluidStack ->
                    listIterator.add(ingredients.replaceItemsWithFluid(ingredient, fluidStack))
                }
            }
        }
    }.mapIndexed { i, ingredients ->
        ProcessingRecipeBuilder(::MixingRecipe, id.withSuffix("_$i"))
            .withItemIngredients(*ingredients.items.toTypedArray())
            .withFluidIngredients(*ingredients.fluids.toTypedArray())
            .requiresHeat(HeatCondition.HEATED)
            .duration(cookTime)
            .withSingleItemOutput(output).build()
    }
}

data class Ingredients(val items: List<Ingredient>, val fluids: List<FluidIngredient>) {

    fun replaceItems(original: Ingredient, new: Ingredient) = Ingredients(
        items.toMutableList().apply { remove(original); add(new) },
        fluids.toMutableList()
    )

    fun replaceItemsWithFluid(item: Ingredient, fluid: FluidStack) = Ingredients(
        items.toMutableList().apply { remove(item) },
        fluids.toMutableList().apply { add(FluidIngredient.fromFluidStack(fluid)) }
    )
}