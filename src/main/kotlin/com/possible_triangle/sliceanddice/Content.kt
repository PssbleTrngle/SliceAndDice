package com.possible_triangle.sliceanddice

import com.possible_triangle.sliceanddice.SliceAndDice.MOD_ID
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlock
import com.possible_triangle.sliceanddice.block.slicer.SlicerInstance
import com.possible_triangle.sliceanddice.block.slicer.SlicerRenderer
import com.possible_triangle.sliceanddice.block.slicer.SlicerTile
import com.possible_triangle.sliceanddice.block.sprinkler.SprinkleBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerBlock
import com.possible_triangle.sliceanddice.block.sprinkler.SprinklerTile
import com.possible_triangle.sliceanddice.block.sprinkler.WetAir
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.BurningBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.FertilizerBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.MoistBehaviour
import com.possible_triangle.sliceanddice.block.sprinkler.behaviours.PotionBehaviour
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.AllBlocks
import com.simibubi.create.AllFluids
import com.simibubi.create.AllTags
import com.simibubi.create.content.AllSections
import com.simibubi.create.content.CreateItemGroup
import com.simibubi.create.content.contraptions.components.AssemblyOperatorBlockItem
import com.simibubi.create.content.contraptions.processing.ProcessingRecipeSerializer
import com.simibubi.create.foundation.block.BlockStressDefaults
import com.simibubi.create.foundation.data.*
import com.tterrag.registrate.providers.RegistrateRecipeProvider.has
import com.tterrag.registrate.util.nullness.NonNullFunction
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.Registry
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fluids.ForgeFlowingFluid
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeCallable
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import java.util.function.BiFunction
import java.util.function.Supplier

object Content {

    fun modLoc(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }

    private object REGISTRATE : CreateRegistrate(MOD_ID) {
        fun register(bus: IEventBus) = registerEventListeners(bus)

        init {
            creativeModeTab { CreateItemGroup.TAB_TOOLS }
            startSection(AllSections.LOGISTICS)
        }
    }

    val RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID)
    val RECIPE_TYPES = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, MOD_ID)

    val ALLOWED_TOOLS = TagKey.create(Registry.ITEM_REGISTRY, modLoc("allowed_tools"))

    val SLICER_BLOCK = REGISTRATE.block<SlicerBlock>("slicer", ::SlicerBlock).initialProperties(SharedProperties::stone)
        .properties(BlockBehaviour.Properties::noOcclusion).transform(TagGen.axeOrPickaxe()).blockstate { c, p ->
            p.simpleBlock(c.entry, AssetLookup.partialBaseModel(c, p))
        }.addLayer { Supplier { RenderType.cutoutMipped() } }.transform(BlockStressDefaults.setImpact(4.0))
        .item(::AssemblyOperatorBlockItem).transform(ModelGen.customItemModel()).recipe { c, p ->
            ShapedRecipeBuilder.shaped(c.entry).pattern("A").pattern("B").pattern("C")
                .define('A', AllBlocks.COGWHEEL.get()).define('B', AllBlocks.ANDESITE_CASING.get())
                .define('C', AllBlocks.TURNTABLE.get()).unlockedBy("has_tool", has(ALLOWED_TOOLS))
                .unlockedBy("has_mixer", has(AllBlocks.MECHANICAL_MIXER.get())).save(p)
        }.register()

    val SLICER_TILE = REGISTRATE.tileEntity("slicer", ::SlicerTile)
        .instance { BiFunction { manager, tile -> SlicerInstance(manager, tile) } }
        .renderer { NonNullFunction { SlicerRenderer(it) } }.validBlock(SLICER_BLOCK).register()

    private fun <T : Recipe<*>> createRecipeType(id: ResourceLocation): RegistryObject<RecipeType<T>> {
        val type = object : RecipeType<T> {
            override fun toString() = id.toString()
        }
        return RECIPE_TYPES.register(id.path) { type }
    }

    val CUTTING_RECIPE_TYPE = createRecipeType<CuttingProcessingRecipe>(CuttingProcessingRecipe.id)

    val CUTTING_SERIALIZER = RECIPE_SERIALIZERS.register(CuttingProcessingRecipe.id.path) {
        ProcessingRecipeSerializer(
            ::CuttingProcessingRecipe
        )
    }

    val WET_AIR = REGISTRATE.block<WetAir>("wet_air", ::WetAir).initialProperties { Blocks.CAVE_AIR }
        .properties { it.randomTicks() }.blockstate { c, p ->
            p.simpleBlock(c.entry, p.models().withExistingParent(c.name, "block/barrier"))
        }.register()

    val SPRINKLER_BLOCK = REGISTRATE.block<SprinklerBlock>("sprinkler", ::SprinklerBlock)
        .initialProperties { SharedProperties.copperMetal() }.transform(AllTags.pickaxeOnly())
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .blockstate { c, p -> p.simpleBlock(c.entry, AssetLookup.standardModel(c, p)) }.item()
        .transform(ModelGen.customItemModel("_")).recipe { c, p ->
            ShapedRecipeBuilder.shaped(c.entry, 3).pattern("SPS").pattern("SBS")
                .define('S', AllTags.forgeItemTag("plates/copper")).define('B', Blocks.IRON_BARS)
                .define('P', AllBlocks.FLUID_PIPE.get()).unlockedBy("has_pipe", has(AllBlocks.FLUID_PIPE.get())).save(p)
        }.register()

    val SPRINKLER_TILE = REGISTRATE.tileEntity("sprinkler", ::SprinklerTile).validBlock(SPRINKLER_BLOCK).register()

    private val WET_FLUIDS = TagKey.create(Registry.FLUID_REGISTRY, modLoc("moisturizing"))
    private val HOT_FLUIDS = TagKey.create(Registry.FLUID_REGISTRY, modLoc("burning"))
    private val FERTILIZERS = TagKey.create(Registry.FLUID_REGISTRY, modLoc("fertilizer"))

    val FERTILIZER_BLACKLIST = TagKey.create(Registry.BLOCK_REGISTRY, modLoc("fertilizer_blacklist"))

    val FERTILIZER =
        REGISTRATE.fluid("fertilizer", modLoc("fluid/fertilizer_still"), modLoc("fluid/fertilizer_flowing"))
            .tag(FERTILIZERS).source { ForgeFlowingFluid.Source(it) }
            .bucket().model(AssetLookup.existingItemModel()).build().register()

    fun register(modBus: IEventBus) {
        REGISTRATE.register(modBus)

        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        RECIPE_SERIALIZERS.register(modBus)
        RECIPE_TYPES.register(modBus)

        modBus.addListener { _: GatherDataEvent -> PonderScenes.register() }
        modBus.addListener { _: FMLClientSetupEvent -> PonderScenes.register() }
        DistExecutor.safeCallWhenOn(Dist.CLIENT) { SafeCallable { SlicerPartials.load() } }

        SprinkleBehaviour.register(WET_FLUIDS, MoistBehaviour)
        SprinkleBehaviour.register(HOT_FLUIDS, BurningBehaviour)
        SprinkleBehaviour.register(FERTILIZERS, FertilizerBehaviour)
        SprinkleBehaviour.register({ AllFluids.POTION.`is`(it.fluid) }, PotionBehaviour)
    }

}