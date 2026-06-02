package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.LOGGER
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.ItemLike
import net.neoforged.fml.ModList
import vectorwing.farmersdelight.common.registry.ModItems
import vectorwing.farmersdelight.common.registry.ModSounds
import java.util.function.BiConsumer

interface IRecipeInjector {
    fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    )
}

object ModCompat : IRecipeInjector {
    const val FARMERS_DELIGHT = "farmersdelight"
    const val CREATE_ENCHANTMENT_INDUSTRY = "create_enchantment_industry"
    const val OVERWEIGHT_FARMING = "overweight_farming"

    fun <T> ifLoaded(
        mod: String,
        runnable: () -> T,
    ): T? =
        if (ModList.get().isLoaded(mod)) {
            runnable()
        } else {
            null
        }

    override fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        LOGGER.info("Injecting recipes")
        FarmersDelightCompat.ifLoaded { injectRecipes(existing, add) }
        OverweightFarmingCompat.ifLoaded { injectRecipes(existing, add) }
    }

    val harvesterTool
        get(): ItemStack? {
            return ifLoaded(FARMERS_DELIGHT) { ModItems.IRON_KNIFE.get() }?.let(::ItemStack) ?: ItemStack.EMPTY
        }

    val cuttingSound
        get(): SoundEvent {
            return ifLoaded(FARMERS_DELIGHT) { ModSounds.BLOCK_CUTTING_BOARD_KNIFE.get() } ?: SoundEvents.SHEEP_SHEAR
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
