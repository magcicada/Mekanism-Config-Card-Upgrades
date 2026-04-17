package com.lhy.mekccupgrades.network;

import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
          ResourceLocation.fromNamespaceAndPath(MekConfigCardUpgradesMod.MOD_ID, "main"),
          () -> PROTOCOL_VERSION,
          ModNetwork::isAcceptedVersion,
          ModNetwork::isAcceptedVersion
    );

    private static int nextId;

    private ModNetwork() {
    }

    private static boolean isAcceptedVersion(String version) {
        return PROTOCOL_VERSION.equals(version) || "ABSENT".equals(version) || "ACCEPTVANILLA".equals(version);
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, ConfigCardBlockActionPacket.class, ConfigCardBlockActionPacket::encode, ConfigCardBlockActionPacket::decode, ConfigCardBlockActionPacket::handle);
        CHANNEL.registerMessage(nextId++, BatchPastePacket.class, BatchPastePacket::encode, BatchPastePacket::decode, BatchPastePacket::handle);
        CHANNEL.registerMessage(nextId++, TogglePasteModePacket.class, TogglePasteModePacket::encode, TogglePasteModePacket::decode, TogglePasteModePacket::handle);
        CHANNEL.registerMessage(nextId++, BagWindowActionPacket.class, BagWindowActionPacket::encode, BagWindowActionPacket::decode, BagWindowActionPacket::handle);
        CHANNEL.registerMessage(nextId++, FillSupportedUpgradesPacket.class, FillSupportedUpgradesPacket::encode, FillSupportedUpgradesPacket::decode, FillSupportedUpgradesPacket::handle);
    }
}
