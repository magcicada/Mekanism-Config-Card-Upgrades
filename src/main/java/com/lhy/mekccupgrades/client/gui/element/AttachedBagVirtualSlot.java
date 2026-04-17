package com.lhy.mekccupgrades.client.gui.element;

import com.lhy.mekccupgrades.mixin.accessor.AbstractContainerScreenAccessor;
import mekanism.client.gui.VirtualSlotContainerScreen;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.world.inventory.ClickType;

public class AttachedBagVirtualSlot extends GuiVirtualSlot {
    private final VirtualInventoryContainerSlot attachedSlot;

    public AttachedBagVirtualSlot(IGUIWindow window, SlotType type, IGuiWrapper gui, int x, int y, VirtualInventoryContainerSlot containerSlot) {
        super(window, type, gui, x, y, containerSlot);
        this.attachedSlot = containerSlot;
    }

    @Override
    public GuiElement mouseClickedNested(double mouseX, double mouseY, int button) {
        if (mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height) {
            if (gui() instanceof VirtualSlotContainerScreen<?> screen) {
                if (!screen.getMenu().getCarried().isEmpty()) {
                    ((AbstractContainerScreenAccessor) screen).mekccupgrades$invokeSlotClicked(attachedSlot.getSlot(), attachedSlot.getSlot().index, button, ClickType.PICKUP);
                    return this;
                }
            }
        }
        return super.mouseClickedNested(mouseX, mouseY, button);
    }
}
