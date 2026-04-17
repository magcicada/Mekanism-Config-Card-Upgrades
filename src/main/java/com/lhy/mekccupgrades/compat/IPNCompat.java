package com.lhy.mekccupgrades.compat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lhy.mekccupgrades.MekConfigCardUpgradesMod;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

public final class IPNCompat {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path HINT_FILE = FMLPaths.GAMEDIR.get().resolve("inventoryprofilesnext").resolve("integrationHints").resolve("mekccupgrades.json");
    private static final Set<String> RECORDED_SCREENS = new HashSet<>();

    private IPNCompat() {
    }

    public static void ignoreScreenForLockOverlay(Class<?> screenClass) {
        if (!ModList.get().isLoaded("inventoryprofilesnext")) {
            return;
        }
        String screenClassName = screenClass.getName();
        if (!RECORDED_SCREENS.add(screenClassName)) {
            return;
        }
        boolean runtimePatched = applyRuntimeIgnore(screenClass);
        try {
            Files.createDirectories(HINT_FILE.getParent());
            JsonObject root = readHintRoot();
            if (root.has(screenClassName)) {
                if (runtimePatched) {
                    MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades] Applied runtime IPN ignore for screen {}", screenClassName);
                }
                return;
            }
            JsonObject hint = new JsonObject();
            hint.addProperty("ignore", true);
            hint.addProperty("playerSideOnly", false);
            hint.addProperty("force", true);
            hint.add("buttonHints", new JsonObject());
            root.add(screenClassName, hint);
            try (Writer writer = Files.newBufferedWriter(HINT_FILE)) {
                GSON.toJson(root, writer);
            }
            MekConfigCardUpgradesMod.LOGGER.info("[mekccupgrades] Added IPN ignore hint for screen {}", screenClassName);
        } catch (IOException exception) {
            MekConfigCardUpgradesMod.LOGGER.warn("[mekccupgrades] Failed writing IPN integration hint for {}", screenClassName, exception);
        }
    }

    private static boolean applyRuntimeIgnore(Class<?> screenClass) {
        try {
            Class<?> hintsManagerClass = Class.forName("org.anti_ad.mc.ipnext.integration.HintsManagerNG");
            Object hintsManager = hintsManagerClass.getField("INSTANCE").get(null);
            Method getHints = hintsManagerClass.getMethod("getHints", Class.class);
            Object hintClassData = getHints.invoke(hintsManager, screenClass);
            if (hintClassData == null) {
                return false;
            }
            Method setIgnore = hintClassData.getClass().getMethod("setIgnore", boolean.class);
            setIgnore.invoke(hintClassData, true);
            try {
                Method setForce = hintClassData.getClass().getMethod("setForce", boolean.class);
                setForce.invoke(hintClassData, true);
            } catch (ReflectiveOperationException ignored) {
                // Older/newer IPN versions may not expose this mutator; ignore silently.
            }
            return true;
        } catch (ReflectiveOperationException exception) {
            MekConfigCardUpgradesMod.LOGGER.debug("[mekccupgrades] Failed runtime IPN ignore patch for {}", screenClass.getName(), exception);
            return false;
        }
    }

    private static JsonObject readHintRoot() throws IOException {
        if (!Files.exists(HINT_FILE)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(HINT_FILE)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (RuntimeException ignored) {
            return new JsonObject();
        }
    }
}
