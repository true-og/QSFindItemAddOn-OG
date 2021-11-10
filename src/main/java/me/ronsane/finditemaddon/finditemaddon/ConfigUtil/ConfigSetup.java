package me.ronsane.finditemaddon.finditemaddon.ConfigUtil;

import me.ronsane.finditemaddon.finditemaddon.Utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigSetup {

    private static File file;
    private static FileConfiguration fileConfig;

    public static void setupConfig() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("QSFindItemAddOn").getDataFolder(), "config.yml");

        if(!file.exists()) {
            try {
                boolean isConfigGenerated = file.createNewFile();
                if(isConfigGenerated) {
                    LoggerUtils.logInfo("Generated a new config.yml");
                }
            }
            catch (IOException e) {
                LoggerUtils.logError("Error generating config.yml");
            }
        }

        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static void saveConfig() {
        try {
            fileConfig.save(file);
        }
        catch (IOException e) {
            LoggerUtils.logError("Error saving config.yml");
        }
    }

    public static void checkForMissingProperties() {
        if(!fileConfig.contains("config-version", true)) {
            fileConfig.set("config-version", 9);
        }
        else {
            fileConfig.set("config-version", 9);
        }
        if(!fileConfig.contains("search-loaded-shops-only", true)) {
            fileConfig.set("search-loaded-shops-only", false);
        }
        if(!fileConfig.contains("nearest-warp-mode", true)) {
            fileConfig.set("nearest-warp-mode", 1);
        }
        else {
            try {
                int nearestWarpMode = fileConfig.getInt("nearest-warp-mode");
                if(nearestWarpMode != 1 && nearestWarpMode != 2 && nearestWarpMode != 3) {
                    LoggerUtils.logError("Invalid value for 'nearest-warp-mode' in config.yml!");
                    LoggerUtils.logError("Resetting by default to &e1");
                    fileConfig.set("nearest-warp-mode", 1);
                }
            }
            catch(Exception e) {
                LoggerUtils.logError("Invalid value for 'nearest-warp-mode' in config.yml!");
                LoggerUtils.logError("Resetting by default to &e1");
                fileConfig.set("nearest-warp-mode", 1);
            }
        }
        if(!fileConfig.contains("shop-gui-item-lore", true)) {
            List<String> shopGUIItemLore = new ArrayList<>();
            shopGUIItemLore.add("");
            shopGUIItemLore.add("&fPrice: &a${ITEM_PRICE}");
            shopGUIItemLore.add("&fStock: &7{SHOP_STOCK}");
            shopGUIItemLore.add("&fOwner: &7{SHOP_OWNER}");
            shopGUIItemLore.add("&fLocation: &7{SHOP_LOC}");
            shopGUIItemLore.add("&fWorld: &7{SHOP_WORLD}");
            shopGUIItemLore.add("&fWarp: &7{NEAREST_WARP}");
            shopGUIItemLore.add("");
            fileConfig.set("shop-gui-item-lore", shopGUIItemLore);
        }
        if(!fileConfig.contains("blacklisted-worlds", true)) {
            List<String> blacklistedWorlds = new ArrayList<>();
            blacklistedWorlds.add("world_number_1");
            blacklistedWorlds.add("world_number_2");
            fileConfig.set("blacklisted-worlds", blacklistedWorlds);
        }
        if(!fileConfig.contains("find-item-command.hideshop-autocomplete", true)) {
            fileConfig.set("find-item-command.hideshop-autocomplete", "hideshop");
        }
        if(!fileConfig.contains("find-item-command.revealshop-autocomplete", true)) {
            fileConfig.set("find-item-command.revealshop-autocomplete", "revealshop");
        }
        if(!fileConfig.contains("find-item-command.shop-hide-success-message", true)) {
            fileConfig.set("find-item-command.shop-hide-success-message", "&aShop is now hidden from search list!");
        }
        if(!fileConfig.contains("find-item-command.shop-reveal-success-message", true)) {
            fileConfig.set("find-item-command.shop-reveal-success-message", "&aShop is no longer hidden from search list!");
        }
        if(!fileConfig.contains("find-item-command.shop-already-hidden-message", true)) {
            fileConfig.set("find-item-command.shop-already-hidden-message", "&cThis shop is already hidden!");
        }
        if(!fileConfig.contains("find-item-command.shop-already-public-message", true)) {
            fileConfig.set("find-item-command.shop-already-public-message", "&cThis shop is already public!");
        }
        if(!fileConfig.contains("find-item-command.invalid-shop-block-message", true)) {
            fileConfig.set("find-item-command.invalid-shop-block-message", "&cThe block you are looking at is not a shop!");
        }
        if(!fileConfig.contains("find-item-command.hiding-shop-owner-invalid-message", true)) {
            fileConfig.set("find-item-command.hiding-shop-owner-invalid-message", "&cThat shop is not yours!");
        }
    }

    public static FileConfiguration get() {
        return fileConfig;
    }

    public static void reloadConfig() {
        fileConfig = YamlConfiguration.loadConfiguration(file);
    }

}
