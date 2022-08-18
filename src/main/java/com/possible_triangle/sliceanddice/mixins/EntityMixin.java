package com.possible_triangle.sliceanddice.mixins;

import com.possible_triangle.sliceanddice.Content;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At("HEAD"), method = "baseTick()V")
    public void baseTick(CallbackInfo callback) {
        Entity entity = (Entity) (Object) this;
        if (entity.level.getBlockState(entity.blockPosition()).is(Content.INSTANCE.getWET_AIR().get())) {
            entity.clearFire();
        }
    }

}
