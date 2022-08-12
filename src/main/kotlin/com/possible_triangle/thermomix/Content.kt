package com.possible_triangle.thermomix

import com.possible_triangle.thermomix.ThermomixMod.MOD_ID
import com.possible_triangle.thermomix.block.ThermomixBlock
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.possible_triangle.thermomix.config.Configs
import com.simibubi.create.AllTags
import com.simibubi.create.content.AllSections
import com.simibubi.create.content.CreateItemGroup
import com.simibubi.create.content.contraptions.components.mixer.MixerInstance
import com.simibubi.create.foundation.block.BlockStressDefaults
import com.simibubi.create.foundation.data.AssetLookup
import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.repack.registrate.util.entry.BlockEntityEntry
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.config.ModConfig
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import java.util.function.BiFunction
import java.util.function.Supplier

object Content {

    val REGISTRATE = CreateRegistrate.lazy(MOD_ID).get()
        .creativeModeTab { CreateItemGroup.TAB_TOOLS }
        .startSection(AllSections.LOGISTICS)

    val KNIVES = TagKey.create(Registry.ITEM_REGISTRY, ResourceLocation("farmersdelight"))

    val THERMOMIX_BLOCK = REGISTRATE
        .block<ThermomixBlock>("thermomix", ::ThermomixBlock)
        .initialProperties(SharedProperties::stone)
        .properties(BlockBehaviour.Properties::noOcclusion)
        .transform(AllTags.axeOrPickaxe())
        .blockstate { c, p ->
            p.simpleBlock(c.entry, AssetLookup.partialBaseModel(c, p));
        }
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .transform(BlockStressDefaults.setImpact(4.0))
        //.item(AssemblyOperatorBlockItem::new)
        //.transform { NonNullFunction { ModelGen.customItemModel() } }
        .register()

    val THERMOMIX_TILE = REGISTRATE
        .tileEntity("thermomix", ::ThermomixTile)
        .instance { BiFunction { manager, tile -> MixerInstance(manager, tile) } }
        .validBlock(THERMOMIX_BLOCK)
        .register()

    fun register(modBus: IEventBus) {
        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)
    }

}