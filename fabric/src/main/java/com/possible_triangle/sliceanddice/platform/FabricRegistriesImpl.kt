package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.Constants
import com.possible_triangle.sliceanddice.platform.services.IRegistries
import com.simibubi.create.foundation.data.CreateRegistrate
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import java.util.function.Supplier

class FabricRegistriesImpl : IRegistries {

    companion object {
        private val REGISTRATE = CreateRegistrate.create(Constants.MOD_ID)

        fun register() {
            REGISTRATE.register()
        }
    }

    override fun createRegistrate() = REGISTRATE

    override fun <T : Recipe<*>> registerRecipeType(id: ResourceLocation): Supplier<RecipeType<T>> {
        TODO("Not yet implemented")
    }

    override fun <T : Recipe<*>> registerRecipeSerializer(
        id: ResourceLocation,
        supplier: () -> RecipeSerializer<T>,
    ): Supplier<RecipeSerializer<T>> {
        TODO("Not yet implemented")
    }
}