package com.lhy.mekccupgrades.mixin;

import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import mekanism.api.NBTConstants;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemConfigurationCard.class, remap = false)
public abstract class ItemConfigurationCardMixin {
    /**
     * Vanilla Mekanism only checks {@code isConfigurationDataCompatible} (exact BlockEntityType) and never applies
     * factory tiers or our relaxed compatibility. Intercept server-side paste (non-sneak) so it uses the same path as
     * {@link ConfigCardUpgradeHelper#pasteCardToTarget} (left-click / packet flow).
     */
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void mekConfigCardUpgrades$delegatePaste(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return;
        }
        Level world = context.getLevel();
        if (world.isClientSide) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockEntity tile = WorldUtils.getTileEntity(world, pos);
        if (CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD, side).resolve().isEmpty()) {
            return;
        }
        if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
            return;
        }
        ItemStack stack = context.getItemInHand();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        try {
            if (ConfigCardUpgradeHelper.isClearMode(stack)) {
                Component failure = ConfigCardUpgradeHelper.pasteCardToTarget(serverPlayer, tile, side, stack, true);
                if (failure != null) {
                    player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, failure));
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(false));
                cir.cancel();
                return;
            }
            CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
            if (data.isEmpty()) {
                return;
            }
            if (ConfigCardUpgradeHelper.getStoredTileType(data) == null) {
                return;
            }
            Component failure = ConfigCardUpgradeHelper.pasteCardToTarget(serverPlayer, tile, side, stack, true);
            if (failure != null) {
                player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, failure));
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(false));
            cir.cancel();
        } catch (Throwable t) {
            MekConfigCardUpgradesMod.LOGGER.error("Configuration card paste threw unexpectedly", t);
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED,
                  Component.literal("配置卡粘贴失败：内部错误（详见日志 mekccupgrades）")));
            cir.setReturnValue(InteractionResult.sidedSuccess(false));
            cir.cancel();
        }
    }

    @Inject(method = "useOn", at = @At("RETURN"))
    private void mekConfigCardUpgrades$logCardCopyResult(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getPlayer() == null || context.getLevel().isClientSide || !context.getPlayer().isShiftKeyDown()) {
            return;
        }
        ItemStack stack = context.getItemInHand();
        ConfigCardUpgradeHelper.logCardState("copy result", stack);
    }
}
