package com.lhy.mekccupgrades.recipe;

import com.lhy.mekccupgrades.registration.ModRecipeSerializers;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ConfigCardResetRecipe extends CustomRecipe {
    public ConfigCardResetRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, @NotNull Level level) {
        return !getMachineStack(inv).isEmpty() && !getConfigCardStack(inv).isEmpty();
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingContainer inv, @NotNull RegistryAccess registryAccess) {
        ItemStack machineStack = getMachineStack(inv);
        if (machineStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(machineStack.getItem(), 1);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.getItem() instanceof ItemConfigurationCard) {
                remainingItems.set(i, stackInSlot.copy());
                break;
            }
        }
        return remainingItems;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CONFIG_CARD_RESET.get();
    }

    private static ItemStack getMachineStack(CraftingContainer inv) {
        ItemStack machineStack = ItemStack.EMPTY;
        ItemStack configCard = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }
            if (stackInSlot.getItem() instanceof ItemConfigurationCard) {
                if (!configCard.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                configCard = stackInSlot;
            } else if (isResettableMachine(stackInSlot)) {
                if (!machineStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                machineStack = stackInSlot;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return machineStack.isEmpty() || configCard.isEmpty() ? ItemStack.EMPTY : machineStack;
    }

    private static ItemStack getConfigCardStack(CraftingContainer inv) {
        ItemStack configCard = ItemStack.EMPTY;
        ItemStack machineStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }
            if (stackInSlot.getItem() instanceof ItemConfigurationCard) {
                if (!configCard.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                configCard = stackInSlot;
            } else if (isResettableMachine(stackInSlot)) {
                if (!machineStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                machineStack = stackInSlot;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return machineStack.isEmpty() || configCard.isEmpty() ? ItemStack.EMPTY : configCard;
    }

    private static boolean isResettableMachine(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        BlockState defaultState = blockItem.getBlock().defaultBlockState();
        if (!(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
            return false;
        }
        BlockEntity tile = entityBlock.newBlockEntity(BlockPos.ZERO, defaultState);
        return tile instanceof TileEntityMekanism;
    }
}
