package com.possible_triangle.sliceanddice.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.possible_triangle.sliceanddice.block.slicer.SlicerBlockEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PressingBehaviour.class, remap = false)
public class PressingBehaviourMixin {

    @WrapOperation(
            require = 0,
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllSoundEvents$SoundEntry;playOnServer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Vec3i;)V")
    )
    private void overwriteDefaultItem(AllSoundEvents.SoundEntry instance, Level world, Vec3i pos, Operation<Void> original) {
        var self = (PressingBehaviour) (Object) this;
        if(self.specifics instanceof SlicerBlockEntity slicer) {
            slicer.playSound();
        } else {
            original.call(instance, world, pos);
        }
    }

    @WrapOperation(
            require = 0,
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllSoundEvents$SoundEntry;playOnServer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Vec3i;FF)V")
    )
    private void overwriteDefaultItem(AllSoundEvents.SoundEntry instance, Level world, Vec3i pos, float volume, float pitch, Operation<Void> original) {
        var self = (PressingBehaviour) (Object) this;
        if(self.specifics instanceof SlicerBlockEntity slicer) {
            slicer.playSound();
        } else {
            original.call(instance, world, pos, volume, pitch);
        }
    }

}
