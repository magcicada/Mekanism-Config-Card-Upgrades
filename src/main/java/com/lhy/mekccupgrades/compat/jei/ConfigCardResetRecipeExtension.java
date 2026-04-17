package com.lhy.mekccupgrades.compat.jei;

import com.lhy.mekccupgrades.recipe.ConfigCardResetRecipe;
import java.util.List;
import mekanism.common.item.ItemConfigurationCard;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class ConfigCardResetRecipeExtension implements ICraftingCategoryExtension {
    private static final List<ItemStack> RESETTABLE_MACHINE_ITEMS = ForgeRegistries.ITEMS.getValues().stream()
          .filter(item -> item instanceof BlockItem blockItem && isResettableMachine(blockItem))
          .map(ItemStack::new)
          .toList();

    private final ConfigCardResetRecipe recipe;

    public ConfigCardResetRecipeExtension(ConfigCardResetRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        craftingGridHelper.createAndSetInputs(builder, List.of(
              List.of(new ItemStack(mekanism.common.registries.MekanismItems.CONFIGURATION_CARD.get())),
              RESETTABLE_MACHINE_ITEMS
        ), 0, 0);
        craftingGridHelper.createAndSetOutputs(builder, RESETTABLE_MACHINE_ITEMS);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getId();
    }

    private static boolean isResettableMachine(BlockItem blockItem) {
        BlockState defaultState = blockItem.getBlock().defaultBlockState();
        if (!(blockItem.getBlock() instanceof EntityBlock entityBlock)) {
            return false;
        }
        BlockEntity blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, defaultState);
        return blockEntity instanceof mekanism.common.tile.base.TileEntityMekanism;
    }
}
