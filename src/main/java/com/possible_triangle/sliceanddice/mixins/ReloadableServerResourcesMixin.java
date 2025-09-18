package com.possible_triangle.sliceanddice.mixins;

import com.possible_triangle.sliceanddice.RecipeInjection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Accessor
    public abstract RecipeManager getRecipes();

    @Inject(at = @At("RETURN"), method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V")
    public void injectRecipes(RegistryAccess access, CallbackInfo ci) {
        RecipeInjection.INSTANCE.injectRecipes(((RecipeManagerAccessor) getRecipes()));
    }

}
