package com.lhy.mekccupgrades.mixin;

import com.lhy.mekccupgrades.mixin.accessor.ItemAccessor;
import mekanism.common.item.block.machine.ItemBlockFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemBlockFactory.class, remap = false)
public abstract class ItemBlockFactoryMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mekConfigCardUpgrades$expandFactoryStackSize(CallbackInfo ci) {
        ((ItemAccessor) this).mekConfigCardUpgrades$setMaxStackSize(64);
    }
}
