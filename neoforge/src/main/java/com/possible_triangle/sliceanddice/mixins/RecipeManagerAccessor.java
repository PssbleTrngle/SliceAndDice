package com.possible_triangle.sliceanddice.mixins;

import com.google.common.collect.Multimap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {

    @Accessor
    void setByName(Map<ResourceLocation, RecipeHolder<?>> recipes);

    @Accessor
    Map<ResourceLocation, RecipeHolder<?>> getByName();

    @Accessor
    void setByType(Multimap<RecipeType<?>, RecipeHolder<?>> recipes);

    @Accessor
    Multimap<RecipeType<?>, RecipeHolder<?>> getByType();

}
