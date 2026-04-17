package com.lhy.mekccupgrades.integration.mekanism;

import java.util.List;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.world.entity.player.Player;

public interface BagWindowContainerAccess {
    List<VirtualInventoryContainerSlot> mekccupgrades$getBagWindowSlots();

    List<BagWindowInventorySlot> mekccupgrades$getBagBackingSlots();

    void mekccupgrades$saveBagWindow(Player player);
}
