package com.possible_triangle.sliceanddice.platform.services

import com.simibubi.create.content.contraptions.base.KineticTileEntity
import com.simibubi.create.foundation.data.CreateRegistrate
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import java.util.function.Supplier

interface IRegistries {

    fun <T : Recipe<*>> registerRecipeType(id: ResourceLocation): Supplier<RecipeType<T>>

    fun <T : Recipe<*>> registerRecipeSerializer(id: ResourceLocation, supplier: () -> RecipeSerializer<T>): Supplier<RecipeSerializer<T>>

    fun createRegistrate(): CreateRegistrate

    fun slicerTileFactory(): BlockEntityFactory<KineticTileEntity>

}