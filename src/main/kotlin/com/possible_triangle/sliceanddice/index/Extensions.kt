package com.possible_triangle.sliceanddice.index

import com.simibubi.create.AllBlocks
import com.tterrag.registrate.providers.RegistrateRecipeProvider.has
import net.minecraft.data.recipes.RecipeBuilder

fun RecipeBuilder.unlockedByPipe() = unlockedBy("has_pipe", has(AllBlocks.FLUID_PIPE.get()))

internal fun Any.load() {}
