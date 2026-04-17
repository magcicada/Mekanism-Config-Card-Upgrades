package com.lhy.mekccupgrades.compat.jei;

import com.lhy.mekccupgrades.recipe.ConfigCardDataClearRecipe;
import mekanism.api.NBTConstants;
import mekanism.common.registries.MekanismItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ConfigCardDataClearRecipeExtension implements ICraftingCategoryExtension {
    private final ConfigCardDataClearRecipe recipe;

    public ConfigCardDataClearRecipeExtension(ConfigCardDataClearRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        craftingGridHelper.createAndSetInputs(builder, java.util.List.of(java.util.List.of(createEncodedConfigCard())), 0, 0);
        craftingGridHelper.createAndSetOutputs(builder, java.util.List.of(new ItemStack(MekanismItems.CONFIGURATION_CARD.get())));
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getId();
    }

    private static ItemStack createEncodedConfigCard() {
        ItemStack stack = new ItemStack(MekanismItems.CONFIGURATION_CARD.get());
        CompoundTag data = new CompoundTag();
        data.putString(NBTConstants.DATA_NAME, "Sample");
        stack.getOrCreateTag().put(NBTConstants.DATA, data);
        return stack;
    }
}
