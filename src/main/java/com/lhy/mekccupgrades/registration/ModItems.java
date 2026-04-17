package com.lhy.mekccupgrades.registration;

import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import com.lhy.mekccupgrades.item.CardSlotBagItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MekConfigCardUpgradesMod.MOD_ID);

    public static final RegistryObject<Item> CARD_SLOT_BAG = ITEMS.register("card_slot_bag",
          () -> new CardSlotBagItem(new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
