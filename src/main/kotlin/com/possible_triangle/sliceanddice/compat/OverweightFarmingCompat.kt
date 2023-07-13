package com.possible_triangle.sliceanddice.compat

import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.compat.ModCompat.OVERWEIGHT_FARMING
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.BiConsumer
import java.util.function.Supplier


class OverweightFarmingCompat private constructor() : IRecipeInjector {
    private object MiscEvents {
        val PEELABLES = Supplier { emptyMap<Block, Block>() }
        val WAXABLES = Supplier { emptyMap<Block, Block>() }
    }

    companion object {
        private val INSTANCE = OverweightFarmingCompat()

        fun ifLoaded(runnable: OverweightFarmingCompat.() -> Unit) {
            ModCompat.ifLoaded(OVERWEIGHT_FARMING) {
                runnable(INSTANCE)
            }
        }
    }

    fun registerRecipes(register: (List<ManualApplicationRecipe>) -> Unit) {
        val axe = Ingredient.of(Items.IRON_AXE)
        val recipes = MiscEvents.PEELABLES.get().map { (from, to) ->
            val fromId = ForgeRegistries.BLOCKS.getKey(from)!!
            val toId = ForgeRegistries.BLOCKS.getKey(to)!!
            val id = ResourceLocation(
                SliceAndDice.MOD_ID,
                "$OVERWEIGHT_FARMING/peeling/from_${fromId.path}_to_${toId.path}"
            )
            ProcessingRecipeBuilder(::ManualApplicationRecipe, id).let {
                it.output(to)
                it.require(from)
                it.require(axe)
                it.toolNotConsumed()
                it.build()
            }
        }

        register(recipes)
    }

    override fun injectRecipes(
        existing: Map<ResourceLocation, Recipe<*>>,
        add: BiConsumer<ResourceLocation, Recipe<*>>,
    ) {
        MiscEvents.WAXABLES.get().forEach { (from, to) ->
            val fromId = ForgeRegistries.BLOCKS.getKey(from)!!
            val toId = ForgeRegistries.BLOCKS.getKey(to)!!
            val id = ResourceLocation(
                SliceAndDice.MOD_ID,
                "$OVERWEIGHT_FARMING/waxing/from_${fromId.path}_to_${toId.path}"
            )
            val recipe = ProcessingRecipeBuilder(::DeployerApplicationRecipe, id).let {
                it.output(to)
                it.require(from)
                it.require(Blocks.HONEYCOMB_BLOCK)
                it.toolNotConsumed()
                it.build()
            }
            add.accept(id, recipe)
        }
    }
}