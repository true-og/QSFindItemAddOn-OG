package io.myzticbean.finditemaddon.Utils.JsonStorageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import io.myzticbean.finditemaddon.Models.HiddenShopModel;
import io.myzticbean.finditemaddon.Models.PlayerShopVisitModel;
import io.myzticbean.finditemaddon.Models.ShopSearchActivityModel;
import io.myzticbean.finditemaddon.Utils.LoggerUtils;
import lombok.Getter;
import me.kodysimpson.simpapi.colors.ColorTranslator;

public class ShopSearchActivityStorageUtil {

    private static final String SHOP_SEARCH_ACTIVITY_JSON_FILE_NAME = "shops.json";
    // private static final String COOLDOWNS_YAML_FILE_NAME = "cooldowns.yml";

    @Getter
    private static final Map<String, Long> cooldowns = Collections.unmodifiableMap(new HashMap<>());

    // private static File cooldownsYaml;
    // private static FileConfiguration cooldownsConfig;

    @Getter
    private static List<ShopSearchActivityModel> globalShopsList = new ArrayList<>();

    /**
     * Returns true if cooldown is not present
     * 
     * @param player
     * @return
     */
    private static boolean handleCooldownIfPresent(Location shopLocation, Player player) {
        // The below logic is flawed

        /*
         * // check player is inside hashmap if(cooldowns.containsKey(player.getName()))
         * { // check if player still has cooldown if(cooldowns.get(player.getName()) >
         * (System.currentTimeMillis()/1000)) { long timeLeft =
         * (cooldowns.get(player.getName()) - System.currentTimeMillis()/1000);
         * LoggerUtils.logDebugInfo(ColorTranslator.translateColorCodes("&6" +
         * player.getName() + " still has cooldown of " + timeLeft + " seconds!"));
         * return false; } } cooldowns.put(player.getName(),
         * (System.currentTimeMillis()/1000) +
         * FindItemAddOn.getConfigProvider().SHOP_PLAYER_VISIT_COOLDOWN_IN_MINUTES *
         * 60); LoggerUtils.logDebugInfo(ColorTranslator.
         * translateColorCodes("&aCooldown added for " +
         * FindItemAddOn.getConfigProvider().SHOP_PLAYER_VISIT_COOLDOWN_IN_MINUTES * 60
         * + " seconds for " + player.getName())); return true;
         */

        // New logic
        for (ShopSearchActivityModel shopSearchActivity : globalShopsList) {

            if (shopSearchActivity.compareWith(shopLocation.getWorld().getName(), shopLocation.getX(),
                    shopLocation.getY(), shopLocation.getZ()))
            {

                final List<PlayerShopVisitModel> playerShopVisitList = shopSearchActivity.getPlayerVisitList().stream()
                        .filter(p -> p.getPlayerUUID().equals(player.getUniqueId()))
                        .sorted(Comparator.comparing(PlayerShopVisitModel::getVisitDateTime))
                        .collect(Collectors.toCollection(ArrayList::new));

                final boolean isCooldownTimeElapsed;
                if (playerShopVisitList.size() > 0) {

                    isCooldownTimeElapsed = Instant.now()
                            .minusSeconds(
                                    FindItemAddOnOG.getConfigProvider().SHOP_PLAYER_VISIT_COOLDOWN_IN_MINUTES * 60)
                            .isAfter(playerShopVisitList.get(playerShopVisitList.size() - 1).getVisitDateTime());

                } else {

                    isCooldownTimeElapsed = true;

                }

                if (isCooldownTimeElapsed) {

                    LoggerUtils.logDebugInfo(
                            ColorTranslator.translateColorCodes("&6" + player.getName() + " is out of cooldown"));
                    return true;

                } else {

                    LoggerUtils.logDebugInfo(
                            ColorTranslator.translateColorCodes("&6" + player.getName() + " still has cooldown"));
                    return false;

                }

            }

        }

        LoggerUtils.logDebugInfo(
                ColorTranslator.translateColorCodes("&6Shop not found, returning false for cooldown check"));
        return false;

    }

    public static void syncShops() {

        globalShopsList = FindItemAddOnOG.getQsApiInstance().syncShopsListForStorage(globalShopsList);

    }

    // public static void saveCooldowns() {
    // try {
    // for(Map.Entry<String, Long> entry : cooldowns.entrySet()) {
    // cooldownsConfig.set("cooldowns." + entry.getKey(), entry.getValue());
    // }
    // cooldownsConfig.save(cooldownsYaml);
    // }
    // catch (IOException e) {
    // LoggerUtils.logError("Error saving config.yml");
    // }
    // }

    // public static void restoreCooldowns() {
    // if(cooldownsConfig.isConfigurationSection("cooldowns")) {
    // cooldownsConfig.getConfigurationSection("cooldowns").getKeys(false).forEach(key
    // -> {
    // long timeLeft = cooldownsConfig.getLong("cooldowns." + key);
    // cooldowns.put(key, timeLeft);
    // });
    // }
    // }

    /**
     * QuickShop Reremake
     * 
     * @param shop
     */
    public void addShop(org.maxgamer.quickshop.api.shop.Shop shop) {

        for (ShopSearchActivityModel shop_i : globalShopsList) {

            if (shop_i.getX() == shop.getLocation().getX() && shop_i.getY() == shop.getLocation().getY()
                    && shop_i.getZ() == shop.getLocation().getZ()
                    && shop_i.getWorldName().equalsIgnoreCase(shop.getLocation().getWorld().getName()))
            {

                break;

            }

        }

        final ShopSearchActivityModel shopModel = new ShopSearchActivityModel(shop.getLocation().getWorld().getName(),
                shop.getLocation().getX(), shop.getLocation().getY(), shop.getLocation().getZ(),
                shop.getLocation().getPitch(), shop.getLocation().getYaw(), shop.getOwner().toString(),
                new ArrayList<>(), false);
        globalShopsList.add(shopModel);

    }

    /**
     * QuickShop Hikari
     * 
     * @param shop
     */
    public void addShop(com.ghostchu.quickshop.api.shop.Shop shop) {

        final ShopSearchActivityModel shopModel = new ShopSearchActivityModel(shop.getLocation().getWorld().getName(),
                shop.getLocation().getX(), shop.getLocation().getY(), shop.getLocation().getZ(),
                shop.getLocation().getPitch(), shop.getLocation().getYaw(), shop.getOwner().toString(),
                new ArrayList<>(), false);
        globalShopsList.add(shopModel);

    }

    // public void deleteShop(org.maxgamer.quickshop.api.shop.Shop shop) {
    //
    // }
    //
    // public void deleteShop(com.ghostchu.quickshop.api.shop.Shop shop) {
    //
    // }

    public static void loadShopsFromFile() {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(FindItemAddOnOG.getInstance().getDataFolder().getAbsolutePath() + "/"
                + SHOP_SEARCH_ACTIVITY_JSON_FILE_NAME);
        if (file.exists()) {

            try {

                final Reader reader = new FileReader(file);
                final ShopSearchActivityModel[] h = gson.fromJson(reader, ShopSearchActivityModel[].class);
                if (h != null) {

                    globalShopsList = new ArrayList<>(Arrays.asList(h));

                } else {

                    globalShopsList = new ArrayList<>();

                }

                LoggerUtils.logInfo("Loaded shops from file");

            } catch (FileNotFoundException error) {

                error.printStackTrace();

            }

        }

        // else {
        // try {
        // file.createNewFile();
        // LoggerUtils.logInfo("Generated new " + SHOP_SEARCH_ACTIVITY_JSON_FILE_NAME);
        // } catch (IOException e) {
        // LoggerUtils.logError("Error generating " +
        // SHOP_SEARCH_ACTIVITY_JSON_FILE_NAME);
        // e.printStackTrace();
        // }
        // }
        globalShopsList = FindItemAddOnOG.getQsApiInstance().syncShopsListForStorage(globalShopsList);

    }

    public static void saveShopsToFile() {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File file = new File(FindItemAddOnOG.getInstance().getDataFolder().getAbsolutePath() + "/"
                + SHOP_SEARCH_ACTIVITY_JSON_FILE_NAME);
        file.getParentFile().mkdir();
        try {

            file.createNewFile();
            final Writer writer = new FileWriter(file, false);
            gson.toJson(globalShopsList, writer);
            writer.flush();
            writer.close();
            LoggerUtils.logInfo("Saved shops to file");

        } catch (IOException error) {

            error.printStackTrace();

        }

    }

    /*
     * public static void setupCooldownsConfigFile() { cooldownsYaml = new
     * File(FindItemAddOn.getInstance().getDataFolder(), COOLDOWNS_YAML_FILE_NAME);
     * if(!cooldownsYaml.exists()) { try { boolean isConfigGenerated =
     * cooldownsYaml.createNewFile(); if(isConfigGenerated) {
     * LoggerUtils.logInfo("Generated a new " + COOLDOWNS_YAML_FILE_NAME); } } catch
     * (IOException e) { LoggerUtils.logError("Error generating " +
     * COOLDOWNS_YAML_FILE_NAME); } } cooldownsConfig =
     * YamlConfiguration.loadConfiguration(cooldownsYaml); }
     */

    public static void migrateHiddenShopsToShopsJson() {

        final File hiddenShopsJsonfile = new File(FindItemAddOnOG.getInstance().getDataFolder().getAbsolutePath() + "/"
                + HiddenShopStorageUtil.HIDDEN_SHOP_STORAGE_JSON_FILE_NAME);
        if (hiddenShopsJsonfile.exists()) {

            HiddenShopStorageUtil.loadHiddenShopsFromFile();
            if (HiddenShopStorageUtil.hiddenShopsList.size() > 0) {

                for (int globalShopsList_i = 0; globalShopsList_i < globalShopsList.size(); globalShopsList_i++) {

                    final ShopSearchActivityModel shopSearchActivity = globalShopsList.get(globalShopsList_i);
                    HiddenShopModel tempHiddenShop = null;
                    for (HiddenShopModel hiddenShop_i : HiddenShopStorageUtil.hiddenShopsList) {

                        if (shopSearchActivity.compareWith(hiddenShop_i.getWorldName(), hiddenShop_i.getX(),
                                hiddenShop_i.getY(), hiddenShop_i.getZ(), hiddenShop_i.getShopOwnerUUID()))
                        {

                            tempHiddenShop = hiddenShop_i;
                            shopSearchActivity.setHiddenFromSearch(true);
                            globalShopsList.set(globalShopsList_i, shopSearchActivity);
                            LoggerUtils.logDebugInfo("Converted shop: " + shopSearchActivity);

                        }

                    }

                    HiddenShopStorageUtil.hiddenShopsList.remove(tempHiddenShop);

                }

            }

            LoggerUtils.logDebugInfo("Here we will delete the hiddenShops.json");
            hiddenShopsJsonfile.delete();

        } else {

            LoggerUtils.logDebugInfo("hiddenshops.json: No conversion required");

        }

    }

    public static void addPlayerVisitEntryAsync(Location shopLocation, Player visitingPlayer) {

        Bukkit.getScheduler().runTaskAsynchronously(FindItemAddOnOG.getInstance(), () -> {

            if (handleCooldownIfPresent(shopLocation, visitingPlayer)) {

                final Iterator<ShopSearchActivityModel> shopSearchActivityIterator = globalShopsList.iterator();
                int i = 0;
                while (shopSearchActivityIterator.hasNext()) {

                    final ShopSearchActivityModel shopSearchActivity = shopSearchActivityIterator.next();
                    if (shopSearchActivity.compareWith(shopLocation.getWorld().getName(), shopLocation.getX(),
                            shopLocation.getY(), shopLocation.getZ()))
                    {

                        final PlayerShopVisitModel playerShopVisit = new PlayerShopVisitModel();
                        playerShopVisit.setPlayerUUID(visitingPlayer.getUniqueId());
                        playerShopVisit.setVisitDateTime();
                        // shopSearchActivity.getPlayerVisitList().add(playerShopVisit);
                        globalShopsList.get(i).getPlayerVisitList().add(playerShopVisit);
                        LoggerUtils.logDebugInfo("Added new player visit entry at " + shopLocation.toString());
                        break;

                    }

                    i++;

                }

                // for(ShopSearchActivityModel shopSearchActivity : globalShopsList) {
                // if(shopSearchActivity.compareWith(
                // shopLocation.getWorld().getName(),
                // shopLocation.getX(),
                // shopLocation.getY(),
                // shopLocation.getZ()
                // )) {
                // PlayerShopVisitModel playerShopVisit = new PlayerShopVisitModel();
                // playerShopVisit.setPlayerUUID(visitingPlayer.getUniqueId());
                // playerShopVisit.setVisitDateTime();
                // shopSearchActivity.getPlayerVisitList().add(playerShopVisit);
                // LoggerUtils.logDebugInfo("Added new player visit entry at " +
                // shopLocation.toString());
                // break;
                // }
                // }
            }

        });

    }

    public static int getPlayerVisitCount(Location shopLocation) {

        return globalShopsList.stream()
                .filter(shopSearchActivity -> shopSearchActivity.compareWith(shopLocation.getWorld().getName(),
                        shopLocation.getX(), shopLocation.getY(), shopLocation.getZ()))
                .findFirst().map(shopSearchActivity -> shopSearchActivity.getPlayerVisitList().size()).orElse(0);

    }

    @Nullable
    public static OfflinePlayer getShopOwner(@NotNull Location shopLocation) {

        return globalShopsList.stream()
                .filter(shopSearchActivity -> shopSearchActivity.compareWith(shopLocation.getWorld().getName(),
                        shopLocation.getX(), shopLocation.getY(), shopLocation.getZ()))
                .findFirst().map(shopSearchActivity -> Bukkit
                        .getOfflinePlayer(UUID.fromString(shopSearchActivity.getShopOwnerUUID())))
                .orElse(null);

    }

    @Nullable
    public static UUID getShopOwnerUUID(@NotNull Location shopLocation) {

        return globalShopsList.stream()
                .filter(shopSearchActivity -> shopSearchActivity.compareWith(shopLocation.getWorld().getName(),
                        shopLocation.getX(), shopLocation.getY(), shopLocation.getZ()))
                .findFirst().map(shopSearchActivity -> UUID.fromString(shopSearchActivity.getShopOwnerUUID()))
                .orElse(null);

    }

}