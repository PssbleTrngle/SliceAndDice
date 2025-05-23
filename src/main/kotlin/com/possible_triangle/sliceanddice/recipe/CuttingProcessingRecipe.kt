package com.possible_triangle.sliceanddice.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.Content
import com.possible_triangle.sliceanddice.SliceAndDice
import com.possible_triangle.sliceanddice.compat.jei.CuttingProcessingSubCategory
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory
import com.simibubi.create.content.processing.basin.BasinRecipe
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import java.util.function.Supplier

data class CuttingProcessingRecipe(
    val params: ProcessingRecipeParams,
    val tool: Ingredient? = null,
    val converted: Boolean = false
) :
    BasinRecipe(CuttingProcessingRecipe, params), IAssemblyRecipe {

    companion object : IRecipeTypeInfo {
        override fun getId() = Content.CUTTING_RECIPE_TYPE.key!!.location()

        override fun <T : RecipeSerializer<*>> getSerializer() = Content.CUTTING_SERIALIZER.get() as T

        override fun <I : RecipeInput, R : Recipe<I>> getType() = Content.CUTTING_RECIPE_TYPE.get() as RecipeType<R>
    }

    override fun matches(inv: RecipeInput, world: Level) = true

    override fun getDescriptionForAssembly(): Component {
        return Component.translatable("${SliceAndDice.MOD_ID}.recipe.assembly.slicer")
    }

    override fun addRequiredMachines(machines: MutableSet<ItemLike>) {
        machines.add(Content.SLICER_BLOCK)
    }

    override fun addAssemblyIngredients(ingredients: List<Ingredient>) {
        // Nothing to do here
    }

    override fun getJEISubCategory(): Supplier<Supplier<SequencedAssemblySubCategory>> {
        return Supplier { Supplier { CuttingProcessingSubCategory() } }
    }

    override fun getMaxInputCount() = 1

    object Serializer : ProcessingRecipeSerializer<CuttingProcessingRecipe>(::CuttingProcessingRecipe) {

        private val CODEC: MapCodec<CuttingProcessingRecipe> = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                codec<CuttingProcessingRecipe>(CuttingProcessingRecipe).forGetter { it },
                Ingredient.CODEC.fieldOf("tool").forGetter { it.tool }
            ).apply(builder) { recipe, tool -> recipe.copy(tool = tool) }
        }

        override fun codec() = CODEC

        override fun toNetwork(buffer: RegistryFriendlyByteBuf, recipe: CuttingProcessingRecipe) {
            super.toNetwork(buffer, recipe)
            buffer.writeBoolean(recipe.converted)
            buffer.writeBoolean(recipe.tool != null)
            if (recipe.tool != null) Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tool)
        }

        override fun fromNetwork(buffer: RegistryFriendlyByteBuf): CuttingProcessingRecipe {
            val recipe = super.fromNetwork(buffer)
            val converted = buffer.readBoolean()
            val tool = if (buffer.readBoolean()) Ingredient.CONTENTS_STREAM_CODEC.decode(buffer) else null
            return recipe.copy(tool = tool, converted = converted)
        }

    }

}