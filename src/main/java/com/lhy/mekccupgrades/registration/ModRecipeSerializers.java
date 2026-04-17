package com.lhy.mekccupgrades.registration;

import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import com.lhy.mekccupgrades.recipe.ConfigCardDataClearRecipe;
import com.lhy.mekccupgrades.recipe.ConfigCardResetRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
          DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MekConfigCardUpgradesMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<ConfigCardResetRecipe>> CONFIG_CARD_RESET =
          RECIPE_SERIALIZERS.register("config_card_reset", () -> new SimpleCraftingRecipeSerializer<>(ConfigCardResetRecipe::new));

    public static final RegistryObject<RecipeSerializer<ConfigCardDataClearRecipe>> CONFIG_CARD_DATA_CLEAR =
          RECIPE_SERIALIZERS.register("config_card_data_clear", () -> new SimpleCraftingRecipeSerializer<>(ConfigCardDataClearRecipe::new));

    private ModRecipeSerializers() {
    }
}
