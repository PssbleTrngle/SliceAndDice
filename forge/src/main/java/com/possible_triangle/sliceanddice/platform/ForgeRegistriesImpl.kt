package com.possible_triangle.sliceanddice.platform

import com.possible_triangle.sliceanddice.Constants
import com.possible_triangle.sliceanddice.block.slicer.SlicerTile
import com.possible_triangle.sliceanddice.platform.services.IRegistries
import com.simibubi.create.content.contraptions.base.KineticTileEntity
import com.simibubi.create.foundation.data.CreateRegistrate
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.Supplier

class ForgeRegistriesImpl : IRegistries {

    companion object {
        private val REGISTRATE: CreateRegistrate = CreateRegistrate.create(Constants.MOD_ID)

        private val RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID)
        private val RECIPE_TYPES = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, Constants.MOD_ID)

        fun register(modBus: IEventBus) {
            REGISTRATE.registerEventListeners(modBus)

            RECIPE_SERIALIZERS.register(modBus)
            RECIPE_TYPES.register(modBus)
        }
    }

    override fun createRegistrate() = REGISTRATE

    override fun <T : Recipe<*>> registerRecipeType(id: ResourceLocation): Supplier<RecipeType<T>> {
        val type = object : RecipeType<T> {
            override fun toString() = id.toString()
        }
        return RECIPE_TYPES.register(id.path) { type }
    }

    override fun <T : Recipe<*>> registerRecipeSerializer(
        id: ResourceLocation,
        supplier: () -> RecipeSerializer<T>
    ): Supplier<RecipeSerializer<T>> {
        return RECIPE_SERIALIZERS.register(id.path, supplier)
    }

    override fun slicerTileFactory() = BlockEntityFactory<KineticTileEntity> { type, pos, state ->
        SlicerTile(type, pos, state)
    }

}