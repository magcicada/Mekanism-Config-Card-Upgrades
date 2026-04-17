package com.lhy.mekccupgrades.network;

import com.lhy.mekccupgrades.integration.mekanism.BagWindowContainerAccess;
import com.lhy.mekccupgrades.integration.mekanism.BagWindowInventorySlot;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record BagWindowActionPacket(ActionType actionType) {
    public enum ActionType {
        INSERT_SUPPORTED,
        EXTRACT_ALL
    }

    public static void encode(BagWindowActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.actionType);
    }

    public static BagWindowActionPacket decode(FriendlyByteBuf buffer) {
        return new BagWindowActionPacket(buffer.readEnum(ActionType.class));
    }

    public static void handle(BagWindowActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (!(menu instanceof MekanismTileContainer<?> tileContainer) || !(menu instanceof BagWindowContainerAccess access)) {
                return;
            }
            TileComponentUpgrade component = tileContainer.getTileEntity().getComponent();
            List<BagWindowInventorySlot> bagSlots = access.mekccupgrades$getBagBackingSlots();
            if (bagSlots.isEmpty()) {
                return;
            }
            switch (packet.actionType) {
                case INSERT_SUPPORTED -> insertSupportedUpgrades(component, bagSlots);
                case EXTRACT_ALL -> extractAllUpgrades(player, component, bagSlots);
            }
            access.mekccupgrades$saveBagWindow(player);
            menu.broadcastChanges();
        });
        context.setPacketHandled(true);
    }

    private static void insertSupportedUpgrades(TileComponentUpgrade component, List<BagWindowInventorySlot> bagSlots) {
        for (Upgrade upgrade : component.getSupportedTypes()) {
            int needed = upgrade.getMax() - component.getUpgrades(upgrade);
            if (needed <= 0) {
                continue;
            }
            Item upgradeItem = UpgradeUtils.getStack(upgrade).getItem();
            int available = countMatching(bagSlots, upgradeItem);
            int toInsert = Math.min(needed, available);
            if (toInsert <= 0) {
                continue;
            }
            int added = component.addUpgrades(upgrade, toInsert);
            if (added > 0) {
                extractMatching(bagSlots, upgradeItem, added);
            }
        }
    }

    private static void extractAllUpgrades(ServerPlayer player, TileComponentUpgrade component, List<BagWindowInventorySlot> bagSlots) {
        moveSlotStackToBagOrInventory(player, bagSlots, component.getUpgradeSlot().getStack());
        component.getUpgradeSlot().setEmpty();
        moveSlotStackToBagOrInventory(player, bagSlots, component.getUpgradeOutputSlot().getStack());
        component.getUpgradeOutputSlot().setEmpty();
        for (Upgrade upgrade : component.getSupportedTypes()) {
            int safety = 0;
            while (component.getUpgrades(upgrade) > 0 && safety++ < upgrade.getMax()) {
                int before = component.getUpgrades(upgrade);
                component.removeUpgrade(upgrade, true);
                if (component.getUpgrades(upgrade) == before) {
                    break;
                }
                moveSlotStackToBagOrInventory(player, bagSlots, component.getUpgradeOutputSlot().getStack());
                component.getUpgradeOutputSlot().setEmpty();
            }
        }
    }

    private static void moveSlotStackToBagOrInventory(ServerPlayer player, List<BagWindowInventorySlot> bagSlots, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remainder = insertIntoBagSlots(bagSlots, stack.copy());
        if (!remainder.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, remainder);
        }
    }

    private static ItemStack insertIntoBagSlots(List<BagWindowInventorySlot> bagSlots, ItemStack stack) {
        ItemStack remainder = stack;
        for (BagWindowInventorySlot bagSlot : bagSlots) {
            remainder = bagSlot.insertItem(remainder, Action.EXECUTE, AutomationType.MANUAL);
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return remainder;
    }

    private static int countMatching(List<BagWindowInventorySlot> bagSlots, Item item) {
        int total = 0;
        for (BagWindowInventorySlot bagSlot : bagSlots) {
            ItemStack stack = bagSlot.getStack();
            if (!stack.isEmpty() && stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void extractMatching(List<BagWindowInventorySlot> bagSlots, Item item, int amount) {
        int remaining = amount;
        for (BagWindowInventorySlot bagSlot : bagSlots) {
            ItemStack stack = bagSlot.getStack();
            if (stack.isEmpty() || !stack.is(item)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            bagSlot.shrinkStack(removed, Action.EXECUTE);
            remaining -= removed;
            if (remaining <= 0) {
                return;
            }
        }
    }
}
