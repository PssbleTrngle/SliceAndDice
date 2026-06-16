package com.possible_triangle.sliceanddice.compat.jei

import com.possible_triangle.sliceanddice.compat.FarmersDelightCompat
import com.possible_triangle.sliceanddice.compat.OverweightFarmingCompat
import com.possible_triangle.sliceanddice.modLoc
import com.simibubi.create.AllRecipeTypes
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import mezz.jei.api.runtime.IJeiRuntime

@JeiPlugin
@Suppress("unused")
class JEICompat : IModPlugin {
    private val cutting = CuttingProcessingCategory()

    override fun getPluginUid() = modLoc("jei")

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        registration.addRecipeCategories(cutting)
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        cutting.registerCatalysts(registration)

        FarmersDelightCompat.ifLoaded {
            addCatalysts(registration)
        }
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        cutting.registerRecipes(registration)
    }

    override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime) {
        val itemApplication =
            jeiRuntime.recipeManager
                .createRecipeCategoryLookup()
                .get()
                .filter {
                    it.recipeType.uid == AllRecipeTypes.ITEM_APPLICATION.id
                }.findFirst()
                .map { it.recipeType as RecipeType<ItemApplicationRecipe> }

        itemApplication.ifPresent { category ->
            OverweightFarmingCompat.Companion.ifLoaded {
                registerRecipes { recipes ->
                    jeiRuntime.recipeManager.addRecipes(category, recipes)
                }
            }
        }
    }
}
