package com.possible_triangle.sliceanddice.platform.services

import mezz.jei.api.registration.IRecipeCatalystRegistration
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Recipe
import java.util.function.BiConsumer

interface IMods {

    val knife: Item
    val cakeSlice: Item

    fun injectRecipes(existing: Map<ResourceLocation, Recipe<*>>, add: BiConsumer<ResourceLocation, Recipe<*>>)

    fun addCatalysts(registration: IRecipeCatalystRegistration)

}