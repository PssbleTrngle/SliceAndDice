package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.config.Configs
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe
import com.simibubi.create.content.kinetics.mixer.MixingRecipe
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams
import com.simibubi.create.foundation.fluid.FluidIngredient
import net.minecraft.core.NonNullList
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.fluids.FluidStack

private fun <T> Collection<T>.toNonnullList() = let {
    NonNullList.createWithCapacity<T>(it.size).apply {
        addAll(it)
    }
}

class LazyMixingRecipe(params: ProcessingRecipeParams) : MixingRecipe(params) {

    private var emptyingRecipes: Collection<EmptyingRecipe> = emptyList()
    private var resolved = false
    private lateinit var resolvedIngredients: NonNullList<Ingredient>
    private lateinit var resolvedFluidIngredients: NonNullList<FluidIngredient>

    fun withRecipeLookup(emptyingRecipes: Collection<EmptyingRecipe>) = apply {
        this.emptyingRecipes = emptyingRecipes
    }

    private fun fluidOf(ingredient: Ingredient): FluidStack {
        if (!Configs.SERVER.REPLACE_FLUID_CONTAINERS.get()) return FluidStack.EMPTY
        val fluids = ingredient.items.mapNotNull { stack ->
            emptyingRecipes.find {
                val required = it.ingredients[0]
                required.test(stack)
            }?.resultingFluid
        }

        return fluids.minByOrNull { it.amount } ?: FluidStack.EMPTY
    }

    private fun resolve() {
        if (resolved) return
        val replaceable = super.ingredients
            .associateWith { fluidOf(it) }
            .filterValues { !it.isEmpty }
            .mapValues { FluidIngredient.fromFluidStack(it.value) }

        resolvedIngredients = super.ingredients.filterNot { replaceable.containsKey(it) }.toNonnullList()
        resolvedFluidIngredients = (super.fluidIngredients + replaceable.values).toNonnullList()
        resolved = true
    }

    override fun getIngredients(): NonNullList<Ingredient> {
        resolve()
        return resolvedIngredients
    }

    override fun getFluidIngredients(): NonNullList<FluidIngredient> {
        resolve()
        return resolvedFluidIngredients
    }

}