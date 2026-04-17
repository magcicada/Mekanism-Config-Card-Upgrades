package com.lhy.mekccupgrades.compat;

import com.lhy.mekccupgrades.item.BagLocation;
import com.lhy.mekccupgrades.item.CardSlotBagItem;
import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    @Nullable
    public static BagLocation findFirstBag(Player player) {
        if (!isLoaded()) {
            return null;
        }
        return CuriosApi.getCuriosHelper().findFirstCurio(player, CardSlotBagItem::isBag)
              .map(slotResult -> BagLocation.curios(slotResult.slotContext().identifier(), slotResult.slotContext().index()))
              .orElse(null);
    }

    public static void forEachBag(Player player, Consumer<BagLocation> consumer) {
        if (!isLoaded()) {
            return;
        }
        CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve().ifPresent(handler ->
              handler.getCurios().forEach((identifier, stacksHandler) -> {
                  for (int slot = 0; slot < stacksHandler.getStacks().getSlots(); slot++) {
                      ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);
                      if (CardSlotBagItem.isBag(stack)) {
                          consumer.accept(BagLocation.curios(identifier, slot));
                      }
                  }
              }));
    }

    public static ItemStack getBagStack(Player player, BagLocation location) {
        if (!isLoaded() || location.storageType() != BagLocation.StorageType.CURIOS) {
            return ItemStack.EMPTY;
        }
        return CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve()
              .flatMap(handler -> handler.getStacksHandler(location.curiosIdentifier()))
              .map(stacksHandler -> stacksHandler.getStacks().getStackInSlot(location.slotIndex()))
              .orElse(ItemStack.EMPTY);
    }

    public static boolean setBagStack(Player player, BagLocation location, ItemStack stack) {
        if (!isLoaded() || location.storageType() != BagLocation.StorageType.CURIOS) {
            return false;
        }
        return CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve()
              .flatMap(handler -> handler.getStacksHandler(location.curiosIdentifier()))
              .map(stacksHandler -> {
                  stacksHandler.getStacks().setStackInSlot(location.slotIndex(), stack);
                  return true;
              })
              .orElse(false);
    }
}
