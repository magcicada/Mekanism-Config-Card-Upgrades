package com.lhy.mekccupgrades.network;

import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import java.util.function.Supplier;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record TogglePasteModePacket(InteractionHand hand) {
    public static void encode(TogglePasteModePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand);
    }

    public static TogglePasteModePacket decode(FriendlyByteBuf buffer) {
        return new TogglePasteModePacket(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(TogglePasteModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getItemInHand(packet.hand);
            if (!(stack.getItem() instanceof ItemConfigurationCard)) {
                return;
            }
            ConfigCardUpgradeHelper.PasteMode mode = ConfigCardUpgradeHelper.togglePasteMode(stack);
            ConfigCardUpgradeHelper.logCardState("toggle mode", stack);
            player.sendSystemMessage(MekanismUtils.logFormat(mode.messageColor(), ConfigCardUpgradeHelper.getModeSwitchMessage(mode)));
        });
        context.setPacketHandled(true);
    }
}
