package com.lhy.mekccupgrades.mixin.pseudo;

import com.lhy.mekccupgrades.mixin.accessor.ItemAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.jerry.mekmm.common.item.block.machine.ItemBlockMoreMachineFactory", remap = false)
public abstract class MekanismMoreMachineFactoryItemMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mekConfigCardUpgrades$expandFactoryStackSize(CallbackInfo ci) {
        ((ItemAccessor) this).mekConfigCardUpgrades$setMaxStackSize(64);
    }
}
