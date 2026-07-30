package com.possible_triangle.sliceanddice.compat.jei

import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.compat.FarmersDelightCompat
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.ResourceLocation

@JeiPlugin
@Suppress("unused")
class JEICompat : IModPlugin {
    private val cutting = CuttingProcessingCategory()

    override fun getPluginUid() = ResourceLocation(SliceAndDice.MOD_ID, "jei")

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
}
