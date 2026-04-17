package com.lhy.mekccupgrades.network;

import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

public record ConfigCardBlockActionPacket(BlockPos pos, Direction side, InteractionHand hand) {
    public static void encode(ConfigCardBlockActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.side);
        buffer.writeEnum(packet.hand);
    }

    public static ConfigCardBlockActionPacket decode(FriendlyByteBuf buffer) {
        return new ConfigCardBlockActionPacket(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(ConfigCardBlockActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ConfigCardUpgradeHelper.handleBlockAction(player, packet.hand, packet.pos, packet.side);
            }
        });
        context.setPacketHandled(true);
    }
}
