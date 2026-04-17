package com.lhy.mekccupgrades.mixin.client;

import com.lhy.mekccupgrades.compat.IPNCompat;
import com.lhy.mekccupgrades.client.gui.element.AttachedBagPanel;
import com.lhy.mekccupgrades.integration.mekanism.BagWindowContainerAccess;
import com.lhy.mekccupgrades.mixin.accessor.GuiElementAccessor;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiUpgradeWindow;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiUpgradeWindow.class, remap = false)
public abstract class GuiUpgradeWindowMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mekccupgrades$addAttachedBagPanel(IGuiWrapper gui, int x, int y, TileEntityMekanism tile, CallbackInfo ci) {
        if (!(gui instanceof GuiMekanism<?> guiMekanism)) {
            return;
        }
        IPNCompat.ignoreScreenForLockOverlay(guiMekanism.getClass());
        if (!(guiMekanism.getMenu() instanceof BagWindowContainerAccess access)) {
            return;
        }
        List<VirtualInventoryContainerSlot> bagSlots = access.mekccupgrades$getBagWindowSlots();
        if (bagSlots.isEmpty()) {
            return;
        }
        GuiElement self = (GuiElement) (Object) this;
        int panelX = self.getRelativeX() - 10;
        int panelY = self.getRelativeY() + self.getHeight() + 2;
        ((GuiElementAccessor) this).mekccupgrades$invokeAddChild(
              new AttachedBagPanel(gui, (IGUIWindow) (Object) this, panelX, panelY, bagSlots)
        );
    }
}
