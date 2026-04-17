package com.lhy.mekccupgrades.network;

import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

public record BatchPastePacket(BlockPos firstPos, BlockPos secondPos, Direction side, InteractionHand hand) {
    public static void encode(BatchPastePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.firstPos);
        buffer.writeBlockPos(packet.secondPos);
        buffer.writeEnum(packet.side);
        buffer.writeEnum(packet.hand);
    }

    public static BatchPastePacket decode(FriendlyByteBuf buffer) {
        return new BatchPastePacket(buffer.readBlockPos(), buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(BatchPastePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ConfigCardUpgradeHelper.batchPasteFromCard(player, packet.hand, packet.firstPos, packet.secondPos, packet.side);
            }
        });
        context.setPacketHandled(true);
    }
}
