package com.lhy.mekccupgrades.mixin;

import com.lhy.mekccupgrades.integration.mekanism.BagWindowContainerAccess;
import com.lhy.mekccupgrades.integration.mekanism.BagWindowInventorySlot;
import com.lhy.mekccupgrades.item.BagLocation;
import com.lhy.mekccupgrades.item.CardSlotBagItem;
import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MekanismTileContainer.class, remap = false)
public abstract class MekanismTileContainerMixin extends MekanismContainer implements BagWindowContainerAccess {
    @Shadow
    @Final
    protected TileEntityMekanism tile;

    @Unique
    private final List<VirtualInventoryContainerSlot> mekccupgrades$bagSlots = new ArrayList<>();
    @Unique
    private final List<BagWindowInventorySlot> mekccupgrades$backingBagSlots = new ArrayList<>();
    @Unique
    private BagLocation mekccupgrades$bagLocation;

    protected MekanismTileContainerMixin(ContainerTypeRegistryObject<?> type, int id, Inventory inv) {
        super(type, id, inv);
    }

    @Inject(method = "addSlots", at = @At("TAIL"))
    private void mekccupgrades$addBagSlots(CallbackInfo ci) {
        mekccupgrades$bagSlots.clear();
        mekccupgrades$backingBagSlots.clear();
        mekccupgrades$bagLocation = null;
        if (!tile.supportsUpgrades()) {
            return;
        }
        BagLocation bagLocation = CardSlotBagItem.findFirstBagLocation(inv.player);
        if (bagLocation == null) {
            return;
        }
        ItemStack bagStack = CardSlotBagItem.getBagStack(inv.player, bagLocation);
        if (bagStack.isEmpty()) {
            return;
        }
        mekccupgrades$bagLocation = bagLocation;
        ItemStackHandler handler = CardSlotBagItem.readHandler(bagStack);
        for (int i = 0; i < CardSlotBagItem.BAG_SIZE; i++) {
            BagWindowInventorySlot backingSlot = new BagWindowInventorySlot(handler.getStackInSlot(i), null);
            mekccupgrades$backingBagSlots.add(backingSlot);
            VirtualInventoryContainerSlot containerSlot = backingSlot.createContainerSlot();
            mekccupgrades$bagSlots.add(containerSlot);
            addSlot(containerSlot);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void mekccupgrades$saveBagOnClose(Player player, CallbackInfo ci) {
        mekccupgrades$saveBagWindow(player);
    }

    @Override
    public List<VirtualInventoryContainerSlot> mekccupgrades$getBagWindowSlots() {
        return mekccupgrades$bagSlots;
    }

    @Override
    public List<BagWindowInventorySlot> mekccupgrades$getBagBackingSlots() {
        return mekccupgrades$backingBagSlots;
    }

    @Override
    public void mekccupgrades$saveBagWindow(Player player) {
        if (player.level().isClientSide() || mekccupgrades$bagLocation == null || mekccupgrades$backingBagSlots.isEmpty()) {
            return;
        }
        ItemStackHandler handler = new ItemStackHandler(CardSlotBagItem.BAG_SIZE);
        for (int i = 0; i < mekccupgrades$backingBagSlots.size(); i++) {
            handler.setStackInSlot(i, mekccupgrades$backingBagSlots.get(i).getStack().copy());
        }
        CardSlotBagItem.writeHandler(player, mekccupgrades$bagLocation, handler);
    }
}
