package com.lhy.mekccupgrades.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.platform.InputConstants;
import com.lhy.mekccupgrades.ConfigCardUpgradeHelper;
import com.lhy.mekccupgrades.client.screen.CardSlotBagScreen;
import com.lhy.mekccupgrades.network.BatchPastePacket;
import com.lhy.mekccupgrades.network.ConfigCardBlockActionPacket;
import com.lhy.mekccupgrades.network.FillSupportedUpgradesPacket;
import com.lhy.mekccupgrades.network.ModNetwork;
import com.lhy.mekccupgrades.network.TogglePasteModePacket;
import com.lhy.mekccupgrades.registration.ModMenuTypes;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.client.key.MekKeyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import org.jetbrains.annotations.Nullable;

public final class MekConfigCardUpgradesClient {
    public static final KeyMapping BATCH_PASTE_KEY = new KeyMapping(
          "key.mekccupgrades.batch_paste",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_LEFT_CONTROL,
          "key.categories.mekccupgrades"
    );

    private static BlockPos firstCorner;
    private static boolean attackKeyWasDown;

    private MekConfigCardUpgradesClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(MekConfigCardUpgradesClient::registerKeyMappings);
        modBus.addListener(MekConfigCardUpgradesClient::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesClient::onMouseButtonPre);
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesClient::onInteractionKeyMappingTriggered);
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesClient::onLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesClient::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(MekConfigCardUpgradesClient::onRenderLevel);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.CARD_SLOT_BAG_MENU.get(), CardSlotBagScreen::new));
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BATCH_PASTE_KEY);
    }

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        ItemStack stack = event.getEntity().getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof ItemConfigurationCard)) {
            return;
        }
        if (event.getEntity().isShiftKeyDown()) {
            return;
        }
        if (!ConfigCardUpgradeHelper.canUseBatchMode(stack)) {
            return;
        }
        event.setCanceled(true);
    }

    private static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        if (minecraft.player.isShiftKeyDown()) {
            return;
        }
        InteractionHand hand = getHeldConfigCardHand(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND), minecraft.player.getItemInHand(InteractionHand.OFF_HAND));
        if (hand == null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult blockHitResult) || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!minecraft.level.getWorldBorder().isWithinBounds(blockHitResult.getBlockPos())) {
            return;
        }
        Direction face = blockHitResult.getDirection() == null ? Direction.UP : blockHitResult.getDirection();
        ModNetwork.CHANNEL.sendToServer(new FillSupportedUpgradesPacket(blockHitResult.getBlockPos().immutable(), face, hand));
        event.setCanceled(true);
    }

    private static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        ItemStack held = getHeldConfigCard(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND), minecraft.player.getItemInHand(InteractionHand.OFF_HAND));
        if (held.isEmpty() || !ConfigCardUpgradeHelper.canUseBatchMode(held)) {
            return;
        }
        if (!minecraft.player.getAbilities().instabuild) {
            return;
        }
        if (minecraft.player.isShiftKeyDown()) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult blockHitResult) || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (!minecraft.level.getWorldBorder().isWithinBounds(blockHitResult.getBlockPos())) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean attackDown = MekKeyHandler.isKeyPressed(minecraft.options.keyAttack);
        if (!attackDown) {
            attackKeyWasDown = false;
            return;
        }
        if (attackKeyWasDown) {
            return;
        }
        attackKeyWasDown = true;
        if (minecraft.screen != null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        InteractionHand hand = getHeldConfigCardHand(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND), minecraft.player.getItemInHand(InteractionHand.OFF_HAND));
        if (hand == null) {
            return;
        }
        HitResult hitResult = minecraft.hitResult;
        if (BATCH_PASTE_KEY.isDown()) {
            ItemStack held = minecraft.player.getItemInHand(hand);
            if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK && ConfigCardUpgradeHelper.canUseBatchMode(held)) {
                if (firstCorner == null) {
                    firstCorner = blockHitResult.getBlockPos().immutable();
                    String action = ConfigCardUpgradeHelper.isClearMode(held) ? "批量清除"
                          : ConfigCardUpgradeHelper.isFactoryMode(held) ? "批量工厂升级"
                          : "批量粘贴";
                    minecraft.player.sendSystemMessage(Component.literal(action + "已选择第一点：" + firstCorner.getX() + ", " + firstCorner.getY() + ", " + firstCorner.getZ()));
                } else {
                    Direction face = blockHitResult.getDirection() == null ? Direction.UP : blockHitResult.getDirection();
                    ModNetwork.CHANNEL.sendToServer(new BatchPastePacket(firstCorner, blockHitResult.getBlockPos().immutable(), face, hand));
                    firstCorner = null;
                }
            } else if (firstCorner != null) {
                firstCorner = null;
                minecraft.player.sendSystemMessage(Component.literal("已清除当前批量粘贴选区"));
            }
            return;
        }
        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            ItemStack held = minecraft.player.getItemInHand(hand);
            if (!minecraft.player.isShiftKeyDown() && ConfigCardUpgradeHelper.canUseBatchMode(held)) {
                Direction face = blockHitResult.getDirection() == null ? Direction.UP : blockHitResult.getDirection();
                ModNetwork.CHANNEL.sendToServer(new ConfigCardBlockActionPacket(blockHitResult.getBlockPos().immutable(), face, hand));
            }
            return;
        }
        if (minecraft.player.isShiftKeyDown()) {
            ModNetwork.CHANNEL.sendToServer(new TogglePasteModePacket(hand));
        }
    }

    private static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || firstCorner == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            firstCorner = null;
            return;
        }
        ItemStack stack = getHeldConfigCard(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND), minecraft.player.getItemInHand(InteractionHand.OFF_HAND));
        if (stack.isEmpty()) {
            firstCorner = null;
            return;
        }
        BlockPos secondCorner = getPreviewCorner(minecraft);
        if (secondCorner == null) {
            secondCorner = firstCorner;
        }
        Vec3 cameraPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        double minX = Math.min(firstCorner.getX(), secondCorner.getX());
        double minY = Math.min(firstCorner.getY(), secondCorner.getY());
        double minZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        double maxX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
        double maxY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
        double maxZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;
        event.getPoseStack().pushPose();
        event.getPoseStack().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        LevelRenderer.renderLineBox(event.getPoseStack(), buffer, minX, minY, minZ, maxX, maxY, maxZ, 1F, 0.2F, 0.2F, 1F);
        event.getPoseStack().popPose();
        bufferSource.endBatch(RenderType.lines());
    }

    private static BlockPos getPreviewCorner(Minecraft minecraft) {
        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            return blockHitResult.getBlockPos();
        }
        return null;
    }

    private static ItemStack getHeldConfigCard(ItemStack mainHand, ItemStack offHand) {
        if (mainHand.getItem() instanceof ItemConfigurationCard && ConfigCardUpgradeHelper.canUseBatchMode(mainHand)) {
            return mainHand;
        }
        if (offHand.getItem() instanceof ItemConfigurationCard && ConfigCardUpgradeHelper.canUseBatchMode(offHand)) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    private static InteractionHand getHeldConfigCardHand(ItemStack mainHand, ItemStack offHand) {
        if (mainHand.getItem() instanceof ItemConfigurationCard) {
            return InteractionHand.MAIN_HAND;
        }
        if (offHand.getItem() instanceof ItemConfigurationCard) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
