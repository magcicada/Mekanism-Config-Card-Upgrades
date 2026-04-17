package com.lhy.mekccupgrades;

import com.lhy.mekccupgrades.item.CardSlotBagItem;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.IConfigCardAccess;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class ConfigCardUpgradeHelper {
    public static final String UPGRADE_COPY_KEY = "mekccupgrades";
    public static final String FACTORY_TIER_COPY_KEY = "mekccupgrades_factory_tier";
    public static final String EXTRA_FACTORY_TIER_COPY_KEY = "mekccupgrades_extra_factory_tier";
    public static final String PASTE_MODE_KEY = "mekccupgrades_paste_mode";
    public static final String FUZZY_MODE_KEY = "mekccupgrades_fuzzy_mode";
    private static final String[] EXTRA_ATTRIBUTE_UPGRADEABLE_CLASSES = {
          "com.jerry.mekanism_extras.common.block.attribute.ExtraAttributeUpgradeable",
          "com.jerry.mekextras.common.block.attribute.ExtraAttributeUpgradeable"
    };
    private static final String[] EXTRA_ATTRIBUTE_CLASSES = {
          "com.jerry.mekanism_extras.common.block.attribute.ExtraAttribute",
          "com.jerry.mekextras.common.block.attribute.ExtraAttribute"
    };
    private static final String[] EXTRA_ITEM_TIER_INSTALLER_CLASSES = {
          "com.jerry.mekanism_extras.common.item.ExtraItemTierInstaller",
          "com.jerry.mekextras.common.item.ExtraItemTierInstaller"
    };
    private static final List<String> EXTRA_TIER_NAMES = List.of("absolute", "supreme", "cosmic", "infinite");
    private static final String MORE_MACHINE_FACTORY_TYPE = "com.jerry.mekmm.common.block.attribute.AttributeMoreMachineFactoryType";
    private static final String ADVANCED_FACTORY_TYPE = "com.jerry.mekaf.common.block.attribute.AttributeAdvancedFactoryType";
    private static final String MORE_MACHINE_NAMESPACE = "mekmm";
    private static final List<String> RECIPE_FACTORY_TIER_ORDER = List.of(
          "basic", "advanced", "elite", "ultimate",
          "overclocked", "quantum", "dense", "multiversal", "creative"
    );
    private static final Map<String, String> RECIPE_FACTORY_BASE_BLOCKS = Map.ofEntries(
          Map.entry("oxidizing", "chemical_oxidizer"),
          Map.entry("chemical_infusing", "chemical_infuser"),
          Map.entry("dissolving", "chemical_dissolution_chamber"),
          Map.entry("washing", "chemical_washer"),
          Map.entry("crystallizing", "chemical_crystallizer"),
          Map.entry("pressurised_reacting", "pressurized_reaction_chamber"),
          Map.entry("centrifuging", "isotopic_centrifuge"),
          Map.entry("liquifying", "nutritional_liquifier"),
          Map.entry("recycling", "recycler"),
          Map.entry("planting", "planting_station"),
          Map.entry("stamping", "cnc_stamper"),
          Map.entry("lathing", "cnc_lathe"),
          Map.entry("rolling_mill", "cnc_rolling_mill"),
          Map.entry("replicating", "replicator")
    );
    private static final Map<String, FactoryRecipeMaterials> RECIPE_FACTORY_MATERIALS = Map.ofEntries(
          Map.entry("basic", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/iron"},
                new String[]{"mekanism:alloys/basic"},
                new String[]{"forge:circuits/basic"}
          )),
          Map.entry("advanced", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/osmium"},
                new String[]{"mekanism:alloys/infused"},
                new String[]{"forge:circuits/advanced"}
          )),
          Map.entry("elite", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/gold"},
                new String[]{"mekanism:alloys/reinforced"},
                new String[]{"forge:circuits/elite"}
          )),
          Map.entry("ultimate", new FactoryRecipeMaterials(
                new String[]{"forge:gems/diamond"},
                new String[]{"mekanism:alloys/atomic"},
                new String[]{"forge:circuits/ultimate"}
          )),
          Map.entry("overclocked", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/uranium"},
                new String[]{"forge:alloys/overclocked"},
                new String[]{"forge:circuits/overclocked"}
          )),
          Map.entry("quantum", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/tin"},
                new String[]{"forge:alloys/quantum"},
                new String[]{"forge:circuits/quantum"}
          )),
          Map.entry("dense", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/bronze"},
                new String[]{"forge:alloys/dense"},
                new String[]{"forge:circuits/dense"}
          )),
          Map.entry("multiversal", new FactoryRecipeMaterials(
                new String[]{"forge:ingots/netherite"},
                new String[]{"forge:alloys/multiversal"},
                new String[]{"forge:circuits/multiversal"}
          )),
          Map.entry("creative", new FactoryRecipeMaterials(
                new String[]{"forge:nether_stars"},
                new String[]{"evolvedmekanism:alloys/creative", "forge:alloys/creative"},
                new String[]{"forge:circuits/creative"}
          ))
    );

    public enum PasteMode {
        PRECISE("精准模式", EnumColor.AQUA),
        FUZZY("模糊模式", EnumColor.AQUA),
        CLEAR("清除模式", EnumColor.RED),
        FACTORY("工厂模式", EnumColor.AQUA);

        private final String displayName;
        private final EnumColor messageColor;

        PasteMode(String displayName, EnumColor messageColor) {
            this.displayName = displayName;
            this.messageColor = messageColor;
        }

        public String displayName() {
            return displayName;
        }

        public EnumColor messageColor() {
            return messageColor;
        }

        public PasteMode next() {
            return switch (this) {
                case PRECISE -> FUZZY;
                case FUZZY -> CLEAR;
                case CLEAR -> FACTORY;
                case FACTORY -> PRECISE;
            };
        }

        public static PasteMode byIndex(int index) {
            PasteMode[] values = values();
            if (index < 0 || index >= values.length) {
                return PRECISE;
            }
            return values[index];
        }
    }

    private ConfigCardUpgradeHelper() {
    }

    public static CompoundTag appendUpgradeData(IUpgradeTile tile, CompoundTag data) {
        if (!tile.supportsUpgrades()) {
            return data;
        }
        CompoundTag upgradeData = new CompoundTag();
        Upgrade.saveMap(getInstalledUpgrades(tile.getComponent()), upgradeData);
        data.put(UPGRADE_COPY_KEY, upgradeData);
        return data;
    }

    public static CompoundTag appendFactoryData(BlockEntity tile, CompoundTag data) {
        BlockState state = tile.getBlockState();
        BaseTier standardTier = getCurrentStandardFactoryTier(state);
        String extraTier = getCurrentExtraFactoryTierSerialized(tile);
        if (extraTier != null) {
            data.putString(EXTRA_FACTORY_TIER_COPY_KEY, extraTier);
            data.remove(FACTORY_TIER_COPY_KEY);
        } else if (standardTier != null && isStandardFactory(state)) {
            data.putString(FACTORY_TIER_COPY_KEY, standardTier == null ? BaseTier.BASIC.getSerializedName() : standardTier.getSerializedName());
            data.remove(EXTRA_FACTORY_TIER_COPY_KEY);
        } else {
            data.remove(FACTORY_TIER_COPY_KEY);
            data.remove(EXTRA_FACTORY_TIER_COPY_KEY);
        }
        MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] copy infer tile={} standardTier={} extraTier={} dataName={} dataType={} factoryKey={} extraFactoryKey={}",
              describeTile(tile), standardTier == null ? "null" : standardTier.getSerializedName(), extraTier,
              data.getString(NBTConstants.DATA_NAME), data.getString(NBTConstants.DATA_TYPE),
              data.contains(FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(FACTORY_TIER_COPY_KEY) : "<none>",
              data.contains(EXTRA_FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(EXTRA_FACTORY_TIER_COPY_KEY) : "<none>");
        return data;
    }

    public static boolean hasUpgradeData(CompoundTag data) {
        return data.contains(UPGRADE_COPY_KEY);
    }

    public static boolean hasFactoryData(CompoundTag data) {
        return data.contains(FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) || data.contains(EXTRA_FACTORY_TIER_COPY_KEY, Tag.TAG_STRING);
    }

    @Nullable
    public static BaseTier getStoredFactoryTier(CompoundTag data) {
        if (data.contains(FACTORY_TIER_COPY_KEY, Tag.TAG_STRING)) {
            String serializedName = data.getString(FACTORY_TIER_COPY_KEY);
            for (BaseTier tier : BaseTier.values()) {
                if (tier.getSerializedName().equals(serializedName)) {
                    return tier;
                }
            }
        }
        String dataName = getStoredDataName(data);
        if (dataName != null) {
            for (BaseTier tier : BaseTier.values()) {
                if (dataName.contains(tier.getSerializedName())) {
                    return tier;
                }
            }
        }
        return null;
    }

    @Nullable
    public static String getStoredExtraFactoryTier(CompoundTag data) {
        if (data.contains(EXTRA_FACTORY_TIER_COPY_KEY, Tag.TAG_STRING)) {
            return data.getString(EXTRA_FACTORY_TIER_COPY_KEY);
        }
        BlockEntityType<?> storedType = getStoredTileType(data);
        if (storedType == null) {
            return null;
        }
        ResourceLocation key = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(storedType);
        if (key == null) {
            return inferExtraTierFromString(getStoredDataName(data));
        }
        String inferred = inferExtraTierFromString(key.getPath());
        if (inferred != null) {
            return inferred;
        }
        String dataName = getStoredDataName(data);
        return inferExtraTierFromString(dataName);
    }

    public static CompoundTag getConfigurationOnlyData(CompoundTag data) {
        CompoundTag copy = data.copy();
        copy.remove(UPGRADE_COPY_KEY);
        copy.remove(FACTORY_TIER_COPY_KEY);
        copy.remove(EXTRA_FACTORY_TIER_COPY_KEY);
        return copy;
    }

    public static boolean hasCardData(ItemStack stack) {
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        return !data.isEmpty() && (getStoredTileType(data) != null || hasUpgradeData(data) || hasFactoryData(data));
    }

    public static boolean canUseBatchMode(ItemStack stack) {
        return isClearMode(stack) || hasCardData(stack);
    }

    public static PasteMode getPasteMode(ItemStack stack) {
        CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.contains(PASTE_MODE_KEY, Tag.TAG_INT)) {
            return PasteMode.byIndex(dataMap.getInt(PASTE_MODE_KEY));
        }
        return ItemDataUtils.getBoolean(stack, FUZZY_MODE_KEY) ? PasteMode.FUZZY : PasteMode.PRECISE;
    }

    public static boolean isFuzzyMode(ItemStack stack) {
        return getPasteMode(stack) == PasteMode.FUZZY;
    }

    public static boolean isClearMode(ItemStack stack) {
        return getPasteMode(stack) == PasteMode.CLEAR;
    }

    public static boolean isFactoryMode(ItemStack stack) {
        return getPasteMode(stack) == PasteMode.FACTORY;
    }

    public static PasteMode togglePasteMode(ItemStack stack) {
        PasteMode nextMode = getPasteMode(stack).next();
        ItemDataUtils.setInt(stack, PASTE_MODE_KEY, nextMode.ordinal());
        return nextMode;
    }

    public static void logCardState(String stage, ItemStack stack) {
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
        MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] {} mode={} dataName={} dataType={} standardFactory={} extraFactory={} hasUpgradeData={} dataMapKeys={}",
              stage, getPasteMode(stack).name(),
              data.contains(NBTConstants.DATA_NAME, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_NAME) : "<none>",
              data.contains(NBTConstants.DATA_TYPE, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_TYPE) : "<none>",
              data.contains(FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(FACTORY_TIER_COPY_KEY) : "<none>",
              data.contains(EXTRA_FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(EXTRA_FACTORY_TIER_COPY_KEY) : "<none>",
              hasUpgradeData(data), dataMap == null ? "<none>" : dataMap.getAllKeys().toString());
    }

    public static Component getModeSwitchMessage(PasteMode mode) {
        return Component.literal("配置卡已切换为" + mode.displayName());
    }

    public static boolean canStackCleanFactory(ItemStack stack) {
        CompoundTag dataMap = ItemDataUtils.getDataMapIfPresent(stack);
        return dataMap == null || dataMap.isEmpty();
    }

    @Nullable
    public static BlockEntityType<?> getStoredTileType(CompoundTag data) {
        if (!data.contains(NBTConstants.DATA_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        ResourceLocation tileRegistryName = ResourceLocation.tryParse(data.getString(NBTConstants.DATA_TYPE));
        return tileRegistryName == null ? null : ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(tileRegistryName);
    }

    @Nullable
    private static String getStoredDataName(CompoundTag data) {
        return data.contains(NBTConstants.DATA_NAME, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_NAME) : null;
    }

    /**
     * Mekanism's default {@link IConfigCardAccess#isConfigurationDataCompatible} only allows the exact same
     * {@link BlockEntityType}. Addon factories (e.g. More Machine quantum tiers) use different registry IDs for the same
     * machine line as base Mekanism; normalize paths so chemical_infuser matches quantum_chemical_infusing_factory, etc.
     */
    private static String normalizeMachineFamilyPath(String path) {
        String p = path.toLowerCase(Locale.ROOT);
        String[] tierPrefixes = {
              "quantum_", "ultimate_", "overclocked_", "infinite_", "absolute_", "cosmic_", "supreme_",
              "elite_", "advanced_", "basic_"
        };
        boolean changed;
        do {
            changed = false;
            for (String pre : tierPrefixes) {
                if (p.startsWith(pre)) {
                    p = p.substring(pre.length());
                    changed = true;
                    break;
                }
            }
        } while (changed);
        if (p.endsWith("_infusing_factory")) {
            p = p.replace("_infusing_factory", "_infuser");
        } else if (p.endsWith("_factory")) {
            p = p.substring(0, p.length() - "_factory".length());
        }
        return p;
    }

    private static boolean isConfigurationCompatibleForPaste(IConfigCardAccess access, BlockEntityType<?> storedType) {
        if (access.isConfigurationDataCompatible(storedType)) {
            return true;
        }
        BlockEntityType<?> targetType = access.getConfigurationDataType();
        ResourceLocation storedId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(storedType);
        ResourceLocation targetId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(targetType);
        if (storedId == null || targetId == null) {
            return false;
        }
        return normalizeMachineFamilyPath(storedId.getPath()).equals(normalizeMachineFamilyPath(targetId.getPath()));
    }

    @Nullable
    public static Component pasteCardToTarget(Player player, BlockEntity tile, Direction side, ItemStack stack, boolean sendSuccessMessage) {
        if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
            return Component.literal("粘贴失败，无法访问目标");
        }
        if (isClearMode(stack)) {
            return clearUpgradesFromTarget(player, tile, sendSuccessMessage);
        }
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        if (data.isEmpty()) {
            return Component.literal("粘贴失败，配置卡没有保存数据");
        }
        if (isFactoryMode(stack)) {
            MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] paste factory target={} storedName={} storedType={} storedStandardFactory={} resolvedStandardFactory={} storedExtraFactory={} resolvedExtraFactory={}",
                  describeTile(tile),
                  data.contains(NBTConstants.DATA_NAME, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_NAME) : "<none>",
                  data.contains(NBTConstants.DATA_TYPE, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_TYPE) : "<none>",
                  data.contains(FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(FACTORY_TIER_COPY_KEY) : "<none>",
                  getStoredFactoryTier(data) == null ? "null" : getStoredFactoryTier(data).getSerializedName(),
                  data.contains(EXTRA_FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(EXTRA_FACTORY_TIER_COPY_KEY) : "<none>",
                  getStoredExtraFactoryTier(data));
            return applyFactoryTierToTarget(player, tile, stack, sendSuccessMessage);
        }
        if (isFuzzyMode(stack)) {
            if (!hasUpgradeData(data)) {
                return Component.literal("粘贴失败，配置卡没有保存升级数据");
            }
            if (!(tile instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades()) {
                return Component.literal("粘贴失败，目标不支持升级");
            }
            List<Upgrade> unsupported = getUnsupportedDesiredUpgrades(upgradeTile.getComponent(), getStoredUpgrades(data));
            Component failure = validateFuzzyPaste(player, upgradeTile, data);
            if (failure != null) {
                return failure;
            }
            applyFuzzyStoredUpgrades(player, upgradeTile, data);
            if (sendSuccessMessage) {
                player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, Component.literal("模糊模式粘贴成功")));
                if (!unsupported.isEmpty()) {
                    player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.YELLOW, buildUnsupportedUpgradeMessage(tile, unsupported)));
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger(serverPlayer, false);
                }
            }
            return null;
        }
        Optional<IConfigCardAccess> configCardSupport = CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD, side).resolve();
        if (configCardSupport.isEmpty()) {
            return Component.literal("粘贴失败，目标不支持配置卡");
        }
        BlockEntityType<?> storedType = getStoredTileType(data);
        if (storedType == null) {
            return Component.literal("粘贴失败，配置卡数据无效");
        }
        IConfigCardAccess configCardAccess = configCardSupport.get();
        boolean compatibleBeforeFactory = isConfigurationCompatibleForPaste(configCardAccess, storedType);
        if (hasFactoryData(data)) {
            // Precise mode should only apply factory tier for compatible machine families.
            if (!compatibleBeforeFactory) {
                return MekanismLang.CONFIG_CARD_UNEQUAL.translate();
            }
            Component factoryFailure = applyFactoryTierToTarget(player, tile, stack, false);
            if (factoryFailure != null) {
                return factoryFailure;
            }
            BlockEntity refreshed = player.level().getBlockEntity(tile.getBlockPos());
            if (refreshed == null) {
                return Component.literal("粘贴失败，目标机器不存在");
            }
            tile = refreshed;
            configCardSupport = CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD, side).resolve();
            if (configCardSupport.isEmpty()) {
                return Component.literal("粘贴失败，目标不支持配置卡");
            }
            configCardAccess = configCardSupport.get();
        }
        if (!isConfigurationCompatibleForPaste(configCardAccess, storedType)) {
            return MekanismLang.CONFIG_CARD_UNEQUAL.translate();
        }
        IUpgradeTile upgradeTile = tile instanceof IUpgradeTile typedUpgradeTile && typedUpgradeTile.supportsUpgrades() ? typedUpgradeTile : null;
        if (hasUpgradeData(data)) {
            if (upgradeTile == null) {
                return Component.literal("粘贴失败，目标不支持升级");
            }
            Component failure = validatePaste(player, upgradeTile, data);
            if (failure != null) {
                return failure;
            }
        }
        configCardAccess.setConfigurationData(player, getConfigurationOnlyData(data));
        configCardAccess.configurationDataSet();
        if (hasUpgradeData(data) && upgradeTile != null) {
            consumeUpgradeItems(player, upgradeTile, data);
            applyStoredUpgrades(player, upgradeTile, data);
        }
        if (sendSuccessMessage) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN,
                  MekanismLang.CONFIG_CARD_SET.translate(EnumColor.INDIGO, TextComponentUtil.translate(data.getString(NBTConstants.DATA_NAME)))));
            if (player instanceof ServerPlayer serverPlayer) {
                MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger(serverPlayer, false);
            }
        }
        return null;
    }

    public static void batchPasteFromCard(ServerPlayer player, InteractionHand hand, BlockPos firstPos, BlockPos secondPos, Direction side) {
        Level level = player.level();
        ItemStack stack = player.getItemInHand(hand);
        boolean clearMode = isClearMode(stack);
        boolean factoryMode = isFactoryMode(stack);
        if (!clearMode && !hasCardData(stack)) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, Component.literal("批量粘贴失败，配置卡没有保存数据")));
            return;
        }
        int minX = Math.min(firstPos.getX(), secondPos.getX());
        int minY = Math.min(firstPos.getY(), secondPos.getY());
        int minZ = Math.min(firstPos.getZ(), secondPos.getZ());
        int maxX = Math.max(firstPos.getX(), secondPos.getX());
        int maxY = Math.max(firstPos.getY(), secondPos.getY());
        int maxZ = Math.max(firstPos.getZ(), secondPos.getZ());
        List<BlockEntity> targets = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        BlockEntityType<?> storedType = getStoredTileType(data);
        boolean fuzzy = isFuzzyMode(stack);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockEntity target = level.getBlockEntity(cursor);
                    if (target == null) {
                        continue;
                    }
                    if (clearMode) {
                        if (!(target instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades() || !hasInstalledUpgrades(upgradeTile.getComponent())) {
                            continue;
                        }
                    } else if (factoryMode) {
                        if (!canTargetFactoryTier(target, data)) {
                            continue;
                        }
                    } else if (fuzzy) {
                        if (!(target instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades()) {
                            continue;
                        }
                    } else {
                        Optional<IConfigCardAccess> support = CapabilityUtils.getCapability(target, Capabilities.CONFIG_CARD, side).resolve();
                        if (support.isEmpty() || storedType == null || !isConfigurationCompatibleForPaste(support.get(), storedType)) {
                            continue;
                        }
                    }
                    targets.add(target);
                }
            }
        }
        handleBatchTargets(player, stack, side, firstPos, targets, clearMode, factoryMode, isFuzzyMode(stack), data, "批量");
    }

    public static void batchPasteFromPositions(ServerPlayer player, InteractionHand hand, BlockPos origin, Collection<BlockPos> positions, Direction side) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clearMode = isClearMode(stack);
        boolean factoryMode = isFactoryMode(stack);
        if (!clearMode && !hasCardData(stack)) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, Component.literal("连锁粘贴失败，配置卡没有保存数据")));
            return;
        }
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        BlockEntityType<?> storedType = getStoredTileType(data);
        boolean fuzzy = isFuzzyMode(stack);
        List<BlockEntity> targets = new ArrayList<>();
        for (BlockPos pos : positions) {
            BlockEntity target = player.level().getBlockEntity(pos);
            if (target == null) {
                continue;
            }
            if (clearMode) {
                if (!(target instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades() || !hasInstalledUpgrades(upgradeTile.getComponent())) {
                    continue;
                }
            } else if (factoryMode) {
                if (!canTargetFactoryTier(target, data)) {
                    continue;
                }
            } else if (fuzzy) {
                if (!(target instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades()) {
                    continue;
                }
            } else {
                Optional<IConfigCardAccess> support = CapabilityUtils.getCapability(target, Capabilities.CONFIG_CARD, side).resolve();
                if (support.isEmpty() || storedType == null || !isConfigurationCompatibleForPaste(support.get(), storedType)) {
                    continue;
                }
            }
            targets.add(target);
        }
        handleBatchTargets(player, stack, side, origin, targets, clearMode, factoryMode, fuzzy, data, "连锁");
    }

    private static void handleBatchTargets(ServerPlayer player, ItemStack stack, Direction side, BlockPos origin, List<BlockEntity> targets, boolean clearMode, boolean factoryMode, boolean fuzzy, CompoundTag data, String actionPrefix) {
        targets.sort(Comparator.comparingDouble(tile -> tile.getBlockPos().distSqr(origin)));
        if (targets.isEmpty()) {
            Component message = clearMode
                  ? Component.literal(actionPrefix + "清除失败，范围内没有可清除升级的机器")
                  : factoryMode
                  ? Component.literal(actionPrefix + "工厂升级失败，范围内没有可升级到目标等级的机器")
                  : Component.literal(actionPrefix + "粘贴失败，范围内没有兼容机器");
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, message));
            return;
        }
        int successCount = 0;
        Set<String> skippedMessages = new LinkedHashSet<>();
        for (BlockEntity target : targets) {
            if (fuzzy && target instanceof IUpgradeTile upgradeTile) {
                List<Upgrade> unsupported = getUnsupportedDesiredUpgrades(upgradeTile.getComponent(), getStoredUpgrades(data));
                if (!unsupported.isEmpty()) {
                    skippedMessages.add(buildUnsupportedUpgradeMessage(target, unsupported).getString());
                }
            }
            Component failure = pasteCardToTarget(player, target, side, stack, false);
            if (failure != null) {
                BlockPos failedPos = target.getBlockPos();
                if (fuzzy && !skippedMessages.isEmpty()) {
                    sendSkippedUpgradeMessages(player, skippedMessages);
                }
                String action = clearMode ? actionPrefix + "清除" : factoryMode ? actionPrefix + "工厂升级" : actionPrefix + "粘贴";
                player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED,
                      Component.literal(action + "在 " + failedPos.getX() + ", " + failedPos.getY() + ", " + failedPos.getZ() + " 处停止：" + failure.getString())));
                return;
            }
            successCount++;
        }
        if (successCount > 0) {
            MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger(player, false);
            Component success = clearMode
                  ? Component.literal(actionPrefix + "清除完成，共处理 " + successCount + " 台机器")
                  : factoryMode
                  ? Component.literal(actionPrefix + "工厂升级完成，共处理 " + successCount + " 台机器")
                  : Component.literal(actionPrefix + "粘贴完成，共处理 " + successCount + " 台机器");
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, success));
            if (fuzzy && !skippedMessages.isEmpty()) {
                sendSkippedUpgradeMessages(player, skippedMessages);
            }
        }
    }

    public static void handleBlockAction(ServerPlayer player, InteractionHand hand, BlockPos pos, Direction side) {
        Level level = player.level();
        if (!level.hasChunkAt(pos)) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemConfigurationCard)) {
            return;
        }
        Collection<BlockPos> ultiminePositions = UltimineCompat.isPressed(player) ? UltimineCompat.getCachedPositions(player, pos, side) : List.of();
        if (!ultiminePositions.isEmpty()) {
            batchPasteFromPositions(player, hand, pos, ultiminePositions, side);
            return;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile == null) {
            return;
        }
        if (!canUseBatchMode(stack)) {
            return;
        }
        Component failure = pasteCardToTarget(player, tile, side, stack, true);
        if (failure != null) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, failure));
        }
    }

    @Nullable
    public static Component clearUpgradesFromTarget(Player player, BlockEntity tile, boolean sendSuccessMessage) {
        if (!(tile instanceof IUpgradeTile upgradeTile) || !upgradeTile.supportsUpgrades()) {
            return Component.literal("清除失败，目标不支持升级");
        }
        TileComponentUpgrade component = upgradeTile.getComponent();
        if (!hasInstalledUpgrades(component)) {
            return Component.literal("清除失败，目标没有可移出的升级");
        }
        Component failure = validateClear(player, upgradeTile);
        if (failure != null) {
            return failure;
        }
        applyClear(player, upgradeTile);
        if (sendSuccessMessage) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, Component.literal("清除模式清除成功")));
        }
        return null;
    }

    @Nullable
    public static Component validatePaste(Player player, IUpgradeTile tile, CompoundTag data) {
        if (!tile.supportsUpgrades()) {
            return null;
        }
        TileComponentUpgrade component = tile.getComponent();
        Map<Upgrade, Integer> desired = getStoredUpgrades(data);
        InventorySimulation inventory = new InventorySimulation(player.getInventory());
        for (Upgrade upgrade : Upgrade.values()) {
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            if (target > 0 && !component.supports(upgrade)) {
                return Component.literal("粘贴失败，" + getTileName(tile) + "不支持" + UpgradeUtils.getStack(upgrade).getHoverName().getString());
            }
            if (!player.getAbilities().instabuild) {
                if (target > current && !inventory.remove(UpgradeUtils.getStack(upgrade), target - current)) {
                    return Component.literal("粘贴失败，缺少" + UpgradeUtils.getStack(upgrade).getHoverName().getString());
                }
                if (current > target && !inventory.insert(UpgradeUtils.getStack(upgrade, current - target))) {
                    return Component.literal("粘贴失败，背包空间不足");
                }
            }
        }
        return null;
    }

    @Nullable
    public static Component validateFuzzyPaste(Player player, IUpgradeTile tile, CompoundTag data) {
        if (!tile.supportsUpgrades()) {
            return null;
        }
        TileComponentUpgrade component = tile.getComponent();
        Map<Upgrade, Integer> desired = getStoredUpgrades(data);
        InventorySimulation inventory = new InventorySimulation(player.getInventory());
        for (Upgrade upgrade : Upgrade.values()) {
            if (!component.supports(upgrade)) {
                continue;
            }
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            if (!player.getAbilities().instabuild) {
                if (target > current && !inventory.remove(UpgradeUtils.getStack(upgrade), target - current)) {
                    return Component.literal("粘贴失败，缺少" + UpgradeUtils.getStack(upgrade).getHoverName().getString());
                }
                if (current > target && !inventory.insert(UpgradeUtils.getStack(upgrade, current - target))) {
                    return Component.literal("粘贴失败，背包空间不足");
                }
            }
        }
        return null;
    }

    @Nullable
    public static Component validateClear(Player player, IUpgradeTile tile) {
        if (!tile.supportsUpgrades()) {
            return null;
        }
        if (player.getAbilities().instabuild) {
            return null;
        }
        TileComponentUpgrade component = tile.getComponent();
        InventorySimulation inventory = new InventorySimulation(player.getInventory());
        for (Upgrade upgrade : Upgrade.values()) {
            int current = component.getUpgrades(upgrade);
            if (current > 0 && !inventory.insert(UpgradeUtils.getStack(upgrade, current))) {
                return Component.literal("清除失败，背包空间不足");
            }
        }
        return null;
    }

    public static void consumeUpgradeItems(Player player, IUpgradeTile tile, CompoundTag data) {
        if (!tile.supportsUpgrades()) {
            return;
        }
        if (player.getAbilities().instabuild) {
            return;
        }
        TileComponentUpgrade component = tile.getComponent();
        Map<Upgrade, Integer> desired = getStoredUpgrades(data);
        for (Upgrade upgrade : Upgrade.values()) {
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            if (target > current) {
                removeFromInventory(player.getInventory(), UpgradeUtils.getStack(upgrade).getItem(), target - current);
            }
        }
        player.getInventory().setChanged();
    }

    public static void applyStoredUpgrades(Player player, IUpgradeTile tile, CompoundTag data) {
        if (!tile.supportsUpgrades()) {
            return;
        }
        TileComponentUpgrade component = tile.getComponent();
        Map<Upgrade, Integer> desired = getStoredUpgrades(data);
        for (Upgrade upgrade : Upgrade.values()) {
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            while (current > target) {
                component.removeUpgrade(upgrade, false);
                handleRemovedUpgradeItem(player, component);
                current--;
            }
        }
        for (Upgrade upgrade : Upgrade.values()) {
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            if (target > current) {
                component.addUpgrades(upgrade, target - current);
            }
        }
    }

    public static void applyFuzzyStoredUpgrades(Player player, IUpgradeTile tile, CompoundTag data) {
        if (!tile.supportsUpgrades()) {
            return;
        }
        TileComponentUpgrade component = tile.getComponent();
        Map<Upgrade, Integer> desired = getStoredUpgrades(data);
        for (Upgrade upgrade : Upgrade.values()) {
            if (!component.supports(upgrade)) {
                continue;
            }
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            while (current > target) {
                component.removeUpgrade(upgrade, false);
                handleRemovedUpgradeItem(player, component);
                current--;
            }
        }
        for (Upgrade upgrade : Upgrade.values()) {
            if (!component.supports(upgrade)) {
                continue;
            }
            int current = component.getUpgrades(upgrade);
            int target = desired.getOrDefault(upgrade, 0);
            if (target > current) {
                if (!player.getAbilities().instabuild) {
                    removeFromInventory(player.getInventory(), UpgradeUtils.getStack(upgrade).getItem(), target - current);
                }
                component.addUpgrades(upgrade, target - current);
            }
        }
        player.getInventory().setChanged();
    }

    public static void applyClear(Player player, IUpgradeTile tile) {
        if (!tile.supportsUpgrades()) {
            return;
        }
        TileComponentUpgrade component = tile.getComponent();
        for (Upgrade upgrade : Upgrade.values()) {
            int current = component.getUpgrades(upgrade);
            while (current > 0) {
                component.removeUpgrade(upgrade, false);
                ItemStack extracted = component.getUpgradeOutputSlot().extractItem(1, Action.EXECUTE, AutomationType.MANUAL);
                ItemStack remainder = giveToInventory(player.getInventory(), extracted);
                if (!remainder.isEmpty()) {
                    player.drop(remainder, false);
                }
                current--;
            }
        }
    }

    public static boolean fillSupportedUpgrades(Player player, IUpgradeTile tile) {
        if (!tile.supportsUpgrades()) {
            return false;
        }
        TileComponentUpgrade component = tile.getComponent();
        boolean changed = false;
        for (Upgrade upgrade : Upgrade.values()) {
            if (!component.supports(upgrade)) {
                continue;
            }
            int current = component.getUpgrades(upgrade);
            int needed = upgrade.getMax() - current;
            if (needed <= 0) {
                continue;
            }
            int toAdd = needed;
            if (!player.getAbilities().instabuild) {
                toAdd = Math.min(needed, countInInventory(player.getInventory(), UpgradeUtils.getStack(upgrade).getItem()));
                if (toAdd <= 0) {
                    continue;
                }
                removeFromInventory(player.getInventory(), UpgradeUtils.getStack(upgrade).getItem(), toAdd);
            }
            int added = component.addUpgrades(upgrade, toAdd);
            if (!player.getAbilities().instabuild && added < toAdd) {
                ItemStack refund = giveToInventory(player.getInventory(), UpgradeUtils.getStack(upgrade, toAdd - added));
                if (!refund.isEmpty()) {
                    player.drop(refund, false);
                }
            }
            if (added > 0) {
                changed = true;
            }
        }
        if (changed) {
            player.getInventory().setChanged();
        }
        return changed;
    }

    private static void handleRemovedUpgradeItem(Player player, TileComponentUpgrade component) {
        ItemStack extracted = component.getUpgradeOutputSlot().extractItem(1, Action.EXECUTE, AutomationType.MANUAL);
        if (extracted.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        ItemStack remainder = giveToInventory(player.getInventory(), extracted);
        if (!remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }

    private static int countInInventory(Inventory inventory, Item item) {
        int total = 0;
        for (List<ItemStack> section : List.of(inventory.items, inventory.offhand)) {
            for (ItemStack stack : section) {
                if (stack.is(item)) {
                    total += stack.getCount();
                }
            }
        }
        Map<Item, Integer> bagCounts = new HashMap<>();
        CardSlotBagItem.appendBagContentsToSimulation(inventory, new ArrayList<>(), bagCounts);
        return total + bagCounts.getOrDefault(item, 0);
    }

    @Nullable
    private static Component applyFactoryTierToTarget(Player player, BlockEntity tile, ItemStack stack, boolean sendSuccessMessage) {
        CompoundTag data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        String extraTier = getStoredExtraFactoryTier(data);
        if (extraTier != null) {
            MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] paste factory choose extra path target={} extraTier={}", describeTile(tile), extraTier);
            return applyExtraFactoryTierToTarget(player, tile, extraTier, sendSuccessMessage);
        }
        BaseTier desiredTier = getStoredFactoryTier(data);
        if (desiredTier == null) {
            MekConfigCardUpgradesMod.LOGGER.warn("[mekccupgrades-debug] paste factory missing stored tier target={} dataName={} dataType={} standardFactoryKey={} extraFactoryKey={}",
                  describeTile(tile),
                  data.contains(NBTConstants.DATA_NAME, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_NAME) : "<none>",
                  data.contains(NBTConstants.DATA_TYPE, Tag.TAG_STRING) ? data.getString(NBTConstants.DATA_TYPE) : "<none>",
                  data.contains(FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(FACTORY_TIER_COPY_KEY) : "<none>",
                  data.contains(EXTRA_FACTORY_TIER_COPY_KEY, Tag.TAG_STRING) ? data.getString(EXTRA_FACTORY_TIER_COPY_KEY) : "<none>");
            return Component.literal("工厂模式失败，配置卡没有保存工厂等级");
        }
        if (!canTargetFactoryTier(tile, data)) {
            return Component.literal("工厂模式失败，目标不被识别为兼容工厂");
        }
        if (canBridgeToRecipeFactory(tile, data)) {
            Component recipeFailure = upgradeRecipeFactoryTowardTier(player, tile, desiredTier, data);
            if (recipeFailure != null) {
                return recipeFailure;
            }
            if (sendSuccessMessage) {
                player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, Component.literal("工厂模式升级成功")));
            }
            return null;
        }
        Component standardFailure = upgradeStandardFactoryTowardTier(player, tile, desiredTier);
        if (standardFailure != null) {
            return standardFailure;
        }
        if (sendSuccessMessage) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, Component.literal("工厂模式升级成功")));
        }
        return null;
    }

    private static boolean canTargetFactoryTier(BlockEntity tile, CompoundTag data) {
        if (getStoredExtraFactoryTier(data) != null) {
            return canExtraFactoryUpgrade(tile) || canBridgeStandardFactoryToExtra(tile) || canBridgeToStandardFactory(tile);
        }
        return getStoredFactoryTier(data) != null && (isStandardFactory(tile.getBlockState()) || canBridgeToStandardFactory(tile) || canBridgeToRecipeFactory(tile, data));
    }

    /**
     * Applies Mekanism standard factory tier installers until {@code desiredTier} is reached (or failure).
     * Used by factory mode and by bridging toward extended (Mekanism Extras) factory tiers.
     */
    @Nullable
    private static Component upgradeStandardFactoryTowardTier(Player player, BlockEntity tile, BaseTier desiredTier) {
        BaseTier currentTier = getCurrentStandardFactoryTier(tile.getBlockState());
        if (currentTier == desiredTier) {
            return null;
        }
        if (currentTier != null && currentTier.ordinal() > desiredTier.ordinal()) {
            return Component.literal("工厂模式失败，目标工厂等级高于配置卡记录");
        }
        for (BaseTier tier : BaseTier.values()) {
            if (tier == BaseTier.CREATIVE) {
                continue;
            }
            if ((currentTier != null && tier.ordinal() <= currentTier.ordinal()) || tier.ordinal() > desiredTier.ordinal()) {
                continue;
            }
            Component failure = applyTierInstallerStep(player, tile.getBlockPos(), tier);
            if (failure != null) {
                return failure;
            }
            BlockEntity refreshed = player.level().getBlockEntity(tile.getBlockPos());
            if (refreshed == null) {
                return Component.literal("工厂模式失败，升级后的机器不存在");
            }
            tile = refreshed;
            currentTier = getCurrentStandardFactoryTier(tile.getBlockState());
        }
        BaseTier finalTier = getCurrentStandardFactoryTier(player.level().getBlockState(tile.getBlockPos()));
        if (finalTier != desiredTier) {
            return Component.literal("工厂模式失败，未能升级到目标工厂等级");
        }
        return null;
    }

    @Nullable
    private static Component applyTierInstallerStep(Player player, BlockPos pos, BaseTier toTier) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        var upgradeableBlock = Attribute.get(state.getBlock(), AttributeUpgradeable.class);
        if (upgradeableBlock == null) {
            return Component.literal("工厂模式失败，目标不支持工厂安装器升级");
        }
        BaseTier fromTier = isStandardFactory(state) ? Attribute.getBaseTier(state.getBlock()) : null;
        BlockState upgradeState = upgradeableBlock.upgradeResult(state, toTier);
        if (state == upgradeState) {
            return Component.literal("工厂模式失败，无法应用" + installerName(toTier));
        }
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (!(tile instanceof ITierUpgradable tierUpgradable)) {
            return Component.literal("工厂模式失败，目标不支持工厂安装器升级");
        }
        if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
            return Component.literal("工厂模式失败，目标正在被使用");
        }
        IUpgradeData upgradeData = tierUpgradable.getUpgradeData();
        if (upgradeData == null) {
            return tierUpgradable.canBeUpgraded() ? Component.literal("工厂模式失败，目标无法导出升级数据") : Component.literal("工厂模式失败，目标不可升级");
        }
        if (!player.isCreative() && !hasTierInstaller(player, fromTier, toTier)) {
            return Component.literal("工厂模式失败，缺少" + installerName(toTier));
        }
        level.setBlockAndUpdate(pos, upgradeState);
        TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, level, pos);
        if (upgradedTile == null) {
            return Component.literal("工厂模式失败，升级后的机器不存在");
        }
        if (tile instanceof ITileDirectional directional && directional.isDirectional()) {
            upgradedTile.setFacing(directional.getDirection());
        }
        upgradedTile.parseUpgradeData(upgradeData);
        upgradedTile.sendUpdatePacket();
        upgradedTile.setChanged();
        if (!player.isCreative() && !consumeTierInstaller(player, fromTier, toTier)) {
            return Component.literal("工厂模式失败，缺少" + installerName(toTier));
        }
        return null;
    }

    @Nullable
    private static Component applyExtraFactoryTierToTarget(Player player, BlockEntity tile, String desiredTier, boolean sendSuccessMessage) {
        if (!canExtraFactoryUpgrade(tile)) {
            MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] extra path needs bridge target={}", describeTile(tile));
            Component bridgeFailure = bridgeStandardFactoryToExtra(player, tile.getBlockPos());
            if (bridgeFailure != null) {
                return bridgeFailure;
            }
            BlockEntity bridgedTile = player.level().getBlockEntity(tile.getBlockPos());
            if (bridgedTile == null) {
                return Component.literal("工厂模式失败，升级后的机器不存在");
            }
            tile = bridgedTile;
        }
        if (!canExtraFactoryUpgrade(tile)) {
            return Component.literal("工厂模式失败，目标不支持扩展工厂升级");
        }
        String currentTier = getCurrentExtraFactoryTierSerialized(tile);
        if (desiredTier.equals(currentTier)) {
            if (sendSuccessMessage) {
                player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, Component.literal("工厂模式升级成功")));
            }
            return null;
        }
        for (String tier : extraFactoryUpgradeChain()) {
            if ((currentTier != null && extraTierIndex(tier) <= extraTierIndex(currentTier)) || extraTierIndex(tier) > extraTierIndex(desiredTier)) {
                continue;
            }
            Component failure = applyExtraTierInstallerStep(player, tile.getBlockPos(), tier);
            if (failure != null) {
                return failure;
            }
        }
        BlockEntity finalTile = player.level().getBlockEntity(tile.getBlockPos());
        String finalTier = finalTile == null ? null : getCurrentExtraFactoryTierSerialized(finalTile);
        if (!desiredTier.equals(finalTier)) {
            return Component.literal("工厂模式失败，未能升级到目标工厂等级");
        }
        if (sendSuccessMessage) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, Component.literal("工厂模式升级成功")));
        }
        return null;
    }

    @Nullable
    private static Component bridgeStandardFactoryToExtra(Player player, BlockPos pos) {
        BlockEntity tile = player.level().getBlockEntity(pos);
        if (tile == null) {
            return Component.literal("工厂模式失败，目标机器不存在");
        }
        BlockState state = tile.getBlockState();
        if (!isStandardFactory(state)) {
            if (!canBridgeToStandardFactory(tile)) {
                return Component.literal("工厂模式失败，目标不支持扩展工厂升级");
            }
            Component bridgeToStandard = upgradeStandardFactoryTowardTier(player, tile, BaseTier.ULTIMATE);
            if (bridgeToStandard != null) {
                return bridgeToStandard;
            }
            tile = player.level().getBlockEntity(pos);
            if (tile == null) {
                return Component.literal("工厂模式失败，升级后的机器不存在");
            }
            state = tile.getBlockState();
        }
        if (!canBridgeStandardFactoryToExtra(tile)) {
            return Component.literal("工厂模式失败，目标不支持扩展工厂升级");
        }
        BaseTier currentTier = getCurrentStandardFactoryTier(tile.getBlockState());
        for (BaseTier tier : BaseTier.values()) {
            if (tier == BaseTier.CREATIVE || tier.ordinal() > BaseTier.ULTIMATE.ordinal()) {
                continue;
            }
            if (currentTier != null && tier.ordinal() <= currentTier.ordinal()) {
                continue;
            }
            Component failure = applyTierInstallerStep(player, pos, tier);
            if (failure != null) {
                return failure;
            }
            tile = player.level().getBlockEntity(pos);
            if (tile == null) {
                return Component.literal("工厂模式失败，升级后的机器不存在");
            }
            if (canExtraFactoryUpgrade(tile)) {
                MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] extra path bridge complete target={}", describeTile(tile));
                return null;
            }
            currentTier = getCurrentStandardFactoryTier(tile.getBlockState());
        }
        return canExtraFactoryUpgrade(tile) ? null : Component.literal("工厂模式失败，目标不支持扩展工厂升级");
    }

    @Nullable
    private static Component applyExtraTierInstallerStep(Player player, BlockPos pos, String toTier) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        Object upgradeableBlock = getExtraUpgradeable(state);
        if (upgradeableBlock == null) {
            return Component.literal("工厂模式失败，当前等级没有可用的扩展工厂升级路径");
        }
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        String fromTier = tile == null ? getCurrentExtraFactoryTierSerialized(state) : getCurrentExtraFactoryTierSerialized(tile);
        BlockState upgradeState = invokeExtraUpgradeResult(upgradeableBlock, state, toTier);
        if (upgradeState == null || state == upgradeState) {
            return Component.literal("工厂模式失败，无法应用" + installerName(toTier));
        }
        if (!(tile instanceof ITierUpgradable tierUpgradable)) {
            return Component.literal("工厂模式失败，目标不支持扩展工厂安装器升级");
        }
        if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
            return Component.literal("工厂模式失败，目标正在被使用");
        }
        IUpgradeData upgradeData = tierUpgradable.getUpgradeData();
        if (upgradeData == null) {
            return tierUpgradable.canBeUpgraded() ? Component.literal("工厂模式失败，目标无法导出升级数据") : Component.literal("工厂模式失败，目标不可升级");
        }
        if (!player.isCreative() && !hasExtraTierInstaller(player, fromTier, toTier)) {
            return Component.literal("工厂模式失败，缺少" + installerName(toTier));
        }
        level.setBlockAndUpdate(pos, upgradeState);
        TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, level, pos);
        if (upgradedTile == null) {
            return Component.literal("工厂模式失败，升级后的机器不存在");
        }
        if (tile instanceof ITileDirectional directional && directional.isDirectional()) {
            upgradedTile.setFacing(directional.getDirection());
        }
        upgradedTile.parseUpgradeData(upgradeData);
        upgradedTile.sendUpdatePacket();
        upgradedTile.setChanged();
        if (!player.isCreative() && !consumeExtraTierInstaller(player, fromTier, toTier)) {
            return Component.literal("工厂模式失败，缺少" + installerName(toTier));
        }
        return null;
    }

    private static ItemStack findTierInstaller(Player player, @Nullable BaseTier fromTier, BaseTier toTier) {
        for (List<ItemStack> section : List.of(player.getInventory().items, player.getInventory().offhand)) {
            for (ItemStack candidate : section) {
                if (candidate.getItem() instanceof ItemTierInstaller installer && installer.getFromTier() == fromTier && installer.getToTier() == toTier) {
                    return candidate;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack findExtraTierInstaller(Player player, @Nullable String fromTier, String toTier) {
        for (List<ItemStack> section : List.of(player.getInventory().items, player.getInventory().offhand)) {
            for (ItemStack candidate : section) {
                if (matchesExtraTierInstaller(candidate, fromTier, toTier)) {
                    return candidate;
                }
            }
        }
        MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades-debug] extra installer missing fromTier={} toTier={} inventoryCandidates={}",
              fromTier, toTier, describeExtraInstallerCandidates(player));
        return ItemStack.EMPTY;
    }

    private static boolean hasTierInstaller(Player player, @Nullable BaseTier fromTier, BaseTier toTier) {
        if (!findTierInstaller(player, fromTier, toTier).isEmpty()) {
            return true;
        }
        return CardSlotBagItem.hasInBags(player.getInventory(), candidate ->
              candidate.getItem() instanceof ItemTierInstaller installer && installer.getFromTier() == fromTier && installer.getToTier() == toTier, 1);
    }

    private static boolean consumeTierInstaller(Player player, @Nullable BaseTier fromTier, BaseTier toTier) {
        ItemStack installer = findTierInstaller(player, fromTier, toTier);
        if (!installer.isEmpty()) {
            installer.shrink(1);
            player.getInventory().setChanged();
            return true;
        }
        return CardSlotBagItem.consumeFromBags(player.getInventory(), candidate ->
              candidate.getItem() instanceof ItemTierInstaller itemTierInstaller
                    && itemTierInstaller.getFromTier() == fromTier
                    && itemTierInstaller.getToTier() == toTier, 1);
    }

    private static boolean hasExtraTierInstaller(Player player, @Nullable String fromTier, String toTier) {
        if (!findExtraTierInstaller(player, fromTier, toTier).isEmpty()) {
            return true;
        }
        return CardSlotBagItem.hasInBags(player.getInventory(), candidate -> matchesExtraTierInstaller(candidate, fromTier, toTier), 1);
    }

    private static boolean consumeExtraTierInstaller(Player player, @Nullable String fromTier, String toTier) {
        ItemStack installer = findExtraTierInstaller(player, fromTier, toTier);
        if (!installer.isEmpty()) {
            installer.shrink(1);
            player.getInventory().setChanged();
            return true;
        }
        return CardSlotBagItem.consumeFromBags(player.getInventory(), candidate -> matchesExtraTierInstaller(candidate, fromTier, toTier), 1);
    }

    private static String installerName(BaseTier toTier) {
        return toTier.getSimpleName() + "工厂安装器";
    }

    private static String installerName(String toTier) {
        return toTier + "工厂安装器";
    }

    private static boolean isStandardFactory(BlockState state) {
        return Attribute.has(state.getBlock(), AttributeFactoryType.class)
              || hasAttribute(state, MORE_MACHINE_FACTORY_TYPE)
              || hasAttribute(state, ADVANCED_FACTORY_TYPE);
    }

    @Nullable
    private static BaseTier getCurrentStandardFactoryTier(BlockState state) {
        return isStandardFactory(state) ? Attribute.getBaseTier(state.getBlock()) : null;
    }

    private static boolean canExtraFactoryUpgrade(BlockEntity tile) {
        return getCurrentExtraFactoryTierSerialized(tile) != null || getExtraUpgradeable(tile.getBlockState()) != null;
    }

    private static boolean canBridgeToStandardFactory(BlockEntity tile) {
        return tile instanceof ITierUpgradable && Attribute.get(tile.getBlockState().getBlock(), AttributeUpgradeable.class) != null;
    }

    private static boolean canBridgeToRecipeFactory(BlockEntity tile, CompoundTag data) {
        String family = getStoredRecipeFactoryFamily(data);
        if (family == null || !RECIPE_FACTORY_BASE_BLOCKS.containsKey(family)) {
            return false;
        }
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(tile.getBlockState().getBlock());
        if (blockId == null) {
            return false;
        }
        String path = blockId.getPath();
        return path.equals(RECIPE_FACTORY_BASE_BLOCKS.get(family)) || family.equals(recipeFactoryFamilyFromPath(path));
    }

    @Nullable
    private static Component upgradeRecipeFactoryTowardTier(Player player, BlockEntity tile, BaseTier desiredTier, CompoundTag data) {
        String family = getStoredRecipeFactoryFamily(data);
        if (family == null) {
            return Component.literal("工厂模式失败，未识别到配方型工厂类型");
        }
        String desiredTierName = desiredTier.getSerializedName();
        int desiredTierIndex = RECIPE_FACTORY_TIER_ORDER.indexOf(desiredTierName);
        if (desiredTierIndex < 0) {
            return Component.literal("工厂模式失败，不支持目标工厂等级：" + desiredTierName);
        }
        String currentFamily = currentRecipeFactoryFamily(tile);
        String currentTierName = currentRecipeFactoryTierName(tile);
        if (currentFamily != null && !family.equals(currentFamily)) {
            return Component.literal("工厂模式失败，目标属于其他工厂类型");
        }
        int currentTierIndex = currentTierName == null ? -1 : RECIPE_FACTORY_TIER_ORDER.indexOf(currentTierName);
        if (currentTierIndex > desiredTierIndex) {
            return Component.literal("工厂模式失败，目标工厂等级高于配置卡记录");
        }
        for (int i = currentTierIndex + 1; i <= desiredTierIndex; i++) {
            String nextTierName = RECIPE_FACTORY_TIER_ORDER.get(i);
            Component failure = applyRecipeFactoryStep(player, tile.getBlockPos(), family, nextTierName);
            if (failure != null) {
                return failure;
            }
            BlockEntity refreshed = player.level().getBlockEntity(tile.getBlockPos());
            if (refreshed == null) {
                return Component.literal("工厂模式失败，升级后的机器不存在");
            }
            tile = refreshed;
        }
        String finalTier = currentRecipeFactoryTierName(player.level().getBlockEntity(tile.getBlockPos()));
        if (!desiredTierName.equals(finalTier)) {
            return Component.literal("工厂模式失败，未能升级到目标工厂等级");
        }
        return null;
    }

    @Nullable
    private static Component applyRecipeFactoryStep(Player player, BlockPos pos, String family, String toTierName) {
        Level level = player.level();
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile == null) {
            return Component.literal("工厂模式失败，目标机器不存在");
        }
        if (!(tile instanceof ITierUpgradable tierUpgradable)) {
            return Component.literal("工厂模式失败，目标不支持工厂升级");
        }
        if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
            return Component.literal("工厂模式失败，目标正在被使用");
        }
        FactoryRecipeMaterials materials = RECIPE_FACTORY_MATERIALS.get(toTierName);
        if (materials == null) {
            return Component.literal("工厂模式失败，不支持配方型等级：" + toTierName);
        }
        ResourceLocation targetBlockId = new ResourceLocation(MORE_MACHINE_NAMESPACE, toTierName + "_" + family + "_factory");
        Block targetBlock = ForgeRegistries.BLOCKS.getValue(targetBlockId);
        if (targetBlock == null) {
            return Component.literal("工厂模式失败，未找到目标工厂方块：" + targetBlockId);
        }
        IUpgradeData upgradeData = tierUpgradable.getUpgradeData();
        CompoundTag tileTag = upgradeData == null ? tile.saveWithFullMetadata() : null;
        if (!player.isCreative()) {
            InventorySimulation inventory = new InventorySimulation(player.getInventory());
            if (!inventory.removeMatching(materials.ingotMatcher(), 2)) {
                return Component.literal("工厂模式失败，缺少工厂升级材料（锭）");
            }
            if (!inventory.removeMatching(materials.circuitMatcher(), 2)) {
                return Component.literal("工厂模式失败，缺少工厂升级材料（电路）");
            }
            if (!inventory.removeMatching(materials.alloyMatcher(), 4)) {
                return Component.literal("工厂模式失败，缺少工厂升级材料（合金）");
            }
            removeMatchingItems(player.getInventory(), materials.ingotMatcher(), 2);
            removeMatchingItems(player.getInventory(), materials.circuitMatcher(), 2);
            removeMatchingItems(player.getInventory(), materials.alloyMatcher(), 4);
            player.getInventory().setChanged();
        }
        BlockState state = tile.getBlockState();
        BlockState targetState = copySharedState(targetBlock.defaultBlockState(), state);
        level.setBlockAndUpdate(pos, targetState);
        TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, level, pos);
        if (upgradedTile == null) {
            return Component.literal("工厂模式失败，升级后的机器不存在");
        }
        if (tile instanceof ITileDirectional directional && directional.isDirectional()) {
            upgradedTile.setFacing(directional.getDirection());
        }
        if (upgradeData != null) {
            upgradedTile.parseUpgradeData(upgradeData);
        } else if (tileTag != null) {
            upgradedTile.load(tileTag);
        }
        upgradedTile.sendUpdatePacket();
        upgradedTile.setChanged();
        return null;
    }

    private static boolean canBridgeStandardFactoryToExtra(BlockEntity tile) {
        if (!(tile instanceof ITierUpgradable)) {
            return false;
        }
        BlockState state = tile.getBlockState();
        if (!isStandardFactory(state)) {
            return false;
        }
        BaseTier currentTier = getCurrentStandardFactoryTier(state);
        return currentTier == BaseTier.ULTIMATE || Attribute.get(state.getBlock(), AttributeUpgradeable.class) != null;
    }

    @Nullable
    private static String getCurrentExtraFactoryTierSerialized(BlockState state) {
        String fromId = blockStateId(state);
        String inferred = inferExtraTierFromString(fromId);
        if (inferred != null) {
            return inferred;
        }
        try {
            Class<?> extraAttributeClass = loadFirstClass(EXTRA_ATTRIBUTE_CLASSES);
            if (extraAttributeClass == null) {
                return null;
            }
            Method getAdvanceTier = extraAttributeClass.getMethod("getAdvanceTier", net.minecraft.world.level.block.Block.class);
            Object tier = getAdvanceTier.invoke(null, state.getBlock());
            return tier == null ? null : invokeSerializedName(tier);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    private static String getCurrentExtraFactoryTierSerialized(BlockEntity tile) {
        String inferred = inferExtraTierFromString(describeTile(tile));
        if (inferred != null) {
            return inferred;
        }
        String fromBlock = getCurrentExtraFactoryTierSerialized(tile.getBlockState());
        if (fromBlock != null) {
            return fromBlock;
        }
        try {
            var field = tile.getClass().getField("tier");
            return invokeExtraTierSerializedName(field.get(tile));
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    private static Object getExtraUpgradeable(BlockState state) {
        Class<? extends mekanism.common.block.attribute.Attribute> attrClass = loadFirstAttributeClass(EXTRA_ATTRIBUTE_UPGRADEABLE_CLASSES);
        if (attrClass == null) {
            return null;
        }
        return Attribute.get(state.getBlock(), attrClass);
    }

    private static boolean matchesExtraTierInstaller(ItemStack stack, @Nullable String fromTier, String toTier) {
        Item item = stack.getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (matchesExtraTierInstallerByRegistryName(key, fromTier, toTier)) {
            return true;
        }
        Class<?> installerClass = loadFirstClass(EXTRA_ITEM_TIER_INSTALLER_CLASSES);
        if (installerClass == null || !installerClass.isInstance(item)) {
            return false;
        }
        try {
            Method getFromTier = installerClass.getMethod("getFromTier");
            Method getToTier = installerClass.getMethod("getToTier");
            String candidateFrom = invokeExtraTierSerializedName(getFromTier.invoke(item));
            String candidateTo = invokeExtraTierSerializedName(getToTier.invoke(item));
            return toTier.equals(candidateTo) && ((fromTier == null && candidateFrom == null) || (fromTier != null && fromTier.equals(candidateFrom)));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Nullable
    private static BlockState invokeExtraUpgradeResult(Object extraUpgradeable, BlockState state, String toTier) {
        try {
            Method upgradeResult = null;
            for (Method candidate : extraUpgradeable.getClass().getMethods()) {
                Class<?>[] params = candidate.getParameterTypes();
                if (candidate.getName().equals("upgradeResult") && params.length == 2 && params[0] == BlockState.class) {
                    upgradeResult = candidate;
                    break;
                }
            }
            if (upgradeResult == null) {
                return null;
            }
            Object targetTier = getEnumConstantBySerializedName(upgradeResult.getParameterTypes()[1], toTier);
            if (targetTier == null) {
                return null;
            }
            return (BlockState) upgradeResult.invoke(extraUpgradeable, state, targetTier);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static List<String> extraFactoryUpgradeChain() {
        return EXTRA_TIER_NAMES;
    }

    private static int extraTierIndex(String tier) {
        List<String> chain = extraFactoryUpgradeChain();
        int index = chain.indexOf(tier);
        return index == -1 ? Integer.MAX_VALUE : index;
    }

    @Nullable
    private static String previousExtraTier(String tier) {
        int index = extraTierIndex(tier);
        if (index == Integer.MAX_VALUE || index == 0) {
            return null;
        }
        return EXTRA_TIER_NAMES.get(index - 1);
    }

    @Nullable
    private static String invokeSerializedName(@Nullable Object tier) {
        if (tier == null) {
            return null;
        }
        try {
            Method getSerializedName = tier.getClass().getMethod("getSerializedName");
            Object value = getSerializedName.invoke(tier);
            return value instanceof String string ? string : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    private static String invokeExtraTierSerializedName(@Nullable Object tier) {
        String serialized = invokeSerializedName(tier);
        if (serialized != null) {
            return serialized;
        }
        if (tier == null) {
            return null;
        }
        try {
            Method getAdvanceTier = tier.getClass().getMethod("getAdvanceTier");
            return invokeSerializedName(getAdvanceTier.invoke(tier));
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    private static Object getEnumConstantBySerializedName(Class<?> enumClass, String serializedName) {
        if (!enumClass.isEnum()) {
            return null;
        }
        Object[] constants = enumClass.getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (Object constant : constants) {
            String serialized = invokeExtraTierSerializedName(constant);
            if (serializedName.equals(serialized)) {
                return constant;
            }
            if (constant instanceof Enum<?> enumConstant && serializedName.equals(enumConstant.name().toLowerCase())) {
                return constant;
            }
        }
        return null;
    }

    @Nullable
    private static String inferExtraTierFromString(@Nullable String value) {
        if (value == null) {
            return null;
        }
        for (String tier : EXTRA_TIER_NAMES) {
            if (value.contains(tier)) {
                return tier;
            }
        }
        return null;
    }

    @Nullable
    private static String blockStateId(BlockState state) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return blockId == null ? null : blockId.toString();
    }

    private static boolean matchesExtraTierInstallerByRegistryName(@Nullable ResourceLocation key, @Nullable String fromTier, String toTier) {
        if (key == null || !("mekanism_extras".equals(key.getNamespace()) || "mekextras".equals(key.getNamespace()))) {
            return false;
        }
        if (!(toTier + "_tier_installer").equals(key.getPath())) {
            return false;
        }
        String expectedFrom = previousExtraTier(toTier);
        return expectedFrom == null ? fromTier == null : expectedFrom.equals(fromTier);
    }

    private static String describeExtraInstallerCandidates(Player player) {
        List<String> candidates = new ArrayList<>();
        for (List<ItemStack> section : List.of(player.getInventory().items, player.getInventory().offhand)) {
            for (ItemStack stack : section) {
                if (stack.isEmpty()) {
                    continue;
                }
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (key != null && key.getPath().endsWith("_tier_installer")) {
                    candidates.add(key + "x" + stack.getCount());
                }
            }
        }
        return candidates.toString();
    }

    private static boolean hasAttribute(BlockState state, String className) {
        Class<? extends mekanism.common.block.attribute.Attribute> attrClass = loadAttributeClass(className);
        return attrClass != null && Attribute.has(state.getBlock(), attrClass);
    }

    @Nullable
    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (Throwable ignored) {
            try {
                return Class.forName(className);
            } catch (Throwable ignoredAgain) {
                return null;
            }
        }
    }

    @Nullable
    private static Class<?> loadFirstClass(String... classNames) {
        for (String className : classNames) {
            Class<?> loaded = loadClass(className);
            if (loaded != null) {
                return loaded;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Class<? extends mekanism.common.block.attribute.Attribute> loadAttributeClass(String className) {
        Class<?> loaded = loadClass(className);
        if (loaded == null || !mekanism.common.block.attribute.Attribute.class.isAssignableFrom(loaded)) {
            return null;
        }
        return (Class<? extends mekanism.common.block.attribute.Attribute>) loaded;
    }

    @Nullable
    private static Class<? extends mekanism.common.block.attribute.Attribute> loadFirstAttributeClass(String... classNames) {
        for (String className : classNames) {
            Class<? extends mekanism.common.block.attribute.Attribute> loaded = loadAttributeClass(className);
            if (loaded != null) {
                return loaded;
            }
        }
        return null;
    }

    private static Map<Upgrade, Integer> getStoredUpgrades(CompoundTag data) {
        if (!hasUpgradeData(data)) {
            return Map.of();
        }
        return Upgrade.buildMap(data.getCompound(UPGRADE_COPY_KEY));
    }

    private static List<Upgrade> getUnsupportedDesiredUpgrades(TileComponentUpgrade component, Map<Upgrade, Integer> desired) {
        List<Upgrade> unsupported = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            if (desired.getOrDefault(upgrade, 0) > 0 && !component.supports(upgrade)) {
                unsupported.add(upgrade);
            }
        }
        return unsupported;
    }

    private static boolean hasInstalledUpgrades(TileComponentUpgrade component) {
        for (Upgrade upgrade : Upgrade.values()) {
            if (component.getUpgrades(upgrade) > 0) {
                return true;
            }
        }
        return false;
    }

    private static Component buildUnsupportedUpgradeMessage(BlockEntity tile, List<Upgrade> unsupported) {
        List<String> names = new ArrayList<>();
        for (Upgrade upgrade : unsupported) {
            names.add(UpgradeUtils.getStack(upgrade).getHoverName().getString());
        }
        return Component.literal("已跳过不兼容升级：" + getTileName(tile) + " 不支持 " + String.join("、", names));
    }

    private static void sendSkippedUpgradeMessages(Player player, Set<String> skippedMessages) {
        for (String skippedMessage : skippedMessages) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.YELLOW, Component.literal(skippedMessage)));
        }
    }

    private static String getTileName(Object tile) {
        if (tile instanceof BlockEntity blockEntity) {
            return blockEntity.getBlockState().getBlock().getName().getString();
        }
        return "该机器";
    }

    @Nullable
    private static String getStoredRecipeFactoryFamily(CompoundTag data) {
        BlockEntityType<?> storedType = getStoredTileType(data);
        if (storedType == null) {
            return null;
        }
        ResourceLocation storedId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(storedType);
        if (storedId == null || !MORE_MACHINE_NAMESPACE.equals(storedId.getNamespace())) {
            return null;
        }
        String family = recipeFactoryFamilyFromPath(storedId.getPath());
        return family != null && RECIPE_FACTORY_BASE_BLOCKS.containsKey(family) ? family : null;
    }

    @Nullable
    private static String currentRecipeFactoryFamily(@Nullable BlockEntity tile) {
        if (tile == null) {
            return null;
        }
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(tile.getBlockState().getBlock());
        return blockId == null ? null : recipeFactoryFamilyFromPath(blockId.getPath());
    }

    @Nullable
    private static String currentRecipeFactoryTierName(@Nullable BlockEntity tile) {
        if (tile == null) {
            return null;
        }
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(tile.getBlockState().getBlock());
        if (blockId == null) {
            return null;
        }
        String family = recipeFactoryFamilyFromPath(blockId.getPath());
        if (family == null) {
            return null;
        }
        String path = blockId.getPath();
        for (String tier : RECIPE_FACTORY_TIER_ORDER) {
            if (path.startsWith(tier + "_")) {
                return tier;
            }
        }
        return null;
    }

    @Nullable
    private static String recipeFactoryFamilyFromPath(String path) {
        String normalized = path.toLowerCase(Locale.ROOT);
        for (String tier : RECIPE_FACTORY_TIER_ORDER) {
            if (normalized.startsWith(tier + "_")) {
                normalized = normalized.substring(tier.length() + 1);
                break;
            }
        }
        if (!normalized.endsWith("_factory")) {
            return null;
        }
        String family = normalized.substring(0, normalized.length() - "_factory".length());
        return RECIPE_FACTORY_BASE_BLOCKS.containsKey(family) ? family : null;
    }

    private static String describeTile(BlockEntity tile) {
        ResourceLocation blockEntityId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(tile.getType());
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(tile.getBlockState().getBlock());
        return (blockId == null ? "<no-block-id>" : blockId.toString()) + "|" + (blockEntityId == null ? "<no-be-id>" : blockEntityId.toString()) + "|" + tile.getClass().getName();
    }

    private static Map<Upgrade, Integer> getInstalledUpgrades(TileComponentUpgrade component) {
        Map<Upgrade, Integer> installed = new EnumMap<>(Upgrade.class);
        for (Upgrade upgrade : component.getInstalledTypes()) {
            installed.put(upgrade, component.getUpgrades(upgrade));
        }
        return installed;
    }

    private static void removeFromInventory(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        for (List<ItemStack> section : List.of(inventory.items, inventory.offhand)) {
            for (ItemStack stack : section) {
                if (remaining <= 0) {
                    return;
                }
                if (stack.is(item)) {
                    int toShrink = Math.min(stack.getCount(), remaining);
                    stack.shrink(toShrink);
                    remaining -= toShrink;
                }
            }
        }
        if (remaining > 0) {
            CardSlotBagItem.consumeFromBags(inventory, candidate -> candidate.is(item), remaining);
        }
    }

    private static void removeMatchingItems(Inventory inventory, Predicate<ItemStack> matcher, int amount) {
        int remaining = amount;
        for (List<ItemStack> section : List.of(inventory.items, inventory.offhand)) {
            for (ItemStack stack : section) {
                if (remaining <= 0) {
                    return;
                }
                if (!stack.isEmpty() && matcher.test(stack)) {
                    int toShrink = Math.min(stack.getCount(), remaining);
                    stack.shrink(toShrink);
                    remaining -= toShrink;
                }
            }
        }
    }

    private static BlockState copySharedState(BlockState targetState, BlockState sourceState) {
        for (Property<?> property : sourceState.getProperties()) {
            targetState = copyProperty(targetState, sourceState, property);
        }
        return targetState;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState targetState, BlockState sourceState, Property<T> property) {
        if (targetState.hasProperty(property)) {
            return targetState.setValue(property, sourceState.getValue(property));
        }
        return targetState;
    }

    private record FactoryRecipeMaterials(String[] ingotTags, String[] alloyTags, String[] circuitTags) {
        private Predicate<ItemStack> ingotMatcher() {
            return stack -> matchesAnyTag(stack, ingotTags);
        }

        private Predicate<ItemStack> alloyMatcher() {
            return stack -> matchesAnyTag(stack, alloyTags);
        }

        private Predicate<ItemStack> circuitMatcher() {
            return stack -> matchesAnyTag(stack, circuitTags);
        }

        private static boolean matchesAnyTag(ItemStack stack, String[] tagIds) {
            for (String tagId : tagIds) {
                if (stack.is(TagKey.create(Registries.ITEM, new ResourceLocation(tagId)))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static ItemStack giveToInventory(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int remaining = stack.getCount();
        for (List<ItemStack> section : List.of(inventory.items, inventory.offhand)) {
            for (ItemStack existing : section) {
                if (remaining <= 0) {
                    inventory.setChanged();
                    return ItemStack.EMPTY;
                }
                if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)) {
                    int space = Math.min(existing.getMaxStackSize(), stack.getMaxStackSize()) - existing.getCount();
                    if (space > 0) {
                        int toAdd = Math.min(space, remaining);
                        existing.grow(toAdd);
                        remaining -= toAdd;
                    }
                }
            }
        }
        for (List<ItemStack> section : List.of(inventory.items, inventory.offhand)) {
            for (int i = 0; i < section.size(); i++) {
                if (remaining <= 0) {
                    inventory.setChanged();
                    return ItemStack.EMPTY;
                }
                if (section.get(i).isEmpty()) {
                    int toAdd = Math.min(stack.getMaxStackSize(), remaining);
                    section.set(i, stack.copyWithCount(toAdd));
                    remaining -= toAdd;
                }
            }
        }
        inventory.setChanged();
        return stack.copyWithCount(remaining);
    }

    private static final class InventorySimulation {
        private final Map<Item, Integer> counts = new LinkedHashMap<>();
        private final List<ItemStack> slots = new ArrayList<>();

        private InventorySimulation(Inventory inventory) {
            for (List<ItemStack> section : List.of(inventory.items, inventory.offhand)) {
                for (ItemStack stack : section) {
                    ItemStack copy = stack.copy();
                    slots.add(copy);
                    if (!copy.isEmpty()) {
                        counts.merge(copy.getItem(), copy.getCount(), Integer::sum);
                    }
                }
            }
            CardSlotBagItem.appendBagContentsToSimulation(inventory, slots, counts);
        }

        private boolean remove(ItemStack stack, int amount) {
            if (counts.getOrDefault(stack.getItem(), 0) < amount) {
                return false;
            }
            int remaining = amount;
            for (ItemStack slot : slots) {
                if (remaining <= 0) {
                    break;
                }
                if (slot.is(stack.getItem())) {
                    int toShrink = Math.min(slot.getCount(), remaining);
                    slot.shrink(toShrink);
                    remaining -= toShrink;
                }
            }
            counts.merge(stack.getItem(), -amount, Integer::sum);
            if (counts.get(stack.getItem()) != null && counts.get(stack.getItem()) <= 0) {
                counts.remove(stack.getItem());
            }
            return true;
        }

        private boolean insert(ItemStack stack) {
            int remaining = stack.getCount();
            for (ItemStack slot : slots) {
                if (remaining <= 0) {
                    return true;
                }
                if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, stack)) {
                    int space = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()) - slot.getCount();
                    if (space > 0) {
                        int toAdd = Math.min(space, remaining);
                        slot.grow(toAdd);
                        remaining -= toAdd;
                    }
                }
            }
            for (int i = 0; i < slots.size(); i++) {
                if (remaining <= 0) {
                    return true;
                }
                ItemStack slot = slots.get(i);
                if (slot.isEmpty()) {
                    int toAdd = Math.min(stack.getMaxStackSize(), remaining);
                    slots.set(i, stack.copyWithCount(toAdd));
                    remaining -= toAdd;
                }
            }
            return remaining <= 0;
        }

        private boolean removeMatching(Predicate<ItemStack> matcher, int amount) {
            int matched = 0;
            for (ItemStack slot : slots) {
                if (!slot.isEmpty() && matcher.test(slot)) {
                    matched += slot.getCount();
                    if (matched >= amount) {
                        break;
                    }
                }
            }
            if (matched < amount) {
                return false;
            }
            int remaining = amount;
            for (ItemStack slot : slots) {
                if (remaining <= 0) {
                    break;
                }
                if (!slot.isEmpty() && matcher.test(slot)) {
                    int toShrink = Math.min(slot.getCount(), remaining);
                    slot.shrink(toShrink);
                    remaining -= toShrink;
                }
            }
            return true;
        }
    }
}
