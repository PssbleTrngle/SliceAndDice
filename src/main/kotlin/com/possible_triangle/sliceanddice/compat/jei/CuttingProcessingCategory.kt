package com.possible_triangle.sliceanddice.compat.jei

import com.possible_triangle.sliceanddice.MOD_ID
import com.possible_triangle.sliceanddice.config.Configs
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.possible_triangle.sliceanddice.index.SDRecipeTypes
import com.possible_triangle.sliceanddice.modLoc
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.compat.jei.EmptyBackground
import com.simibubi.create.compat.jei.ItemIcon
import com.simibubi.create.compat.jei.category.CreateRecipeCategory
import com.simibubi.create.foundation.gui.AllGuiTextures
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeHolder
import java.util.function.Supplier

@Suppress("UNCHECKED_CAST")
class CuttingProcessingCategory :
    CreateRecipeCategory<CuttingProcessingRecipe>(
        Info(
            RecipeType.createRecipeHolderType(modLoc("slicer")),
            Component.translatable("${MOD_ID}.recipe.slicer"),
            EmptyBackground(177, 85),
            ItemIcon(SLICER),
            ::loadRecipes,
            listOf(SLICER),
        ),
    ) {
    private val slicer = AnimatedSlicer(true)

    companion object {
        private val SLICER = Supplier { ItemStack(SDBlocks.SLICER) }

        private fun loadRecipes(): List<RecipeHolder<CuttingProcessingRecipe>> {
            val manager = Minecraft.getInstance().connection?.recipeManager ?: return emptyList()
            val recipes = manager.getAllRecipesFor(SDRecipeTypes.CUTTING_RECIPE_TYPE.get())

            if (Configs.SERVER.showConvertedRecipes.get()) {
                return recipes
            }

            return recipes.filterNot { it.value().params.converted }
        }
    }

    override fun setRecipe(
        builder: IRecipeLayoutBuilder,
        recipe: CuttingProcessingRecipe,
        focus: IFocusGroup,
    ) {
        builder
            .addSlot(RecipeIngredientRole.INPUT, 27, 65)
            .setBackground(getRenderedSlot(), -1, -1)
            .addIngredients(recipe.getIngredients()[0])

        recipe.params.tool?.let { tool ->
            builder
                .addSlot(RecipeIngredientRole.INPUT, 45, 5)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(tool)
        }

        recipe.rollableResults.forEachIndexed { i, output ->
            builder
                .addSlot(RecipeIngredientRole.OUTPUT, 131 + 19 * i, 65)
                .setBackground(getRenderedSlot(output), -1, -1)
                .addItemStack(output.stack)
                .addRichTooltipCallback(addStochasticTooltip(output))
        }
    }

    override fun draw(
        recipe: CuttingProcessingRecipe,
        recipeSlotsView: IRecipeSlotsView,
        graphics: GuiGraphics,
        mouseX: Double,
        mouseY: Double,
    ) {
        slicer.setRecipe(recipe)

        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 56)
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 69)
        slicer.draw(graphics, background.width / 2 - 17, 22)
    }
}
