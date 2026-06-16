package com.possible_triangle.sliceanddice.index

import com.possible_triangle.sliceanddice.ForgeEntrypoint.Companion.REGISTRATE
import com.possible_triangle.sliceanddice.api.sprinkler.SprinklerType
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.behaviour.MovingSprinklerBehaviour
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour
import com.simibubi.create.api.stress.BlockStressValues
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem
import com.simibubi.create.foundation.data.AssetLookup
import com.simibubi.create.foundation.data.ModelGen
import com.simibubi.create.foundation.data.SharedProperties
import com.simibubi.create.foundation.data.TagGen
import com.tterrag.registrate.providers.RegistrateRecipeProvider.has
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapedRecipeBuilder.shaped
import net.minecraft.data.recipes.ShapelessRecipeBuilder.shapeless
import net.minecraft.resources.ResourceLocation.fromNamespaceAndPath
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.neoforge.client.model.generators.ConfiguredModel

object SDBlocks {
    @JvmField
    val SLICER =
        REGISTRATE
            .block("slicer", ::SlicerBlock)
            .initialProperties(SharedProperties::stone)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(TagGen.axeOrPickaxe())
            .onRegister { BlockStressValues.IMPACTS.register(it) { 4.0 } }
            .blockstate { c, p ->
                p.simpleBlock(c.entry, AssetLookup.partialBaseModel(c, p))
            }.item(::AssemblyOperatorBlockItem)
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .transform(ModelGen.customItemModel())
            .recipe { c, p ->
                shaped(RecipeCategory.MISC, c.entry)
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

    @JvmField
    val SPRINKLER =
        REGISTRATE
            .block("sprinkler", ::SprinklerBlock)
            .initialProperties { SharedProperties.copperMetal() }
            .properties { it.noOcclusion() }
            .transform(TagGen.pickaxeOnly())
            .onRegister(movementBehaviour(MovingSprinklerBehaviour))
            .blockstate { c, p ->
                val base = c.id.withPrefix("block/")
                val ceiling = p.models().getExistingFile(base.withSuffix("/ceiling"))
                val floor = p.models().getExistingFile(base.withSuffix("/floor/base"))

                p.getVariantBuilder(c.get()).forAllStates { state ->
                    val type = state.getValue(SprinklerBlock.TYPE)
                    val model = if (type == SprinklerType.FLOOR) floor else ceiling

                    ConfiguredModel
                        .builder()
                        .modelFile(model)
                        .build()
                }
            }.item()
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .model(AssetLookup.customBlockItemModel("sprinkler", "ceiling"))
            .recipe { c, p ->
                shaped(RecipeCategory.MISC, c.entry, 3)
                    .pattern("SPS")
                    .pattern("SBS")
                    .define('S', TagKey.create(Registries.ITEM, fromNamespaceAndPath("c", "plates/copper")))
                    .define('B', Blocks.IRON_BARS)
                    .define('P', AllBlocks.FLUID_PIPE.get())
                    .unlockedByPipe()
                    .save(p)

                shapeless(RecipeCategory.MISC, c.entry)
                    .requires(SDItems.FLOOR_SPRINKLER)
                    .unlockedByPipe()
                    .save(p, "sprinkler_conversion_0")
            }.build()
            .register()
}
