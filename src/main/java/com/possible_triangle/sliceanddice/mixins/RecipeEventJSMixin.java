package com.possible_triangle.sliceanddice.mixins;

import com.google.gson.JsonObject;
import com.possible_triangle.sliceanddice.RecipeInjection;
import dev.latvian.mods.kubejs.recipe.RecipeEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeEventJS.class, remap = false)
public class RecipeEventJSMixin {

    @Inject(at = @At("RETURN"), require = 0, method = "post")
    public void injectRecipes(RecipeManager recipeManager, Map<ResourceLocation, JsonObject> jsonMap, CallbackInfo ci) {
        RecipeInjection.INSTANCE.injectRecipes((RecipeManagerAccessor) recipeManager);
    }

}
