package com.possible_triangle.sliceanddice.mixins;

import com.google.common.base.Suppliers;
import com.possible_triangle.sliceanddice.compat.ModCompat;
import com.possible_triangle.sliceanddice.config.Configs;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = HarvesterMovementBehaviour.class, remap = false)
public class HarvesterMovementBehaviourMixin {

    @Unique
    private static final Supplier<ItemStack> sliceanddice$TOOL = Suppliers.memoize(ModCompat.INSTANCE::getHarvesterTool);

    @ModifyVariable(
        require = 0,
        method = "visitNewPosition(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;Lnet/minecraft/core/BlockPos;)V",
        at = @At(value = "STORE", ordinal = 0),
        name = "item"
    )
    private ItemStack overwriteDefaultItem(ItemStack stack) {
        if(Configs.getSERVER().harvesterUsesKnife.get()) {
            return sliceanddice$TOOL.get();
        } else {
            return stack;
        }
    }

}
