/**
 * QSFindItemAddOn: An Minecraft add-on plugin for the QuickShop Hikari
 * and Reremake Shop plugins for Spigot server platform.
 * Copyright (C) 2021  myzticbean
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.myzticbean.finditemaddon;

import io.myzticbean.finditemaddon.config.ConfigProvider;
import io.myzticbean.finditemaddon.config.ConfigSetup;
import io.myzticbean.finditemaddon.dependencies.EssentialsXPlugin;
import io.myzticbean.finditemaddon.dependencies.PlayerWarpsPlugin;
import io.myzticbean.finditemaddon.dependencies.ResidencePlugin;
import io.myzticbean.finditemaddon.dependencies.WGPlugin;
import io.myzticbean.finditemaddon.handlers.gui.PlayerMenuUtility;
import io.myzticbean.finditemaddon.listeners.MenuListener;
import io.myzticbean.finditemaddon.listeners.PWPlayerWarpCreateEventListener;
import io.myzticbean.finditemaddon.listeners.PWPlayerWarpRemoveEventListener;
import io.myzticbean.finditemaddon.listeners.PlayerCommandSendEventListener;
import io.myzticbean.finditemaddon.listeners.PluginEnableEventListener;
import io.myzticbean.finditemaddon.quickshop.QSApi;
import io.myzticbean.finditemaddon.quickshop.impl.QSHikariAPIHandler;
import io.myzticbean.finditemaddon.quickshop.impl.QSReremakeAPIHandler;
import io.myzticbean.finditemaddon.scheduledtasks.Task15MinInterval;
import io.myzticbean.finditemaddon.utils.enums.PlayerPermsEnum;
import io.myzticbean.finditemaddon.utils.json.ShopSearchActivityStorageUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.milkbowl.vault.economy.Economy;
import net.trueog.utilitiesog.UtilitiesOG;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author myzticbean
 */
@Slf4j
public final class QSFindItemAddOnOG extends JavaPlugin {

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }

    // ONLY FOR SNAPSHOT BUILDS!
    // Change it to whenever you want your snapshot trial build to expire
    // *****************
    private static final boolean ENABLE_TRIAL_PERIOD = false;
    private static final int TRIAL_END_YEAR = 2024, TRIAL_END_MONTH = 5, TRIAL_END_DAY = 5;
    // ************************************************************************************

    private static Plugin pluginInstance;

    public QSFindItemAddOnOG() {
        pluginInstance = this;
    }

    public static Plugin getInstance() {
        return pluginInstance;
    }

    private static Economy econ;

    public static Economy getEconomy() {
        return econ;
    }

    public static String serverVersion;
    private static final int SPIGOT_PLUGIN_ID = 95104;
    private static final int REPEATING_TASK_SCHEDULE_MINS = 15 * 60 * 20;

    @Getter
    private static ConfigProvider configProvider;

    private static boolean isPluginOutdated = false;
    private static boolean qSReremakeInstalled = false;
    private static boolean qSHikariInstalled = false;

    @SuppressWarnings("rawtypes")
    private static QSApi qsApi;

    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    @Override
    public void onLoad() {
        logger("A Shop Search AddOn for QuickShop developed by myzticbean");
        // Show warning if it's a snapshot build.
        if (this.getPluginMeta().getVersion().toLowerCase().contains("snapshot")) {
            logger("This is a SNAPSHOT build! NOT recommended for production servers.");
            logger(
                    "If you find any bugs, please report them here: https://github.com/myzticbean/QSFindItemAddOn/issues");
        }
    }

    @Override
    public void onEnable() {

        if (ENABLE_TRIAL_PERIOD) {
            logger("THIS IS A TRIAL BUILD!");
            LocalDateTime trialEndDate =
                    LocalDate.of(TRIAL_END_YEAR, TRIAL_END_MONTH, TRIAL_END_DAY).atTime(LocalTime.MIDNIGHT);
            LocalDateTime today = LocalDateTime.now();
            Duration duration = Duration.between(trialEndDate, today);
            boolean hasPassed = Duration.ofDays(ChronoUnit.DAYS.between(today, trialEndDate))
                    .isNegative();
            if (hasPassed) {
                logger("Your trial has expired! Please contact the developer.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else {
                logger("You have " + Math.abs(duration.toDays()) + " days remaining in your trial.");
            }
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("QuickShop")
                && !Bukkit.getPluginManager().isPluginEnabled("QuickShop-Hikari")) {
            logger("Delaying QuickShop hook as they are not enabled yet");
        } else if (Bukkit.getPluginManager().isPluginEnabled("QuickShop")) {
            qSReremakeInstalled = true;
        } else {
            qSHikariInstalled = true;
        }

        if (!setupEconomy()) {
            getLogger().severe("Vault not found – disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Registering Bukkit event listeners
        initBukkitEventListeners();

        // Handle config file
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        ConfigSetup.setupConfig();
        ConfigSetup.get().options().copyDefaults(true);
        ConfigSetup.checkForMissingProperties();
        ConfigSetup.saveConfig();
        initConfigProvider();
        ConfigSetup.copySampleConfig();

        initCommands();

        // Run plugin startup logic after server is done loading
        Bukkit.getScheduler().scheduleSyncDelayedTask(QSFindItemAddOnOG.getInstance(), this::runPluginStartupTasks);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic.
        if (qsApi != null) {
            ShopSearchActivityStorageUtil.saveShopsToFile();
        } else if (!ENABLE_TRIAL_PERIOD) {
            logger(
                    "Uh oh! Looks like either this plugin has crashed or you don't have QuickShop-Hikari or QuickShop-Reremake installed.");
        }
        logger("Bye!");
    }

    private void runPluginStartupTasks() {

        serverVersion = Bukkit.getServer().getVersion();
        logger("Server version found: " + serverVersion);

        if (!isQSReremakeInstalled() && !isQSHikariInstalled()) {
            logger("QuickShop is required to use this addon. Please install QuickShop and try again!");
            logger("Both QuickShop-Hikari and QuickShop-Reremake are supported by this addon.");
            logger("Download links:");
            logger("» QuickShop-Hikari: https://www.spigotmc.org/resources/100125");
            logger("» QuickShop-Reremake (Support ending soon): https://www.spigotmc.org/resources/62575");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else if (isQSReremakeInstalled()) {
            logger("Found QuickShop-Reremake");
            qsApi = new QSReremakeAPIHandler();
            qsApi.registerSubCommand();
        } else {
            logger("Found QuickShop-Hikari");
            qsApi = new QSHikariAPIHandler();
            qsApi.registerSubCommand();
        }

        // Load all hidden shops from file.
        ShopSearchActivityStorageUtil.loadShopsFromFile();

        // v2.0.0.0 - Migrating hiddenShops.json to shops.json
        ShopSearchActivityStorageUtil.migrateHiddenShopsToShopsJson();

        // Setup optional dependencies
        PlayerWarpsPlugin.setup();
        EssentialsXPlugin.setup();
        WGPlugin.setup();
        ResidencePlugin.setup();

        initExternalPluginEventListeners();

        // Initiate batch tasks.
        logger("Registering tasks");
        Bukkit.getServer()
                .getScheduler()
                .scheduleSyncRepeatingTask(this, new Task15MinInterval(), 0, REPEATING_TASK_SCHEDULE_MINS);
    }

    private void initCommands() {
        logger("Registering commands");
        initFindItemCmd();
        initFindItemAdminCmd();
    }

    private void initBukkitEventListeners() {
        logger("Registering Bukkit event listeners");
        this.getServer().getPluginManager().registerEvents(new PluginEnableEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerCommandSendEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    private void initExternalPluginEventListeners() {
        logger("Registering external plugin event listeners");
        if (PlayerWarpsPlugin.getIsEnabled()) {
            this.getServer().getPluginManager().registerEvents(new PWPlayerWarpRemoveEventListener(), this);
            this.getServer().getPluginManager().registerEvents(new PWPlayerWarpCreateEventListener(), this);
        }
    }

    public static void initConfigProvider() {
        configProvider = new ConfigProvider();
    }

    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (playerMenuUtilityMap.containsKey(p)) {
            return playerMenuUtilityMap.get(p);
        } else {
            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);
            return playerMenuUtility;
        }
    }

    public static boolean getPluginOutdated() {
        return isPluginOutdated;
    }

    public static int getPluginID() {
        return SPIGOT_PLUGIN_ID;
    }

    private void initFindItemCmd() {
        List<String> alias;
        if (StringUtils.isEmpty(QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE)
                || StringUtils.containsIgnoreCase(
                        QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE, " ")) {
            alias = Arrays.asList("shopsearch", "searchshop", "searchitem");
        } else {
            alias = QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_COMMAND_ALIAS;
        }

        PluginCommand cmd = this.getCommand("finditem");
        if (cmd != null) {
            FindItemCommandExecutor executor = new FindItemCommandExecutor(alias);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
            cmd.setAliases(alias);
            logger("Registered /finditem command");
        } else {
            logger("Unable to register /finditem command; define it in plugin.yml");
        }
    }

    private void initFindItemAdminCmd() {
        List<String> alias = List.of("fiadmin");
        PluginCommand cmd = this.getCommand("finditemadmin");
        if (cmd != null) {
            FindItemAdminCommandExecutor executor = new FindItemAdminCommandExecutor(alias);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
            cmd.setAliases(alias);
            logger("Registered /finditemadmin command");
        } else {
            logger("Unable to register /finditemadmin command; define it in plugin.yml");
        }
    }

    public static boolean isQSReremakeInstalled() {
        return qSReremakeInstalled;
    }

    public static boolean isQSHikariInstalled() {
        return qSHikariInstalled;
    }

    public static void setQSReremakeInstalled(boolean qSReremakeInstalled) {
        QSFindItemAddOnOG.qSReremakeInstalled = qSReremakeInstalled;
    }

    public static void setQSHikariInstalled(boolean qSHikariInstalled) {
        QSFindItemAddOnOG.qSHikariInstalled = qSHikariInstalled;
    }

    @SuppressWarnings("rawtypes")
    public static QSApi getQsApiInstance() {
        return qsApi;
    }

    public static void logger(String message) {
        UtilitiesOG.logToConsole("[QSFindItemAddOn-OG", message);
    }

    private static class FindItemCommandExecutor implements CommandExecutor, TabCompleter {

        private final List<String> alias;

        FindItemCommandExecutor(List<String> alias) {
            this.alias = alias;
        }

        private void send(CommandSender sender, String message) {
            if (sender instanceof Player player) {
                UtilitiesOG.trueogMessage(player, message);
            } else {
                UtilitiesOG.logToConsole("[FindItem]", message);
            }
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 0) {
                send(sender, "");
                send(sender, "&7------------------------");
                send(sender, "&6&lShop Search Commands");
                send(sender, "&7------------------------");
                send(sender, "&#ff9933/finditem buy <item> &#a3a3c2Search shops buying your item");
                send(sender, "&#ff9933/finditem sell <item> &#a3a3c2Search shops selling your item");
                if (!QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_REMOVE_HIDE_REVEAL_SUBCMDS) {
                    send(sender, "&#ff9933/finditem hideshop &#a3a3c2Hide your shop");
                    send(sender, "&#ff9933/finditem revealshop &#a3a3c2Reveal your shop");
                }
                send(sender, "");
                send(sender, "&#b3b300Command alias:");
                alias.forEach(a -> send(sender, "&8&l» &#2db300/" + a));
                send(sender, "");
                return true;
            }
            send(sender, "&cUnknown subcommand. Use /finditem for help.");
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) {
                if (QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_REMOVE_HIDE_REVEAL_SUBCMDS) {
                    return List.of("buy", "sell");
                }
                return List.of("buy", "sell", "hideshop", "revealshop");
            }
            return List.of();
        }
    }

    private static class FindItemAdminCommandExecutor implements CommandExecutor, TabCompleter {

        private final List<String> alias;

        FindItemAdminCommandExecutor(List<String> alias) {
            this.alias = alias;
        }

        private void send(CommandSender sender, String message) {
            if (sender instanceof Player player) {
                UtilitiesOG.trueogMessage(player, message);
            } else {
                UtilitiesOG.logToConsole("[FindItem]", message);
            }
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender.isOp()
                    || sender.hasPermission(PlayerPermsEnum.FINDITEM_ADMIN.value())
                    || sender.hasPermission(PlayerPermsEnum.FINDITEM_RELOAD.value()))) {
                send(sender, "&cYou do not have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                send(sender, "");
                send(sender, "&7-----------------------------");
                send(sender, "&6&lShop Search Admin Commands");
                send(sender, "&7-----------------------------");
                send(sender, "&#ff1a1a/finditemadmin reload &#a3a3c2Reload configuration");
                send(sender, "");
                send(sender, "&#b3b300Command alias:");
                alias.forEach(a -> send(sender, "&8&l» &#2db300/" + a));
                send(sender, "");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                QSFindItemAddOnOG plugin =
                        QSFindItemAddOnOG.getInstance() instanceof QSFindItemAddOnOG fia ? fia : null;
                if (plugin != null) {
                    plugin.reloadConfig();
                    ConfigSetup.reloadConfig();
                    initConfigProvider();
                    send(sender, "&aConfiguration reloaded.");
                }
                return true;
            }

            send(sender, "&cUnknown subcommand. Use /finditemadmin for help.");
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (args.length == 1) return List.of("reload");
            return List.of();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
