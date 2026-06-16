package com.possible_triangle.sliceanddice

import com.possible_triangle.sliceanddice.api.SDRegistries
import com.possible_triangle.sliceanddice.api.sprinkler.Sprinkler
import com.possible_triangle.sliceanddice.block.slicer.SlicerArmInteractionType
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlockEntity
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlockEntity
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.index.SDBlockEntities
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.possible_triangle.sliceanddice.index.SDFluids
import com.possible_triangle.sliceanddice.index.SDItems
import com.possible_triangle.sliceanddice.index.SDPartials
import com.possible_triangle.sliceanddice.index.SDRecipeTypes
import com.possible_triangle.sliceanddice.index.SDSprinklerActions
import com.possible_triangle.sliceanddice.index.load
import com.simibubi.create.api.registry.CreateRegistries
import com.simibubi.create.foundation.data.CreateRegistrate
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.DataPackRegistryEvent
import net.neoforged.neoforge.registries.NewRegistryEvent
import net.neoforged.neoforge.registries.RegistryBuilder

@Mod(MOD_ID)
class ForgeEntrypoint {
    companion object {
        val REGISTRATE =
            CreateRegistrate
                .create(MOD_ID)
                .defaultCreativeTab(null as ResourceKey<CreativeModeTab>?)
    }

    constructor(container: ModContainer, modBus: IEventBus, dist: Dist) {
        REGISTRATE.registerEventListeners(modBus)

        REGISTRATE
            .generic(
                "slicer",
                CreateRegistries.ARM_INTERACTION_POINT_TYPE,
            ) { SlicerArmInteractionType }
            .register()

        if (dist.isClient) clientInit()

        container.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        container.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        SDBlocks.load()
        SDBlockEntities.load()
        SDFluids.load()
        SDItems.load()
        SDSprinklerActions.load()
        SDRecipeTypes.load()

        modBus.addListener { event: RegisterCapabilitiesEvent ->
            SprinklerBlockEntity.registerCapabilities(event)
            SlicerBlockEntity.registerCapabilities(event)
        }

        modBus.addListener { event: DataPackRegistryEvent.NewRegistry ->
            event.dataPackRegistry(SDRegistries.SPRINKLERS, Sprinkler.CODEC, Sprinkler.CODEC)
        }

        modBus.addListener { event: NewRegistryEvent ->
            SDRegistries.SPRINKLER_ACTIONS_REGISTRY = event.create(RegistryBuilder(SDRegistries.SPRINKLER_ACTIONS))
        }
    }

    private fun clientInit() {
        SDPartials.load()
        PonderScenes.setup()
    }
}
