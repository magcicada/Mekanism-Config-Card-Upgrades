package com.lhy.mekccupgrades.integration.mekanism;

import com.lhy.mekccupgrades.item.CardSlotBagItem;
import mekanism.api.IContentsListener;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BagWindowInventorySlot extends BasicInventorySlot {
    public BagWindowInventorySlot(ItemStack initialStack, @Nullable IContentsListener listener) {
        super(Integer.MAX_VALUE, alwaysTrueBi, (stack, automationType) -> CardSlotBagItem.isSupportedBagItem(stack), CardSlotBagItem::isSupportedBagItem, listener, 0, 0);
        obeyStackLimit = false;
        current = initialStack.copy();
    }

    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, new SelectedWindowData(WindowType.UPGRADE), getSlotOverlay(), this::setStackUnchecked);
    }
}
