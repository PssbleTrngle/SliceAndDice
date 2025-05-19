package com.possible_triangle.sliceanddice.mixins;

import com.possible_triangle.sliceanddice.block.sprinkler.WetAir;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "isRainingAt(Lnet/minecraft/core/BlockPos;)Z")
    public void isRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        var self = (Level) (Object) this;

        if(WetAir.check(self, pos)) {
            callback.setReturnValue(true);
        }
    }

}
