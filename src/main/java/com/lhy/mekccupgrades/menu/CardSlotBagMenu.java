package com.lhy.mekccupgrades.menu;

import com.lhy.mekccupgrades.item.CardSlotBagItem;
import com.lhy.mekccupgrades.item.BagLocation;
import com.lhy.mekccupgrades.registration.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CardSlotBagMenu extends AbstractContainerMenu {
    private static final int BAG_ROWS = 3;
    private static final int BAG_COLS = 9;
    private static final int BAG_SLOT_COUNT = BAG_ROWS * BAG_COLS;
    private final Inventory playerInventory;
    private final BagLocation bagLocation;
    private final ItemStackHandler bagHandler;

    public CardSlotBagMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(id, playerInventory, BagLocation.read(buffer));
    }

    public CardSlotBagMenu(int id, Inventory playerInventory, BagLocation bagLocation) {
        super(ModMenuTypes.CARD_SLOT_BAG_MENU.get(), id);
        this.playerInventory = playerInventory;
        this.bagLocation = bagLocation;
        ItemStack bagStack = CardSlotBagItem.getBagStack(playerInventory.player, bagLocation);
        this.bagHandler = bagStack.isEmpty() ? new ItemStackHandler(CardSlotBagItem.BAG_SIZE) : CardSlotBagItem.readHandler(bagStack);

        addBagSlots();
        addPlayerSlots();
    }

    private void addBagSlots() {
        for (int row = 0; row < BAG_ROWS; row++) {
            for (int col = 0; col < BAG_COLS; col++) {
                int slot = col + row * BAG_COLS;
                addSlot(new SlotItemHandler(bagHandler, slot, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return CardSlotBagItem.isSupportedBagItem(stack);
                    }
                });
            }
        }
    }

    private void addPlayerSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < BAG_SLOT_COUNT) {
                if (!moveItemStackTo(stack, BAG_SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!CardSlotBagItem.isSupportedBagItem(stack) || !moveItemStackTo(stack, 0, BAG_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            CardSlotBagItem.writeHandler(player, bagLocation, bagHandler);
        }
    }

    public static class Provider implements net.minecraft.world.MenuProvider {
        private final BagLocation location;

        public Provider(BagLocation location) {
            this.location = location;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.mekccupgrades.card_slot_bag");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new CardSlotBagMenu(id, inventory, location);
        }
    }
}
