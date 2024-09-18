package com.possible_triangle.sliceanddice.mixins;

import com.google.common.base.Suppliers;
import com.possible_triangle.sliceanddice.block.slicer.SlicerTile;
import com.possible_triangle.sliceanddice.compat.ModCompat;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.function.Supplier;

@Mixin(value = PressingBehaviour.class, remap = false)
public class PressingBehaviourMixin {

    @Redirect(
            require = 0,
            method = "tick()V",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllSoundEvents$SoundEntry;playOnServer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/Vec3i;)V")
    )
    private void overwriteDefaultItem(AllSoundEvents.SoundEntry instance, Level world, Vec3i pos) {
        var self = (PressingBehaviour) (Object) this;
        if(self.specifics instanceof SlicerTile slicer) {
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
        if(self.specifics instanceof SlicerTile slicer) {
            slicer.playSound(world, new BlockPos(pos), true);
        } else {
            instance.playOnServer(world, pos);
        }
    }

}
