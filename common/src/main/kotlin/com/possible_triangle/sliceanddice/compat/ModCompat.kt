package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.SliceAndDice
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.ItemLike
import net.minecraftforge.fml.ModList
import vectorwing.farmersdelight.common.registry.ModItems
import java.util.function.BiConsumer

interface IRecipeInjector {
    fun injectRecipes(existing: Map<ResourceLocation, Recipe<*>>, add: BiConsumer<ResourceLocation, Recipe<*>>)
}

object ModCompat : IRecipeInjector {

    const val FARMERS_DELIGHT = "farmersdelight"
    const val OVERWEIGHT_FARMING = "overweight_farming"

    fun <T> ifLoaded(mod: String, runnable: () -> T): T? {
        return if (ModList.get().isLoaded(mod)) {
            runnable()
        } else null
    }

    override fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        SliceAndDice.LOGGER.info("Injecting recipes")
        FarmersDelightCompat.ifLoaded { injectRecipes(existing, add) }
        OverweightFarmingCompat.ifLoaded { injectRecipes(existing, add) }
    }

    val exampleTool
        get(): ItemLike {
            return ifLoaded(FARMERS_DELIGHT) { ModItems.IRON_KNIFE.get() } ?: Items.IRON_AXE
        }

    val exampleInput
        get(): ItemLike {
            return ifLoaded(FARMERS_DELIGHT) { Items.CAKE } ?: Items.BIRCH_LOG
        }

    val exampleOutput
        get(): ItemLike {
            return ifLoaded(FARMERS_DELIGHT) { ModItems.CAKE_SLICE.get() } ?: Items.STRIPPED_BIRCH_LOG
        }

}