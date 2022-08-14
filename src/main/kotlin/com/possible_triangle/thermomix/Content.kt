package com.possible_triangle.thermomix

import com.jozufozu.flywheel.core.PartialModel
import com.possible_triangle.thermomix.ThermomixMod.MOD_ID
import com.possible_triangle.thermomix.block.ThermomixBlock
import com.possible_triangle.thermomix.block.instance.ThermomixInstance
import com.possible_triangle.thermomix.block.renderer.ThermomixRenderer
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.possible_triangle.thermomix.config.Configs
import com.possible_triangle.thermomix.recipe.CuttingProcessingRecipe
import com.simibubi.create.AllTags
import com.simibubi.create.content.AllSections
import com.simibubi.create.content.CreateItemGroup
import com.simibubi.create.content.contraptions.components.AssemblyOperatorBlockItem
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer
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
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.registerObject
import java.util.function.BiFunction
import java.util.function.Supplier

object Content {

    private val REGISTRATE = CreateRegistrate.lazy(MOD_ID).get()
        .creativeModeTab { CreateItemGroup.TAB_TOOLS }
        .startSection(AllSections.LOGISTICS)

    val RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID)
    val RECIPE_TYPES = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, MOD_ID)

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


    private fun <T : Recipe<*>> createRecipeType(id: ResourceLocation): RegistryObject<RecipeType<T>> {
        val type = object : RecipeType<T> {
            override fun toString() = id.toString()
        }
        return RECIPE_TYPES.register(id.path) { type }
    }

    val CUTTING_RECIPE_TYPE = createRecipeType<CuttingProcessingRecipe>(CuttingProcessingRecipe.id)

    val CUTTING_SERIALIZER by RECIPE_SERIALIZERS.registerObject(CuttingProcessingRecipe.id.path) {
        ProcessingRecipeSerializer(
            ::CuttingProcessingRecipe
        )
    }

    fun register(modBus: IEventBus) {
        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        RECIPE_SERIALIZERS.register(modBus)
        RECIPE_TYPES.register(modBus)
    }

}