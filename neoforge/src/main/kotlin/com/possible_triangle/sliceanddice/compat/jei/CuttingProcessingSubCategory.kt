package com.possible_triangle.sliceanddice.compat.jei

import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe
import com.simibubi.create.compat.jei.category.CreateRecipeCategory
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory
import com.simibubi.create.content.processing.sequenced.SequencedRecipe
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import net.minecraft.client.gui.GuiGraphics

class CuttingProcessingSubCategory : SequencedAssemblySubCategory(25) {
    val slicer = AnimatedSlicer(false)

    override fun setRecipe(
        builder: IRecipeLayoutBuilder,
        sequencedRecipe: SequencedRecipe<*>,
        focuses: IFocusGroup?,
        x: Int,
    ) {
        val recipe = sequencedRecipe.asAssemblyRecipe
        if (recipe !is CuttingProcessingRecipe) return
        val tool = recipe.params.tool ?: return

        builder
            .addSlot(RecipeIngredientRole.INPUT, x + 4, 15)
            .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
            .addIngredients(tool)
    }

    override fun draw(
        sequencedRecipe: SequencedRecipe<*>,
        graphics: GuiGraphics,
        x: Double,
        y: Double,
        index: Int,
    ) {
        val ms = graphics.pose()

        slicer.setRecipe(sequencedRecipe.recipe)

        slicer.offset = index
        ms.pushPose()
        ms.translate(-5.0f, 50.0f, 0.0f)
        ms.scale(0.6f, 0.6f, 0.6f)
        slicer.draw(graphics, width / 2, 0)

        ms.popPose()
    }
}
