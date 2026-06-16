package com.possible_triangle.sliceanddice.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.possible_triangle.sliceanddice.index.SDBlocks;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidTransportBehaviour.class, remap = false)
public class FluidTransportBehaviourMixin {

    @ModifyReturnValue(
        require = 0,
        method = "getRenderedRimAttachment",
        at = @At(value = "RETURN", ordinal = 3)
    )
    private AttachmentTypes overwriteDefaultItem(AttachmentTypes original, @Local(name = "facingState") BlockState facingState) {
        if (facingState.is(SDBlocks.SPRINKLER.get())) return AttachmentTypes.RIM;
        return original;
    }

}
