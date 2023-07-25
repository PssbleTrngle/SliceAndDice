package com.possible_triangle.sliceanddice.mixins;

import com.possible_triangle.sliceanddice.Content;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Inject(at = @At("HEAD"), cancellable = true, method = "isRainingAt(Lnet/minecraft/core/BlockPos;)Z")
    public void isRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        var self = (Level) (Object) this;
        for (BlockPos it : BlockPos.betweenClosed(pos.above(2), pos)) {
            if (self.getBlockState(it).is(Content.INSTANCE.getWET_AIR().get())) {
                callback.setReturnValue(true);
                break;
            }
        }
    }

}
