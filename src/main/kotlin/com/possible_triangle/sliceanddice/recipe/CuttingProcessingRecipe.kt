package com.possible_triangle.sliceanddice.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.possible_triangle.sliceanddice.MOD_ID
import com.possible_triangle.sliceanddice.compat.jei.CuttingProcessingSubCategory
import com.possible_triangle.sliceanddice.index.SDBlocks
import com.possible_triangle.sliceanddice.index.SDRecipeTypes
import com.possible_triangle.sliceanddice.recipe.CuttingProcessingRecipe.Params
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory
import com.simibubi.create.content.processing.recipe.ProcessingRecipe
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import java.util.function.Supplier

data class CuttingProcessingRecipe(
    val params: Params,
) : ProcessingRecipe<RecipeInput, Params>(CuttingProcessingRecipe, params),
    IAssemblyRecipe {
    @Suppress("UNCHECKED_CAST")
    companion object : IRecipeTypeInfo {
        override fun getId() = SDRecipeTypes.CUTTING_RECIPE_TYPE.key!!.location()

        override fun <T : RecipeSerializer<*>> getSerializer() = SDRecipeTypes.CUTTING_SERIALIZER.get() as T

        override fun <I : RecipeInput, R : Recipe<I>> getType() = SDRecipeTypes.CUTTING_RECIPE_TYPE.get() as RecipeType<R>
    }

    override fun matches(
        inv: RecipeInput,
        world: Level,
    ) = true

    override fun getDescriptionForAssembly(): Component = Component.translatable("${MOD_ID}.recipe.assembly.slicer")

    override fun addRequiredMachines(machines: MutableSet<ItemLike>) {
        machines.add(SDBlocks.SLICER)
    }

    override fun addAssemblyIngredients(ingredients: List<Ingredient>) {
        // Nothing to do here
    }

    override fun getJEISubCategory(): Supplier<Supplier<SequencedAssemblySubCategory>> =
        Supplier {
            Supplier { CuttingProcessingSubCategory() }
        }

    override fun getMaxInputCount() = 1

    override fun getMaxOutputCount() = 1

    class Params : ProcessingRecipeParams() {
        var tool: Ingredient? = null
        var converted: Boolean = false

        companion object {
            val CODEC: MapCodec<Params> =
                RecordCodecBuilder.mapCodec { builder ->
                    builder
                        .group(
                            codec(::Params).forGetter { it },
                            Ingredient.CODEC.fieldOf("tool").forGetter { it.tool },
                        ).apply(builder) { params, tool ->
                            params.tool = tool
                            params
                        }
                }

            val STREAM_CODEC = streamCodec(::Params)
        }

        override fun encode(buffer: RegistryFriendlyByteBuf) {
            super.encode(buffer)
            buffer.writeBoolean(tool != null)
            tool?.let {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, it)
            }
            buffer.writeBoolean(converted)
        }

        override fun decode(buffer: RegistryFriendlyByteBuf) {
            super.decode(buffer)
            if (buffer.readBoolean()) {
                tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)
            }
            converted = buffer.readBoolean()
        }
    }

    object Serializer : RecipeSerializer<CuttingProcessingRecipe> {
        private val CODEC = codec(::CuttingProcessingRecipe, Params.CODEC)
        private val STREAM_CODEC = streamCodec(::CuttingProcessingRecipe, Params.STREAM_CODEC)

        override fun codec() = CODEC

        override fun streamCodec() = STREAM_CODEC
    }

    override fun validate(): List<String> {
        val errors = super.validate()
        if (params.tool == null) {
            errors.add("recipe tool should not be null")
        }
        return errors
    }

    class Builder(
        recipeId: ResourceLocation,
    ) : ProcessingRecipeBuilder<Params, CuttingProcessingRecipe, Builder>(::CuttingProcessingRecipe, recipeId) {
        override fun createParams() = Params()

        override fun self() = this

        fun tool(tool: Ingredient) =
            apply {
                params.tool = tool
            }

        fun converted() =
            apply {
                params.converted = true
            }
    }
}
