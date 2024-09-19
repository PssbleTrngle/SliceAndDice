package com.possible_triangle.sliceanddice.mixins;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ProcessingRecipeSerializer.class, remap = false)
public class ProcessingRecipeSerializerMixin {

    @Redirect(
            method = "writeToBuffer",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;ingredients:Lnet/minecraft/core/NonNullList;", opcode = Opcodes.GETFIELD)
    )
    private NonNullList<Ingredient> useIngredientsGetter(ProcessingRecipe<?> instance) {
        return instance.getIngredients();
    }

    @Redirect(
            method = "writeToBuffer",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/processing/recipe/ProcessingRecipe;fluidIngredients:Lnet/minecraft/core/NonNullList;", opcode = Opcodes.GETFIELD)
    )
    private NonNullList<FluidIngredient> useFluidIngredientsGetter(ProcessingRecipe<?> instance) {
        return instance.getFluidIngredients();
    }

}
