package com.lhy.mekccupgrades.compat.jei;

import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import com.lhy.mekccupgrades.recipe.ConfigCardDataClearRecipe;
import com.lhy.mekccupgrades.recipe.ConfigCardResetRecipe;
import com.lhy.mekccupgrades.registration.ModItems;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class MekConfigCardUpgradesJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(MekConfigCardUpgradesMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(List.of(new ItemStack(ModItems.CARD_SLOT_BAG.get())));
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(ConfigCardResetRecipe.class, ConfigCardResetRecipeExtension::new);
        registration.getCraftingCategory().addCategoryExtension(ConfigCardDataClearRecipe.class, ConfigCardDataClearRecipeExtension::new);
    }
}
