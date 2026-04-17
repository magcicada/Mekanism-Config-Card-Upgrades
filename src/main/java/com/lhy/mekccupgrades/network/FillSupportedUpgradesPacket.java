package com.lhy.mekccupgrades.network;

import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import java.util.function.Supplier;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public record FillSupportedUpgradesPacket(BlockPos pos, Direction side, InteractionHand hand) {
    public static void encode(FillSupportedUpgradesPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.side);
        buffer.writeEnum(packet.hand);
    }

    public static FillSupportedUpgradesPacket decode(FriendlyByteBuf buffer) {
        return new FillSupportedUpgradesPacket(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(FillSupportedUpgradesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            BlockEntity tile = WorldUtils.getTileEntity(player.level(), packet.pos);
            if (!(tile instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades()) {
                return;
            }
            boolean changed = ConfigCardUpgradeHelper.fillSupportedUpgrades(player, upgradeTile);
            if (changed) {
                player.sendSystemMessage(Component.literal("已一键放入支持的升级卡"));
            }
        });
        context.setPacketHandled(true);
    }
}
