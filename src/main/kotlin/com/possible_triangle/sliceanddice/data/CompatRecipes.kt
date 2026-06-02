package com.possible_triangle.sliceanddice.data

import com.possible_triangle.sliceanddice.compat.ModCompat
import com.possible_triangle.sliceanddice.index.SDFluids
import com.possible_triangle.sliceanddice.modLoc
import com.simibubi.create.AllFluids
import com.simibubi.create.AllItems
import com.simibubi.create.content.fluids.transfer.FillingRecipe
import com.simibubi.create.content.kinetics.mixer.MixingRecipe
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe
import com.simibubi.create.foundation.data.recipe.Mods
import com.tterrag.registrate.providers.RegistrateRecipeProvider
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.common.conditions.ModLoadedCondition
import vectorwing.farmersdelight.common.registry.ModBlocks
import vectorwing.farmersdelight.common.registry.ModItems

object CompatRecipes {
    fun generate(output: RegistrateRecipeProvider) {
        StandardProcessingRecipe
            .Builder(::FillingRecipe, modLoc("hot_cocoa_from_fluid"))
            .require(Items.GLASS_BOTTLE)
            .require(AllFluids.CHOCOLATE.get(), 250)
            .output(ModItems.HOT_COCOA.get())
            .withCondition(ModLoadedCondition(ModCompat.FARMERS_DELIGHT))
            .build(output)

        fertilizerMixing(500, "tree_fertilizer")
            .require(AllItems.TREE_FERTILIZER)
            .build(output)

        fertilizerMixing(250, "compost")
            .require(ModItems.ORGANIC_COMPOST.get())
            .withCondition(ModLoadedCondition(ModCompat.FARMERS_DELIGHT))
            .build(output)

        fertilizerMixing(1000, "phyto")
            .require(Mods.TH, "phytogro")
            .withCondition(ModLoadedCondition("thermal_foundation"))
            .build(output)

        StandardProcessingRecipe
            .Builder(::FillingRecipe, modLoc("rich_soil"))
            .require(ModBlocks.ORGANIC_COMPOST.get())
            .require(SDFluids.FERTILIZER.get(), 500)
            .output(ModBlocks.RICH_SOIL.get())
            .withCondition(ModLoadedCondition(ModCompat.FARMERS_DELIGHT))
            .build(output)

        val doughTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/dough"))
        ShapelessRecipeBuilder
            .shapeless(RecipeCategory.MISC, Items.SLIME_BALL)
            .requires(doughTag)
            .requires(DyeColor.LIME.tag)
            .unlockedBy("has_item", RegistrateRecipeProvider.has(doughTag))
            .save(output, ResourceLocation.fromNamespaceAndPath("create", "crafting/appliances/slime_ball"))
    }

    private fun fertilizerMixing(
        amount: Int,
        id: String,
    ): StandardProcessingRecipe.Builder<MixingRecipe> =
        StandardProcessingRecipe
            .Builder(::MixingRecipe, modLoc("fertilizer/from_$id"))
            .require(Fluids.WATER, amount)
            .output(SDFluids.FERTILIZER.get(), amount)
}
