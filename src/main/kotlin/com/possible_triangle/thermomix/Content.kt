package com.possible_triangle.thermomix

import com.jozufozu.flywheel.core.PartialModel
import com.possible_triangle.thermomix.ThermomixMod.MOD_ID
import com.possible_triangle.thermomix.block.SprinklerBlock
import com.possible_triangle.thermomix.block.ThermomixBlock
import com.possible_triangle.thermomix.block.WetAir
import com.possible_triangle.thermomix.block.instance.ThermomixInstance
import com.possible_triangle.thermomix.block.renderer.ThermomixRenderer
import com.possible_triangle.thermomix.block.tile.SprinkleBehaviour
import com.possible_triangle.thermomix.block.tile.SprinklerTile
import com.possible_triangle.thermomix.block.tile.ThermomixTile
import com.possible_triangle.thermomix.config.Configs
import com.possible_triangle.thermomix.recipe.CuttingProcessingRecipe
import com.simibubi.create.AllBlocks
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
import com.tterrag.registrate.util.nullness.NonNullFunction
import com.tterrag.registrate.providers.RegistrateRecipeProvider.has
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.FluidTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import thedarkcolour.kotlinforforge.forge.registerObject
import vectorwing.farmersdelight.common.registry.ModBlocks
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
        .recipe { c, p ->
            ShapedRecipeBuilder.shaped(c.entry)
                .pattern("A")
                .pattern("B")
                .pattern("C")
                .define('A', AllBlocks.COGWHEEL.get())
                .define('B', AllBlocks.ANDESITE_CASING.get())
                .define('C', AllBlocks.TURNTABLE.get())
                .unlockedBy("has_cutting_board", has(ModBlocks.CUTTING_BOARD.get()))
                .unlockedBy("has_mixer", has(AllBlocks.MECHANICAL_MIXER.get()))
                .save(p)
        }
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

    val WET_AIR = REGISTRATE
        .block<WetAir>("wet_air", ::WetAir)
        .initialProperties { Blocks.CAVE_AIR }
        .properties { it.randomTicks() }
        .blockstate { c, p ->
            p.simpleBlock(c.entry, p.models().withExistingParent(c.name, "block/barrier"))
        }
        .register()

    val SPRINKLER_BLOCK = REGISTRATE
        .block<SprinklerBlock>("sprinkler", ::SprinklerBlock)
        .initialProperties { SharedProperties.copperMetal() }
        .transform(AllTags.pickaxeOnly())
        .addLayer { Supplier { RenderType.cutoutMipped() } }
        .blockstate { c, p -> p.simpleBlock(c.entry, AssetLookup.standardModel(c, p)) }
        .item()
        .transform(ModelGen.customItemModel("_"))
        .recipe { c, p ->
            ShapedRecipeBuilder.shaped(c.entry, 3)
                .pattern("SPS")
                .pattern("SBS")
                .define('S', AllTags.forgeItemTag("plates/copper"))
                .define('B', Blocks.IRON_BARS)
                .define('P', AllBlocks.FLUID_PIPE.get())
                .unlockedBy("has_pipe", has(AllBlocks.FLUID_PIPE.get()))
                .save(p)
        }
        .register()

    val SPRINKLER_TILE = REGISTRATE
        .tileEntity("sprinkler", ::SprinklerTile)
        .validBlock(SPRINKLER_BLOCK)
        .register()

    fun register(modBus: IEventBus) {
        LOADING_CONTEXT.registerConfig(ModConfig.Type.COMMON, Configs.SERVER_SPEC)
        LOADING_CONTEXT.registerConfig(ModConfig.Type.CLIENT, Configs.CLIENT_SPEC)

        RECIPE_SERIALIZERS.register(modBus)
        RECIPE_TYPES.register(modBus)

        modBus.addListener { _: FMLClientSetupEvent -> PonderScenes.register() }
        modBus.addListener { _: GatherDataEvent -> PonderScenes.register() }

        SprinkleBehaviour.register(FluidTags.WATER) { pos, world, _, random ->
            val start = pos.offset(-2, -7, -2)
            val end = pos.offset(2, -1, 2)
            val state = WET_AIR.defaultState
            for (it in BlockPos.betweenClosed(start, end)) {
                if (world.getBlockState(it).isAir) {
                    world.setBlockAndUpdate(it, state)
                    world.scheduleTick(it, WET_AIR.get(), random.nextInt(60, 120))
                }
            }
        }

        SprinkleBehaviour.register(FluidTags.LAVA) { pos, world, _, _ ->
            val start = pos.offset(-2, -7, -2)
            val end = pos.offset(2, -1, 2)
            world.getEntities(EntityTypeTest.forClass(LivingEntity::class.java), AABB(start, end)) {
                !it.fireImmune()
            }.forEach {
                it.hurt(DamageSource.IN_FIRE, 0.5F)
            }
        }
    }

}