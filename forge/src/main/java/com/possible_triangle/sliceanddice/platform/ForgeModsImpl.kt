package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.compat.FarmersDelightCompat
import com.possible_triangle.sliceanddice.compat.OverweightFarmingCompat
import com.possible_triangle.sliceanddice.platform.services.IMods
import mezz.jei.api.registration.IRecipeCatalystRegistration
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import vectorwing.farmersdelight.common.registry.ModItems
import java.util.function.BiConsumer

class ForgeModsImpl : IMods {

    override val knife get() = ModItems.IRON_KNIFE.get()

    override val cakeSlice get() = ModItems.CAKE_SLICE.get()

    override fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>
    ) {
        FarmersDelightCompat.ifLoaded { injectRecipes(existing, add) }
        OverweightFarmingCompat.ifLoaded { injectRecipes(existing, add) }
    }

    override fun addCatalysts(registration: IRecipeCatalystRegistration) {
        FarmersDelightCompat.ifLoaded { addCatalysts(registration) }
    }
}