package com.possible_triangle.thermomix

import com.jozufozu.flywheel.core.PartialModel
import com.possible_triangle.thermomix.ThermomixMod.MOD_ID
import com.possible_triangle.thermomix.block.ThermomixBlock
import com.possible_triangle.thermomix.block.instance.ThermomixInstance
import com.possible_triangle.thermomix.block.renderer.ThermomixRenderer
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.possible_triangle.thermomix.config.Configs
import com.simibubi.create.AllTags
import com.simibubi.create.content.AllSections
import com.simibubi.create.content.CreateItemGroup
import com.simibubi.create.content.contraptions.components.AssemblyOperatorBlockItem
import com.simibubi.create.foundation.block.BlockStressDefaults
import com.simibubi.create.foundation.data.AssetLookup
import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.data.ModelGen
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.repack.registrate.util.nullness.NonNullFunction
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

    private val REGISTRATE = CreateRegistrate.lazy(MOD_ID).get()
        .creativeModeTab { CreateItemGroup.TAB_TOOLS }
        .startSection(AllSections.LOGISTICS)

    val ALLOWED_TOOLS = TagKey.create(Registry.ITEM_REGISTRY, ResourceLocation(MOD_ID, "allowed_tools"))

    val THERMOMIX_BLOCK = REGISTRATE
        .block<ThermomixBlock>("thermomix", ::ThermomixBlock)
        .initialProperties(SharedProperties::stone)
        .properties(BlockBehaviour.Properties::noOcclusion)
        .transform(AllTags.axeOrPickaxe())
        .blockstate { c, p ->
            p.simpleBlock(c.entry, AssetLookup.partialBaseModel(c, p))
        }
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .transform(BlockStressDefaults.setImpact(4.0))
        .item(::AssemblyOperatorBlockItem)
        .transform(ModelGen.customItemModel())
        .register()

    val THERMOMIX_TILE = REGISTRATE
        .tileEntity("thermomix", ::ThermomixTile)
        .instance { BiFunction { manager, tile -> ThermomixInstance(manager, tile) } }
        .renderer { NonNullFunction { ThermomixRenderer(it) } }
        .validBlock(THERMOMIX_BLOCK)
        .register()

    val THERMOMIX_HEAD = PartialModel(ResourceLocation(MOD_ID, "block/thermomix/head"))

    fun register(modBus: IEventBus) {
        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)
    }

}