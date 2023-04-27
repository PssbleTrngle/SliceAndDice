package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.Constants
import com.possible_triangle.sliceanddice.platform.Services
import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.content.contraptions.processing.ItemApplicationRecipe
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.runtime.IJeiRuntime
import net.minecraft.resources.ResourceLocation

@JeiPlugin
@Suppress("unused")
class JEICompat : IModPlugin {

    override fun getPluginUid() = ResourceLocation(Constants.MOD_ID, "jei")

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        Services.MODS.addCatalysts(registration)
    }

    override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime) {
        val itemApplication = jeiRuntime.recipeManager.createRecipeCategoryLookup().get().filter {
            it.recipeType.uid == AllRecipeTypes.ITEM_APPLICATION.id
        }.findFirst().map { it.recipeType as RecipeType<ItemApplicationRecipe> }

        itemApplication.ifPresent { category ->
            OverweightFarmingCompat.ifLoaded {
                registerRecipes { recipes ->
                    jeiRuntime.recipeManager.addRecipes(category, recipes)
                }
            }
        }
    }

}