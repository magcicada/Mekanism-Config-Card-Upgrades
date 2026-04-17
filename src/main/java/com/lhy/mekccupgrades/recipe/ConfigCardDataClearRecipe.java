package com.lhy.mekccupgrades.recipe;

import com.lhy.mekccupgrades.registration.ModRecipeSerializers;
import mekanism.api.NBTConstants;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ConfigCardDataClearRecipe extends CustomRecipe {
    public ConfigCardDataClearRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, @NotNull Level level) {
        return !getConfigCard(inv).isEmpty();
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingContainer inv, @NotNull RegistryAccess registryAccess) {
        ItemStack configCard = getConfigCard(inv);
        if (configCard.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = configCard.copyWithCount(1);
        ItemDataUtils.removeData(result, NBTConstants.DATA);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CONFIG_CARD_DATA_CLEAR.get();
    }

    private static ItemStack getConfigCard(CraftingContainer inv) {
        ItemStack configCard = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackInSlot = inv.getItem(i);
            if (stackInSlot.isEmpty()) {
                continue;
            }
            if (!(stackInSlot.getItem() instanceof ItemConfigurationCard) || !ItemDataUtils.getCompound(stackInSlot, NBTConstants.DATA).contains(NBTConstants.DATA_NAME)) {
                return ItemStack.EMPTY;
            }
            if (!configCard.isEmpty()) {
                return ItemStack.EMPTY;
            }
            configCard = stackInSlot;
        }
        return configCard;
    }
}
