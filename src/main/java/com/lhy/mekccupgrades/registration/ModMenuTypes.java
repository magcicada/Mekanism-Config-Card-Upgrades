package com.lhy.mekccupgrades.registration;

import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import com.lhy.mekccupgrades.menu.CardSlotBagMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MekConfigCardUpgradesMod.MOD_ID);

    public static final RegistryObject<MenuType<CardSlotBagMenu>> CARD_SLOT_BAG_MENU = MENUS.register("card_slot_bag",
          () -> IForgeMenuType.create(CardSlotBagMenu::new));

    private ModMenuTypes() {
    }
}
