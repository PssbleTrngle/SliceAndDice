package com.possible_triangle.thermomix.compat

import com.possible_triangle.thermomix.Content
import com.possible_triangle.thermomix.ThermomixMod
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import vectorwing.farmersdelight.integration.jei.FDRecipeTypes

@JeiPlugin
@SuppressWarnings("unused")
class ThermomixJEI : IModPlugin {

    override fun getPluginUid() = ResourceLocation(ThermomixMod.MOD_ID, "jei")

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addRecipeCatalyst(ItemStack(Content.THERMOMIX_BLOCK.get()), FDRecipeTypes.COOKING)
    }

}