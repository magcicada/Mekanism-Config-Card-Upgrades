package com.lhy.mekccupgrades.item;

import com.lhy.mekccupgrades.compat.CuriosCompat;
import com.lhy.mekccupgrades.registration.ModItems;
import com.lhy.mekccupgrades.menu.CardSlotBagMenu;
import mekanism.api.Upgrade;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CardSlotBagItem extends Item {
    private static final String BAG_ITEMS_KEY = "CardSlotBagItems";
    public static final int BAG_SIZE = 27;

    public CardSlotBagItem(Properties properties) {
        super(properties);
    }

    public static boolean tryOpenFromConfigCard(ServerPlayer player) {
        BagLocation location = findFirstBagLocation(player);
        if (location == null) {
            player.sendSystemMessage(Component.literal("未找到卡槽包"));
            return false;
        }
        openMenu(player, location);
        return true;
    }

    public static BagLocation findFirstBagLocation(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (isBag(inventory.items.get(i))) {
                return BagLocation.inventory(i);
            }
        }
        for (int i = 0; i < inventory.offhand.size(); i++) {
            if (isBag(inventory.offhand.get(i))) {
                return BagLocation.offhand(i);
            }
        }
        return CuriosCompat.findFirstBag(player);
    }

    public static boolean isBag(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModItems.CARD_SLOT_BAG.get();
    }

    public static boolean isSupportedBagItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemTierInstaller) {
            return true;
        }
        String className = item.getClass().getName();
        if (className.contains("ExtraItemTierInstaller")) {
            return true;
        }
        for (Upgrade upgrade : Upgrade.values()) {
            if (UpgradeUtils.getStack(upgrade).is(item)) {
                return true;
            }
        }
        return false;
    }

    public static void appendBagContentsToSimulation(Inventory inventory, List<ItemStack> slots, Map<Item, Integer> counts) {
        forEachBagLocation(inventory.player, location -> {
            ItemStack stack = getBagStack(inventory.player, location);
            if (stack.isEmpty()) {
                return;
            }
            ItemStackHandler handler = readHandler(stack);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack copy = handler.getStackInSlot(i).copy();
                slots.add(copy);
                if (!copy.isEmpty()) {
                    counts.merge(copy.getItem(), copy.getCount(), Integer::sum);
                }
            }
        });
    }

    public static boolean consumeFromBags(Inventory inventory, Predicate<ItemStack> matcher, int amount) {
        int total = countInBags(inventory, matcher);
        if (total < amount) {
            return false;
        }
        int remaining = amount;
        Player player = inventory.player;
        for (BagLocation location : getAllBagLocations(player)) {
            ItemStack bag = getBagStack(player, location);
            if (bag.isEmpty()) {
                continue;
            }
            ItemStackHandler handler = readHandler(bag);
            boolean changed = false;
            for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
                ItemStack inSlot = handler.getStackInSlot(slot);
                if (!inSlot.isEmpty() && matcher.test(inSlot)) {
                    int toExtract = Math.min(remaining, inSlot.getCount());
                    handler.extractItem(slot, toExtract, false);
                    remaining -= toExtract;
                    changed = true;
                }
            }
            if (changed) {
                writeHandler(player, location, handler);
            }
            if (remaining <= 0) {
                inventory.setChanged();
                return true;
            }
        }
        inventory.setChanged();
        return remaining <= 0;
    }

    public static boolean hasInBags(Inventory inventory, Predicate<ItemStack> matcher, int amount) {
        return countInBags(inventory, matcher) >= amount;
    }

    public static ItemStackHandler readHandler(ItemStack bagStack) {
        ItemStackHandler handler = new ItemStackHandler(BAG_SIZE);
        CompoundTag tag = bagStack.getTag();
        if (tag != null && tag.contains(BAG_ITEMS_KEY)) {
            handler.deserializeNBT(tag.getCompound(BAG_ITEMS_KEY));
        }
        return handler;
    }

    public static void writeHandler(ItemStack bagStack, ItemStackHandler handler) {
        CompoundTag tag = bagStack.getOrCreateTag();
        tag.put(BAG_ITEMS_KEY, handler.serializeNBT());
    }

    public static ItemStack getBagStack(Player player, BagLocation location) {
        return switch (location.storageType()) {
            case INVENTORY -> location.slotIndex() >= 0 && location.slotIndex() < player.getInventory().items.size() && isBag(player.getInventory().items.get(location.slotIndex()))
                  ? player.getInventory().items.get(location.slotIndex()) : ItemStack.EMPTY;
            case OFFHAND -> location.slotIndex() >= 0 && location.slotIndex() < player.getInventory().offhand.size() && isBag(player.getInventory().offhand.get(location.slotIndex()))
                  ? player.getInventory().offhand.get(location.slotIndex()) : ItemStack.EMPTY;
            case CURIOS -> CuriosCompat.getBagStack(player, location);
        };
    }

    public static void writeHandler(Player player, BagLocation location, ItemStackHandler handler) {
        ItemStack bagStack = getBagStack(player, location).copy();
        if (bagStack.isEmpty()) {
            return;
        }
        writeHandler(bagStack, handler);
        switch (location.storageType()) {
            case INVENTORY -> {
                player.getInventory().items.set(location.slotIndex(), bagStack);
                player.getInventory().setChanged();
            }
            case OFFHAND -> {
                player.getInventory().offhand.set(location.slotIndex(), bagStack);
                player.getInventory().setChanged();
            }
            case CURIOS -> CuriosCompat.setBagStack(player, location, bagStack);
        }
    }

    public static void openMenu(ServerPlayer player, BagLocation location) {
        NetworkHooks.openScreen(player, new CardSlotBagMenu.Provider(location), location::write);
    }

    private static int countInBags(Inventory inventory, Predicate<ItemStack> matcher) {
        int[] total = {0};
        forEachBagLocation(inventory.player, location -> {
            ItemStack bag = getBagStack(inventory.player, location);
            if (bag.isEmpty()) {
                return;
            }
            ItemStackHandler handler = readHandler(bag);
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack inSlot = handler.getStackInSlot(slot);
                if (!inSlot.isEmpty() && matcher.test(inSlot)) {
                    total[0] += inSlot.getCount();
                }
            }
        });
        return total[0];
    }

    private static List<BagLocation> getAllBagLocations(Player player) {
        List<BagLocation> locations = new java.util.ArrayList<>();
        forEachBagLocation(player, locations::add);
        return locations;
    }

    private static void forEachBagLocation(Player player, java.util.function.Consumer<BagLocation> consumer) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (isBag(inventory.items.get(i))) {
                consumer.accept(BagLocation.inventory(i));
            }
        }
        for (int i = 0; i < inventory.offhand.size(); i++) {
            if (isBag(inventory.offhand.get(i))) {
                consumer.accept(BagLocation.offhand(i));
            }
        }
        CuriosCompat.forEachBag(player, consumer);
    }
}
