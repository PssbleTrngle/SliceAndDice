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
import net.minecraft.world.level.block.Blocks
import net.orcinus.overweightfarming.events.MiscEvents
import java.util.function.BiConsumer

class OverweightFarmingCompat private constructor() : IRecipeInjector {

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
            val id = ResourceLocation(
                SliceAndDice.MOD_ID,
                "$OVERWEIGHT_FARMING/peeling/from_${from.registryName!!.path}_to_${to.registryName!!.path}"
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
            val id = ResourceLocation(
                SliceAndDice.MOD_ID,
                "$OVERWEIGHT_FARMING/waxing/from_${from.registryName!!.path}_to_${to.registryName!!.path}"
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