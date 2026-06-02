package com.possible_triangle.sliceanddice.compat

import com.mojang.datafixers.util.Either
import com.possible_triangle.sliceanddice.LOGGER
import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe
import com.simibubi.create.content.kinetics.mixer.MixingRecipe
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient
import kotlin.jvm.optionals.getOrNull

class MixingRecipeGenerator(
    private val emptyingRecipes: Collection<EmptyingRecipe>,
) {
    private fun getFromEmptying(stack: ItemStack) =
        emptyingRecipes
            .filter { it.ingredients.isNotEmpty() }
            .find {
                val required = it.ingredients[0]
                required.test(stack)
            }?.resultingFluid

    private fun getFromFluidHandler(stack: ItemStack): FluidStack? =
        stack
            .getCapability(Capabilities.FluidHandler.ITEM)
            ?.drain(1000, IFluidHandler.FluidAction.SIMULATE)

    private fun resolveIngredient(stack: ItemStack): Either<FluidStack, ItemStack> {
        val fluid = getFromEmptying(stack) ?: getFromFluidHandler(stack)
        return fluid
            ?.takeUnless { it.isEmpty }
            ?.let { Either.left(fluid) }
            ?: Either.right(stack)
    }

    private fun findFluids(ingredient: Ingredient): Pair<Collection<FluidStack>, Ingredient> {
        if (!Configs.SERVER.replaceFluidContainers.get()) return Pair(listOf(), Ingredient.EMPTY)

        val resolved = ingredient.items.map { resolveIngredient(it) }
        val fluids =
            resolved
                .mapNotNull { it.left().getOrNull() }
                .groupBy { it.fluid.fluidType }
                .values
                .map { fluidStacks -> fluidStacks.minBy { it.amount } }

        val stacks = resolved.mapNotNull { it.right().getOrNull() }

        return Pair(fluids, Ingredient.of(stacks.stream()))
    }

    fun resolveAll(
        ingredients: List<Ingredient>,
        output: ItemStack,
        cookTime: Int,
        id: ResourceLocation,
    ): Collection<MixingRecipe> {
        val initial = Ingredients(ingredients, emptyList())
        val variations =
            ingredients.fold(listOf(initial)) { previous, ingredient ->
                val (fluids, nonFluids) = findFluids(ingredient)
                if (fluids.isEmpty()) return@fold previous

                val fluidVariants =
                    fluids.flatMap { fluid ->
                        previous.map { it.replaceItemsWithFluid(ingredient, fluid) }
                    }

                if (nonFluids.isEmpty) return@fold fluidVariants

                fluidVariants +
                    previous.map {
                        it.replaceItems(ingredient, nonFluids)
                    }
            }

        return try {
            variations.map {
                val recipe = it.createRecipe(id, cookTime, output)
                val errors = recipe.validate()
                if (errors.isNotEmpty()) throw IllegalArgumentException(errors.joinToString())
                recipe
            }
        } catch (ex: IllegalArgumentException) {
            LOGGER.warn("Unable to convert fluids in recipe $id", ex)
            listOf(initial.createRecipe(id, cookTime, output))
        }
    }
}

data class Ingredients(
    val items: List<Ingredient>,
    val fluids: List<FluidStack>,
) {
    fun replaceItems(
        original: Ingredient,
        new: Ingredient,
    ) = Ingredients(
        items = items.filter { it != original } + new,
        fluids = fluids,
    )

    fun replaceItemsWithFluid(
        original: Ingredient,
        fluid: FluidStack,
    ): Ingredients {
        val filteredItems = items.filter { it != original }

        val fluidMatch = fluids.indexOfFirst { FluidStack.isSameFluidSameComponents(it, fluid) }
        val modifiedFluids =
            if (fluidMatch < 0) {
                fluids + fluid
            } else {
                fluids.mapIndexed { i, it ->
                    if (i == fluidMatch) {
                        it.copyWithAmount(it.amount + fluid.amount)
                    } else {
                        it
                    }
                }
            }

        return Ingredients(
            items = filteredItems,
            fluids = modifiedFluids,
        )
    }

    fun createRecipe(
        id: ResourceLocation,
        cookTime: Int,
        output: ItemStack,
    ): MixingRecipe {
        val builder =
            StandardProcessingRecipe
                .Builder(::MixingRecipe, id)
                .withItemIngredients(*items.toTypedArray())
                .withFluidIngredients(*fluids.map(SizedFluidIngredient::of).toTypedArray())
                .requiresHeat(Configs.SERVER.cookingHeatConditition.get())
                .duration(cookTime)
                .withSingleItemOutput(output)

        return builder.build()
    }
}
