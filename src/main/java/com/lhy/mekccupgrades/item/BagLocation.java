package com.lhy.mekccupgrades.item;

import net.minecraft.network.FriendlyByteBuf;

public record BagLocation(StorageType storageType, int slotIndex, String curiosIdentifier) {
    public enum StorageType {
        INVENTORY,
        OFFHAND,
        CURIOS
    }

    public static BagLocation inventory(int slotIndex) {
        return new BagLocation(StorageType.INVENTORY, slotIndex, "");
    }

    public static BagLocation offhand(int slotIndex) {
        return new BagLocation(StorageType.OFFHAND, slotIndex, "");
    }

    public static BagLocation curios(String identifier, int slotIndex) {
        return new BagLocation(StorageType.CURIOS, slotIndex, identifier);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(storageType);
        buffer.writeVarInt(slotIndex);
        buffer.writeUtf(curiosIdentifier);
    }

    public static BagLocation read(FriendlyByteBuf buffer) {
        return new BagLocation(buffer.readEnum(StorageType.class), buffer.readVarInt(), buffer.readUtf());
    }
}
