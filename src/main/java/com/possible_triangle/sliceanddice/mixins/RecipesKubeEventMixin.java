package com.possible_triangle.sliceanddice.mixins;

import com.google.gson.JsonElement;
import com.possible_triangle.sliceanddice.RecipeInjection;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipesKubeEvent.class, remap = false)
public class RecipesKubeEventMixin {

    @Inject(at = @At("RETURN"), require = 0, method = "post(Ldev/latvian/mods/kubejs/core/RecipeManagerKJS;Ljava/util/Map;)V")
    public void injectRecipes(RecipeManagerKJS recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        RecipeInjection.INSTANCE.injectRecipes((RecipeManagerAccessor) recipeManager);
    }

}
