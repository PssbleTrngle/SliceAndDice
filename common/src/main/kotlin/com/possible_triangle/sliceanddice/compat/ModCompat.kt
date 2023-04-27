package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.Constants
import com.possible_triangle.sliceanddice.platform.Services
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.ItemLike
import java.util.function.BiConsumer

interface IRecipeInjector {
    fun injectRecipes(existing: Map<ResourceLocation, Recipe<*>>, add: BiConsumer<ResourceLocation, Recipe<*>>)
}

object ModCompat : IRecipeInjector {

    const val FARMERS_DELIGHT = "farmersdelight"
    const val OVERWEIGHT_FARMING = "overweight_farming"

    fun <T> ifLoaded(mod: String, runnable: () -> T): T? {
        return if (Services.PLATFORM.isLoaded(mod)) {
            runnable()
        } else null
    }

    override fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        Constants.LOGGER.info("Injecting recipes")
        Services.MODS.injectRecipes(existing, add)
    }

    val exampleTool
        get(): ItemLike {
            return ifLoaded(FARMERS_DELIGHT) { Services.MODS.knife } ?: Items.IRON_AXE
        }

    val exampleInput
        get(): ItemLike {
            return ifLoaded(FARMERS_DELIGHT) { Items.CAKE } ?: Items.BIRCH_LOG
        }

    val exampleOutput
        get(): ItemLike {
            return ifLoaded(FARMERS_DELIGHT) { Services.MODS.cakeSlice } ?: Items.STRIPPED_BIRCH_LOG
        }

}