package com.lhy.mekccupgrades;

import com.mojang.logging.LogUtils;
import com.lhy.mekccupgrades.client.MekConfigCardUpgradesClient;
import com.lhy.mekccupgrades.network.ModNetwork;
import com.lhy.mekccupgrades.registration.ModItems;
import com.lhy.mekccupgrades.registration.ModMenuTypes;
import com.lhy.mekccupgrades.registration.ModRecipeSerializers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

@Mod(MekConfigCardUpgradesMod.MOD_ID)
public class MekConfigCardUpgradesMod {
    public static final String MOD_ID = "mekccupgrades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MekConfigCardUpgradesMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::enqueueImc);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenuTypes.MENUS.register(modBus);
        ModNetwork.register();
        MekConfigCardUpgradesEvents.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> MekConfigCardUpgradesClient.init(modBus));
        LOGGER.info("Mekanism Config Card Upgrades loaded");
    }

    private void enqueueImc(InterModEnqueueEvent event) {
        if (!ModList.get().isLoaded("curios")) {
            return;
        }
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
              new SlotTypeMessage.Builder("belt").size(1).build()
        );
    }
}
