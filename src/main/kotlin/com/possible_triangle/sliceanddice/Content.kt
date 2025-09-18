package com.possible_triangle.sliceanddice

import com.possible_triangle.sliceanddice.SliceAndDice.MOD_ID
import com.possible_triangle.sliceanddice.block.slicer.*
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerTile
import com.possible_triangle.sliceanddice.block.sprinkler.WetAir
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.BurningBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.FertilizerBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.MoistBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.PotionBehaviour
import com.possible_triangle.sliceanddice.compat.CreateEnchantmentIndustryCompat
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.data.CompatRecipes
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllCreativeModeTabs
import com.simibubi.create.AllFluids
import com.simibubi.create.AllTags
import com.simibubi.create.api.registry.CreateRegistries
import com.simibubi.create.api.stress.BlockStressValues
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer
import com.simibubi.create.foundation.data.*
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory
import com.tterrag.registrate.providers.ProviderType
import com.tterrag.registrate.providers.RegistrateRecipeProvider.has
import com.tterrag.registrate.util.entry.ItemEntry
import com.tterrag.registrate.util.nullness.NonNullFunction
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
import net.createmod.ponder.foundation.PonderIndex
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeCallable
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.util.function.Supplier

object Content {

    fun modLoc(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }

    private val REGISTRATE = CreateRegistrate.create(MOD_ID)

    val RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID)
    val RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID)

    val ALLOWED_TOOLS = TagKey.create(Registries.ITEM, modLoc("allowed_tools"))

    val SLICER_BLOCK = REGISTRATE.block<SlicerBlock>("slicer", ::SlicerBlock).initialProperties(SharedProperties::stone)
        .properties(BlockBehaviour.Properties::noOcclusion).transform(TagGen.axeOrPickaxe()).blockstate { c, p ->
            p.simpleBlock(c.entry, AssetLookup.partialBaseModel(c, p))
        }
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .onRegister { BlockStressValues.IMPACTS.register(it) { 4.0 } }
        .item(::AssemblyOperatorBlockItem)
        .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
        .transform(ModelGen.customItemModel())
        .recipe { c, p ->
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.entry).pattern("A").pattern("B").pattern("C")
                .define('A', AllBlocks.COGWHEEL.get()).define('B', AllBlocks.ANDESITE_CASING.get())
                .define('C', AllBlocks.TURNTABLE.get()).unlockedBy("has_tool", has(ALLOWED_TOOLS))
                .unlockedBy("has_mixer", has(AllBlocks.MECHANICAL_MIXER.get())).save(p)
        }.register()

    val SLICER_TILE = REGISTRATE.blockEntity("slicer", BlockEntityFactory(::SlicerTile))
        .visual { SimpleBlockEntityVisualizer.Factory(::SlicerVisual) }
        .renderer { NonNullFunction { SlicerRenderer(it) } }.validBlock(SLICER_BLOCK).register()

    private fun <T : Recipe<*>> createRecipeType(id: ResourceLocation): RegistryObject<RecipeType<T>> {
        val type = object : RecipeType<T> {
            override fun toString() = id.toString()
        }
        return RECIPE_TYPES.register(id.path) { type }
    }

    val CUTTING_RECIPE_TYPE = createRecipeType<CuttingProcessingRecipe>(CuttingProcessingRecipe.id)

    val CUTTING_SERIALIZER = RECIPE_SERIALIZERS.register(CuttingProcessingRecipe.id.path) {
        CuttingProcessingRecipe.Serializer
    }

    val WET_AIR = REGISTRATE.block<WetAir>("wet_air", ::WetAir).initialProperties { Blocks.CAVE_AIR }
        .properties { it.randomTicks() }.blockstate { c, p ->
            p.simpleBlock(c.entry, p.models().withExistingParent(c.name, "block/barrier"))
        }.register()

    val SPRINKLER_BLOCK = REGISTRATE.block<SprinklerBlock>("sprinkler", ::SprinklerBlock)
        .initialProperties { SharedProperties.copperMetal() }.transform(TagGen.pickaxeOnly())
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .blockstate { c, p -> p.simpleBlock(c.entry, AssetLookup.standardModel(c, p)) }
        .item()
        .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
        .transform(ModelGen.customItemModel("_"))
        .recipe { c, p ->
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.entry, 3).pattern("SPS").pattern("SBS")
                .define('S', AllTags.forgeItemTag("plates/copper")).define('B', Blocks.IRON_BARS)
                .define('P', AllBlocks.FLUID_PIPE.get()).unlockedBy("has_pipe", has(AllBlocks.FLUID_PIPE.get())).save(p)
        }
        .register()

    val SPRINKLER_TILE =
        REGISTRATE.blockEntity("sprinkler", BlockEntityFactory(::SprinklerTile)).validBlock(SPRINKLER_BLOCK).register()

    private val WET_FLUIDS = TagKey.create(Registries.FLUID, modLoc("moisturizing"))
    private val HOT_FLUIDS = TagKey.create(Registries.FLUID, modLoc("burning"))
    private val FERTILIZERS = TagKey.create(Registries.FLUID, modLoc("fertilizer"))

    val FERTILIZER_BLACKLIST = TagKey.create(Registries.BLOCK, modLoc("fertilizer_blacklist"))

    val FERTILIZER_BUCKET: ItemEntry<BucketItem>
    val FERTILIZER =
        REGISTRATE.fluid("fertilizer", modLoc("block/fluid/fertilizer_still"), modLoc("block/fluid/fertilizer_flowing"))
            .lang("Liquid Fertilizer")
            .tag(FERTILIZERS)
            .source { ForgeFlowingFluid.Source(it) }
            .bucket()
            .tab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key!!)
            .model(AssetLookup.existingItemModel())
            .lang("Bucket of Liquid Fertilizer")
            .apply { FERTILIZER_BUCKET = register() }
            .parent
            .register()

    val SLICER_INTERACTION_POINT =
        REGISTRATE
            .generic<ArmInteractionPointType, SlicerArmInteractionType>(
                "slicer",
                CreateRegistries.ARM_INTERACTION_POINT_TYPE
            ) { SlicerArmInteractionType }
            .register()

    fun register(modBus: IEventBus) {
        REGISTRATE.registerEventListeners(modBus)

        REGISTRATE.addRawLang("sliceanddice.tooltip.rotationDirection", "Rotation Direction")
        REGISTRATE.addRawLang(
            "sliceanddice.gui.contraptions.wrong_direction",
            "It appears that this %s is rotating in the _wrong direction_."
        )

        REGISTRATE.addRawLang("$MOD_ID.recipe.assembly.slicer", "Cut with Slicer")
        REGISTRATE.addRawLang("$MOD_ID.recipe.slicer", "Slicer")

        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        RECIPE_SERIALIZERS.register(modBus)
        RECIPE_TYPES.register(modBus)

        DistExecutor.unsafeCallWhenOn(Dist.CLIENT) {
            SafeCallable {
                SlicerPartials.load()
                PonderScenes.setup()
            }
        }

        REGISTRATE.addDataGenerator(ProviderType.LANG) { provider ->
            PonderScenes.setup()
            PonderIndex.getLangAccess().provideLang(MOD_ID, provider::add)
        }

        REGISTRATE.addDataGenerator(ProviderType.RECIPE, CompatRecipes::generate)

        SprinkleBehaviour.register(WET_FLUIDS, MoistBehaviour)
        SprinkleBehaviour.register(HOT_FLUIDS, BurningBehaviour)
        SprinkleBehaviour.register(FERTILIZERS, FertilizerBehaviour)
        SprinkleBehaviour.register({ AllFluids.POTION.`is`(it.fluid) }, PotionBehaviour)
        CreateEnchantmentIndustryCompat.ifLoaded { registerSprinkleBehaviour() }
    }

}