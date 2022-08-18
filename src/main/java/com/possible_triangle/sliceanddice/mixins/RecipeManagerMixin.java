package com.possible_triangle.sliceanddice.mixins;

import com.google.gson.JsonElement;
import com.possible_triangle.sliceanddice.RecipeInjection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Accessor
    public abstract void setByName(Map<ResourceLocation, Recipe<?>> recipes);

    @Accessor
    public abstract Map<ResourceLocation, Recipe<?>> getByName();

    @Accessor
    public abstract void setRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes);

    @Inject(at = @At("RETURN"), method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    public void injectRecipes(Map<ResourceLocation, JsonElement> json, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        RecipeInjection.INSTANCE.injectRecipes((RecipeManagerAccessor) this);
    }

}
