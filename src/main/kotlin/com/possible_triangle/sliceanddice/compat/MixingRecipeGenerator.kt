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

    private fun findFluid(
        ingredient: Ingredient, emptyingRecipes: Collection<EmptyingRecipe>
    ): Pair<FluidIngredient, Ingredient> {
        if (!Configs.SERVER.REPLACE_FLUID_CONTAINERS.get()) return Pair(FluidIngredient.EMPTY, Ingredient.EMPTY)

        val nonFluidIngredients = mutableListOf<ItemStack>()

        val fluids = ingredient.items.mapNotNull { stack ->
            emptyingRecipes.filter { it.ingredients.isNotEmpty() }.find {
                val required = it.ingredients[0]
                required.test(stack)
            }?.resultingFluid ?: FluidStack.EMPTY.also { nonFluidIngredients.add(stack) }
        }

        return Pair(
            if (fluids.all { it.isEmpty }) FluidIngredient.EMPTY
            else FluidIngredient.fromFluidStack(fluids.filter { !it.isEmpty }.minBy { it.amount }),
            Ingredient.of(nonFluidIngredients.stream())
        )
    }


    fun resolveAll(
        initialIngredients: MutableList<Ingredient>,
        output: ItemStack,
        cookTime: Int,
        id: ResourceLocation,
        emptyingRecipes: Collection<EmptyingRecipe>
    ) = mutableListOf(Ingredients(ArrayList(initialIngredients), mutableListOf())).also { list ->

        fun addAlternativeRecipes(fluidIngredient: FluidIngredient, itemIngredient: Ingredient) {
            val iterator = list.listIterator()
            iterator.forEach { ingredients ->
                iterator.add(
                    Ingredients(
                        ArrayList(ingredients.item).also { it.add(itemIngredient) },
                        ArrayList(ingredients.fluid).also { it.remove(fluidIngredient) }
                    )
                )
            }
        }

        initialIngredients.forEach { ingredient ->
            val pair = findFluid(ingredient, emptyingRecipes)

            println(pair.first)
            println(pair.second.items.asList())
            println()

            if (pair.first == FluidIngredient.EMPTY) return@forEach // no fluid found

            list.forEach {
                it.item.remove(ingredient)
                it.fluid.add(pair.first)
            }
            if (!pair.second.isEmpty) addAlternativeRecipes(pair.first, pair.second)
        }
    }.mapIndexed { i, ingredients ->
        ProcessingRecipeBuilder(::MixingRecipe, id.withSuffix("_$i"))
            .withItemIngredients(*ingredients.item.toTypedArray())
            .withFluidIngredients(*ingredients.fluid.toTypedArray())
            .requiresHeat(HeatCondition.HEATED)
            .duration(cookTime)
            .withSingleItemOutput(output).build()
    }
}

data class Ingredients(val item: MutableList<Ingredient>, val fluid: MutableList<FluidIngredient>)