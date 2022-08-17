package com.possible_triangle.thermomix.compat

import com.possible_triangle.thermomix.ThermomixMod
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.ResourceLocation

@JeiPlugin
@Suppress("unused")
class ThermomixJEI : IModPlugin {

    override fun getPluginUid() = ResourceLocation(ThermomixMod.MOD_ID, "jei")

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        FarmersDelightCompat.ifLoaded {
            addCatalysts(registration)
        }
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        OverweightFarmingCompat.ifLoaded {
            registerRecipes(registration)
        }
    }

}