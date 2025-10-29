package com.possible_triangle.sliceanddice.mixins;

import com.possible_triangle.sliceanddice.block.slicer.SlicerBlockEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PressingBehaviour.class, remap = false)
public class PressingBehaviourMixin {

    @Redirect(
            require = 0,
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllSoundEvents$SoundEntry;playOnServer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Vec3i;)V")
    )
    private void overwriteDefaultItem(AllSoundEvents.SoundEntry instance, Level world, Vec3i pos) {
        var self = (PressingBehaviour) (Object) this;
        if(self.specifics instanceof SlicerBlockEntity slicer) {
            slicer.playSound(world, new BlockPos(pos), false);
        } else {
            instance.playOnServer(world, pos);
        }
    }

    @Redirect(
            require = 0,
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllSoundEvents$SoundEntry;playOnServer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Vec3i;FF)V")
    )
    private void overwriteDefaultItem(AllSoundEvents.SoundEntry instance, Level world, Vec3i pos, float volume, float pitch) {
        var self = (PressingBehaviour) (Object) this;
        if(self.specifics instanceof SlicerBlockEntity slicer) {
            slicer.playSound(world, new BlockPos(pos), true);
        } else {
            instance.playOnServer(world, pos);
        }
    }

}
