package com.possible_triangle.sliceanddice.api.data

import com.mojang.serialization.Lifecycle
import com.possible_triangle.sliceanddice.MOD_ID
import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.SprinkleAction
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import net.minecraft.core.*
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.registries.RegistryPatchGenerator
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation.fromNamespaceAndPath
import net.neoforged.neoforge.common.conditions.ICondition
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.fluids.crafting.FluidIngredient
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

object SprinklerProvider {
    fun of(
        output: PackOutput,
        lookupProvider: CompletableFuture<HolderLookup.Provider>,
        modId: String,
        consumer: Consumer<SprinklerRegistration>,
    ): DataProvider {
        val registryBuilder = RegistrySetBuilder()
        val conditionsMap = hashMapOf<ResourceKey<*>, List<ICondition>>()

        registryBuilder.add(SDRegistries.SPRINKLERS) {
            consumer.accept(
                object : WrappedRegistration(it) {
                    override fun register(
                        name: String,
                        fluid: FluidIngredient,
                        config: SprinkleAction,
                        conditions: Collection<ICondition>,
                    ) {
                        val key = ResourceKey.create(SDRegistries.SPRINKLERS, fromNamespaceAndPath(modId, name))
                        it.register(key, Sprinkler(fluid, config = config))
                        if (conditions.isNotEmpty()) {
                            conditionsMap[key] = conditions.toList()
                        }
                    }
                },
            )
        }

        return DatapackBuiltinEntriesProvider(
            output,
            RegistryPatchGenerator.createLookup(lookupProvider, registryBuilder),
            conditionsMap,
            setOf(MOD_ID, modId),
        )
    }

    interface SprinklerRegistration : BootstrapContext<Sprinkler<*>> {
        fun register(
            name: String,
            fluid: FluidIngredient,
            config: SprinkleAction,
            conditions: Collection<ICondition>,
        )

        fun register(
            name: String,
            fluid: FluidIngredient,
            config: SprinkleAction,
        ) = register(name, fluid, config, emptyList())
    }

    private abstract class WrappedRegistration(
        private val inner: BootstrapContext<Sprinkler<*>>,
    ) : SprinklerRegistration {
        override fun register(
            key: ResourceKey<Sprinkler<*>?>,
            value: Sprinkler<*>,
            lifecycle: Lifecycle,
        ): Holder.Reference<Sprinkler<*>> = inner.register(key, value, lifecycle)

        override fun <S> lookup(key: ResourceKey<out Registry<out S>>): HolderGetter<S> = inner.lookup(key)
    }
}
