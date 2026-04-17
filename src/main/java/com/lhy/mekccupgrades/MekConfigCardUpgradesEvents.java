package com.lhy.mekccupgrades;

import com.lhy.mekccupgrades.item.CardSlotBagItem;
import java.util.Collection;
import mekanism.common.item.ItemConfigurationCard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class MekConfigCardUpgradesEvents {
    private MekConfigCardUpgradesEvents() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesEvents::onRightClickItem);
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof ItemConfigurationCard)) {
            return;
        }
        if (!UltimineCompat.isPressed(player)) {
            return;
        }
        Direction face = event.getFace() == null ? Direction.UP : event.getFace();
        Collection<BlockPos> positions = UltimineCompat.getCachedPositions(player, event.getPos(), face);
        if (positions.size() <= 1) {
            return;
        }
        int affected = 0;
        for (BlockPos pos : positions) {
            if (!player.level().hasChunkAt(pos)) {
                continue;
            }
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
            InteractionResult result = stack.useOn(new UseOnContext(player, event.getHand(), hitResult));
            if (result.consumesAction()) {
                affected++;
            }
            if (player.getItemInHand(event.getHand()).isEmpty()) {
                break;
            }
        }
        if (affected > 0) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof ItemConfigurationCard) || player.isShiftKeyDown()) {
            return;
        }
        if (CardSlotBagItem.tryOpenFromConfigCard(player)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
