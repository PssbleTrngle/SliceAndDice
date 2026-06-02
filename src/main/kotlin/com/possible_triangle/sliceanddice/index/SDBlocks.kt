package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.api.stress.BlockStressValues
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem
import com.simibubi.create.foundation.data.AssetLookup
import com.simibubi.create.foundation.data.ModelGen
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.foundation.data.TagGen
import com.tterrag.registrate.providers.RegistrateRecipeProvider.has
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.ResourceLocation.fromNamespaceAndPath
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour

object SDBlocks {
    val SLICER =
        REGISTRATE
            .block("slicer", ::SlicerBlock)
            .initialProperties(SharedProperties::stone)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(TagGen.axeOrPickaxe())
            .blockstate { c, p ->
                p.simpleBlock(c.entry, AssetLookup.partialBaseModel(c, p))
            }.onRegister { BlockStressValues.IMPACTS.register(it) { 4.0 } }
            .item(::AssemblyOperatorBlockItem)
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .transform(ModelGen.customItemModel())
            .recipe { c, p ->
                ShapedRecipeBuilder
                    .shaped(RecipeCategory.MISC, c.entry)
                    .pattern("A")
                    .pattern("B")
                    .pattern("C")
                    .define('A', AllBlocks.COGWHEEL.get())
                    .define('B', AllBlocks.ANDESITE_CASING.get())
                    .define('C', AllBlocks.TURNTABLE.get())
                    .unlockedBy("has_tool", has(SDTags.ALLOWED_TOOLS))
                    .unlockedBy("has_mixer", has(AllBlocks.MECHANICAL_MIXER.get()))
                    .save(p)
            }.register()

    val SPRINKLER =
        REGISTRATE
            .block("sprinkler", ::SprinklerBlock)
            .initialProperties { SharedProperties.copperMetal() }
            .transform(TagGen.pickaxeOnly())
            .blockstate { c, p -> p.simpleBlock(c.entry, AssetLookup.standardModel(c, p)) }
            .item()
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .transform(ModelGen.customItemModel("_"))
            .recipe { c, p ->
                ShapedRecipeBuilder
                    .shaped(RecipeCategory.MISC, c.entry, 3)
                    .pattern("SPS")
                    .pattern("SBS")
                    .define('S', TagKey.create(Registries.ITEM, fromNamespaceAndPath("c", "plates/copper")))
                    .define('B', Blocks.IRON_BARS)
                    .define('P', AllBlocks.FLUID_PIPE.get())
                    .unlockedBy("has_pipe", has(AllBlocks.FLUID_PIPE.get()))
                    .save(p)
            }.register()
}
