package com.lhy.mekccupgrades.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import com.lhy.mekccupgrades.network.BagWindowActionPacket;
import com.lhy.mekccupgrades.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttachedBagPanel extends GuiElement {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mekccupgrades", "textures/gui/card_slot_bag_panel.png");
    // private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 79;
    /** Hit box slightly larger than {@link UpgradeIconButton#ICON_DRAW_SIZE} so the 7px icon stays centered. */
    private static final int BAG_ACTION_BUTTON_SIZE = 9;
    private static final int BAG_BUTTON_RIGHT_MARGIN = 6;
    private static final int BAG_BUTTON_GAP = 4;

    public AttachedBagPanel(IGuiWrapper gui, IGUIWindow parentWindow, int x, int y, List<VirtualInventoryContainerSlot> slots) {
        super(gui, x, y, PANEL_WIDTH, PANEL_HEIGHT);
        int secondLeft = PANEL_WIDTH - BAG_BUTTON_RIGHT_MARGIN - BAG_ACTION_BUTTON_SIZE;
        int firstLeft = secondLeft - BAG_ACTION_BUTTON_SIZE - BAG_BUTTON_GAP;
        addChild(new UpgradeIconButton(gui, relativeX + firstLeft, relativeY + 4, BAG_ACTION_BUTTON_SIZE, BAG_ACTION_BUTTON_SIZE, false,
              () -> ModNetwork.CHANNEL.sendToServer(new BagWindowActionPacket(BagWindowActionPacket.ActionType.INSERT_SUPPORTED)),
              getOnHover(() -> Component.translatable("tooltip.mekccupgrades.card_slot_bag.insert_supported"))));
        addChild(new UpgradeIconButton(gui, relativeX + secondLeft, relativeY + 4, BAG_ACTION_BUTTON_SIZE, BAG_ACTION_BUTTON_SIZE, true,
              () -> ModNetwork.CHANNEL.sendToServer(new BagWindowActionPacket(BagWindowActionPacket.ActionType.EXTRACT_ALL)),
              getOnHover(() -> Component.translatable("tooltip.mekccupgrades.card_slot_bag.extract_all"))));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                addChild(new AttachedBagVirtualSlot(parentWindow, SlotType.NORMAL, gui, relativeX + 7 + col * 18, relativeY + 17 + row * 18, slots.get(slotIndex)));
            }
        }
    }

    @Override
    public void drawBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(TEXTURE, relativeX, relativeY, 0, 0, width, height);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawTitleText(guiGraphics, Component.translatable("item.mekccupgrades.card_slot_bag"), 6);
    }

    /**
     * Mek-style button with {@code upgrade.png} drawn at 7×7 (source texture 18×18), optional 180° for the extract action.
     */
    private static final class UpgradeIconButton extends MekanismButton {
        private static final ResourceLocation UPGRADE_ICON = ResourceLocation.fromNamespaceAndPath("mekccupgrades", "textures/gui/upgrade.png");
        private static final int ICON_TEX_SIZE = 18;
        private static final int ICON_DRAW_SIZE = 7;
        private final boolean rotate180;

        private UpgradeIconButton(IGuiWrapper gui, int x, int y, int width, int height, boolean rotate180, @NotNull Runnable onLeftClick,
              @Nullable GuiElement.IHoverable onHover) {
            super(gui, x, y, width, height, Component.empty(), onLeftClick, onHover);
            this.rotate180 = rotate180;
        }

        @Override
        public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super.renderForeground(guiGraphics, mouseX, mouseY);
            int ox = getButtonX() + (getButtonWidth() - ICON_DRAW_SIZE) / 2;
            int oy = getButtonY() + (getButtonHeight() - ICON_DRAW_SIZE) / 2;
            MekanismRenderer.resetColor(guiGraphics);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            try {
                if (rotate180) {
                    guiGraphics.pose().pushPose();
                    float cx = ox + ICON_DRAW_SIZE / 2f;
                    float cy = oy + ICON_DRAW_SIZE / 2f;
                    guiGraphics.pose().translate(cx, cy, 0);
                    guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(180));
                    guiGraphics.pose().translate(-cx, -cy, 0);
                    guiGraphics.blit(UPGRADE_ICON, ox, oy, ICON_DRAW_SIZE, ICON_DRAW_SIZE, 0, 0, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE);
                    guiGraphics.pose().popPose();
                } else {
                    guiGraphics.blit(UPGRADE_ICON, ox, oy, ICON_DRAW_SIZE, ICON_DRAW_SIZE, 0, 0, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE);
                }
            } finally {
                RenderSystem.enableDepthTest();
            }
        }
    }
}
