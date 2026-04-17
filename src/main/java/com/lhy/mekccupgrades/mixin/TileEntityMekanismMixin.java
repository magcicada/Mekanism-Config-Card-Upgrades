package com.lhy.mekccupgrades.mixin;

import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityMekanism.class, remap = false)
public abstract class TileEntityMekanismMixin {
    @Inject(method = "getConfigurationData", at = @At("RETURN"), cancellable = true)
    private void mekConfigCardUpgrades$appendUpgradeData(Player player, CallbackInfoReturnable<CompoundTag> cir) {
        TileEntityMekanism tile = (TileEntityMekanism) (Object) this;
        CompoundTag data = ConfigCardUpgradeHelper.appendUpgradeData(tile, cir.getReturnValue());
        cir.setReturnValue(ConfigCardUpgradeHelper.appendFactoryData(tile, data));
    }
}
