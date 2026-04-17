package com.lhy.mekccupgrades.mixin.accessor;

import mekanism.client.gui.element.GuiElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GuiElement.class, remap = false)
public interface GuiElementAccessor {
    @Invoker("addChild")
    <ELEMENT extends GuiElement> ELEMENT mekccupgrades$invokeAddChild(ELEMENT element);
}
