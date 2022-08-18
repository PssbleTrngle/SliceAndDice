package com.possible_triangle.sliceanddice.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.possible_triangle.sliceanddice.compat.ModCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    private Map<ResourceLocation, Recipe<?>> byName;

    @Shadow
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Accessor
    abstract void setByName(Map<ResourceLocation, Recipe<?>> recipes);

    @Accessor
    abstract void setRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes);

    @Inject(at = @At("RETURN"), method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    public void injectRecipes(Map<ResourceLocation, JsonElement> json, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        ImmutableMap.Builder<ResourceLocation, Recipe<?>> byNameBuilder = ImmutableMap.builder();
        byNameBuilder.putAll(byName);

        Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> recipesBuilder = Maps.newHashMap();

        ModCompat.INSTANCE.injectRecipes(byName, byNameBuilder::put);

        var newByName = byNameBuilder.build();
        newByName.forEach((id, recipe) -> {
            var type = recipe.getType();
            recipesBuilder.computeIfAbsent(type, $ -> ImmutableMap.builder()).put(id, recipe);
        });

        setByName(newByName);
        setRecipes(recipesBuilder.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().build())));
    }

}
