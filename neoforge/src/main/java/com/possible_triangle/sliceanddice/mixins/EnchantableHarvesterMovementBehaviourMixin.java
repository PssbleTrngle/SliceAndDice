package com.possible_triangle.sliceanddice.mixins;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.possible_triangle.sliceanddice.compat.ModCompat;
import com.possible_triangle.sliceanddice.config.Configs;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import io.github.cotrin8672.cem.content.block.harvester.EnchantableHarvesterMovementBehaviour;
import io.github.cotrin8672.cem.util.EnchantedItemFactory;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EnchantableHarvesterMovementBehaviour.class, remap = false)
public class EnchantableHarvesterMovementBehaviourMixin {

    @Unique
    private static final Supplier<ItemStack> sliceanddice$TOOL = Suppliers.memoize(ModCompat.INSTANCE::getHarvesterTool);

    @WrapOperation(
            require = 0,
            method = "visitNewPosition(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;Lnet/minecraft/core/BlockPos;)V",
            at = @At(value = "INVOKE", target = "Lio/github/cotrin8672/cem/util/EnchantedItemFactory;getPickaxeItemStack(Lnet/minecraft/nbt/CompoundTag;Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack overwriteDefaultItem(EnchantedItemFactory instance, CompoundTag tag, MovementContext context, Operation<ItemStack> original) {
        var pickaxe = instance.getPickaxeItemStack(tag, context);
        if (Configs.getSERVER().harvesterUsesKnife.get()) {
            var tool = sliceanddice$TOOL.get().copy();
            tool.copyFrom(pickaxe, DataComponents.UNBREAKABLE, DataComponents.ENCHANTMENTS);
            return tool;
        } else {
            return pickaxe;
        }
    }

}
