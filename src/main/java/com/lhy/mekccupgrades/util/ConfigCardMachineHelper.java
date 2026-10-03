package com.lhy.mekccupgrades.util;

import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ConfigCardMachineHelper {

    private ConfigCardMachineHelper() {
    }

    public static boolean isResettableMachine(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
              && isResettableMachine(blockItem);
    }

    public static boolean isResettableMachine(BlockItem blockItem) {
        if (!(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
            return false;
        }

        BlockState state = blockItem.getBlock().defaultBlockState();

        try {
            BlockEntity blockEntity =
                  entityBlock.newBlockEntity(BlockPos.ZERO, state);

            return blockEntity instanceof TileEntityMekanism;
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }
}