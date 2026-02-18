package io.myzticbean.finditemaddon;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import io.myzticbean.finditemaddon.Commands.SAPICommands.BuySubCmd;
import io.myzticbean.finditemaddon.Commands.SAPICommands.HideShopSubCmd;
import io.myzticbean.finditemaddon.Commands.SAPICommands.ReloadSubCmd;
import io.myzticbean.finditemaddon.Commands.SAPICommands.RevealShopSubCmd;
import io.myzticbean.finditemaddon.Commands.SAPICommands.SellSubCmd;
import io.myzticbean.finditemaddon.ConfigUtil.ConfigProvider;
import io.myzticbean.finditemaddon.ConfigUtil.ConfigSetup;
import io.myzticbean.finditemaddon.Dependencies.EssentialsXPlugin;
import io.myzticbean.finditemaddon.Dependencies.WGPlugin;
import io.myzticbean.finditemaddon.Handlers.GUIHandler.PlayerMenuUtility;
import io.myzticbean.finditemaddon.Listeners.MenuListener;
import io.myzticbean.finditemaddon.Listeners.PlayerCommandSendEventListener;
import io.myzticbean.finditemaddon.Listeners.PluginEnableEventListener;
import io.myzticbean.finditemaddon.QuickShopHandler.QSApi;
import io.myzticbean.finditemaddon.QuickShopHandler.QSHikariAPIHandler;
import io.myzticbean.finditemaddon.QuickShopHandler.QSReremakeAPIHandler;
import io.myzticbean.finditemaddon.ScheduledTasks.Task15MinInterval;
import io.myzticbean.finditemaddon.Utils.LoggerUtils;
import io.myzticbean.finditemaddon.Utils.Defaults.PlayerPerms;
import io.myzticbean.finditemaddon.Utils.JsonStorageUtils.ShopSearchActivityStorageUtil;
import net.trueog.utilitiesog.UtilitiesOG;

public final class FindItemAddOnOG extends JavaPlugin {

    private static Plugin plugin;

    public FindItemAddOnOG() {

        plugin = this;

    }

    public static Plugin getInstance() {

        return plugin;

    }

    public static String serverVersion;
    private static final int SPIGOT_PLUGIN_ID = 95104;
    private static final int REPEATING_TASK_SCHEDULE_MINS = 15 * 60 * 20;
    private static ConfigProvider configProvider;
    private static final boolean isPluginOutdated = false;
    private static boolean qSReremakeInstalled = false;
    private static boolean qSHikariInstalled = false;
    @SuppressWarnings("rawtypes")
    private static QSApi qsApi;

    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();

    @Override
    public void onLoad() {

        LoggerUtils.logInfo("A Shop Search AddOn for QuickShop developed by myzticbean");

        // Show warning if it's a snapshot build
        if (!this.getPluginMeta().getVersion().toLowerCase().contains("snapshot")) {

            return;

        }

        LoggerUtils.logWarning("This is a SNAPSHOT build! NOT recommended for production servers.");
        LoggerUtils.logWarning(
                "If you find any bugs, please report them here: https://github.com/myzticbean/QSFindItemAddOn/issues");

    }

    @Override
    public void onEnable() {

        if (!Bukkit.getPluginManager().isPluginEnabled("QuickShop-OG-Alt")
                && !Bukkit.getPluginManager().isPluginEnabled("QuickShop-OG"))
        {

            LoggerUtils.logInfo("Delaying QuickShop hook as they are not enabled yet");

        } else if (Bukkit.getPluginManager().isPluginEnabled("QuickShop-OG-Alt")) {

            qSReremakeInstalled = true;

        } else {

            qSHikariInstalled = true;

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
        Bukkit.getScheduler().scheduleSyncDelayedTask(FindItemAddOnOG.getInstance(), this::runPluginStartupTasks);

    }

    @Override
    public void onDisable() {

        // Plugin shutdown logic
        if (qsApi != null) {

            ShopSearchActivityStorageUtil.saveShopsToFile();

        } else {

            LoggerUtils.logError(
                    "Uh oh! Looks like either this plugin has crashed or you don't have QuickShop or QuickShop-Hikari installed.");

        }

        LoggerUtils.logInfo("Bye!");

    }

    private void runPluginStartupTasks() {
        // if(!Bukkit.getPluginManager().isPluginEnabled("QuickShop")

        // && !Bukkit.getPluginManager().isPluginEnabled("QuickShop-Hikari")) {
        // LoggerUtils.logError("QuickShop is required to use this addon. Please install
        // QuickShop and try again!");
        // LoggerUtils.logError("Both QuickShop-Reremake and QuickShop-Hikari are
        // supported by this addon.");
        // LoggerUtils.logError("Download links:");
        // LoggerUtils.logError("» QuickShop-Reremake:
        // https://www.spigotmc.org/resources/62575");
        // LoggerUtils.logError("» QuickShop-Hikari:
        // https://www.spigotmc.org/resources/100125");
        // getServer().getPluginManager().disablePlugin(this);
        // return;
        // }
        // else if(Bukkit.getPluginManager().isPluginEnabled("QuickShop")) {
        // qSReremakeInstalled = true;
        // qsApi = new QSReremakeAPIHandler();
        // LoggerUtils.logInfo("Found QuickShop-Reremake");
        // }
        // else if(Bukkit.getPluginManager().isPluginEnabled("QuickShop-Hikari")) {
        // qSHikariInstalled = true;
        // qsApi = new QSHikariAPIHandler();
        // LoggerUtils.logInfo("Found QuickShop-Hikari");
        // }

        serverVersion = Bukkit.getServer().getVersion();
        LoggerUtils.logInfo("Server version found: " + serverVersion);

        if (!isQSReremakeInstalled() && !isQSHikariInstalled()) {

            LoggerUtils.logError("QuickShop is required to use this addon. Please install QuickShop and try again!");
            LoggerUtils.logError("Both QuickShop-Reremake and QuickShop-Hikari are supported by this addon.");
            LoggerUtils.logError("Download links:");
            LoggerUtils.logError("» QuickShop-Reremake: https://www.spigotmc.org/resources/62575");
            LoggerUtils.logError("» QuickShop-Hikari: https://www.spigotmc.org/resources/100125");
            getServer().getPluginManager().disablePlugin(this);
            return;

        } else if (isQSReremakeInstalled()) {

            LoggerUtils.logInfo("Found QuickShop-Reremake");
            qsApi = new QSReremakeAPIHandler();
            qsApi.registerSubCommand();

        } else {

            LoggerUtils.logInfo("Found QuickShop-Hikari");
            qsApi = new QSHikariAPIHandler();
            qsApi.registerSubCommand();

        }

        // Load all hidden shops from file
        ShopSearchActivityStorageUtil.loadShopsFromFile();

        // v2.0.0 - Migrating hiddenShops.json to shops.json
        ShopSearchActivityStorageUtil.migrateHiddenShopsToShopsJson();

        EssentialsXPlugin.setup();
        WGPlugin.setup();

        // Initiate batch tasks
        LoggerUtils.logInfo("Registering tasks");
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Task15MinInterval(), 0,
                REPEATING_TASK_SCHEDULE_MINS);

    }

    private void initCommands() {

        LoggerUtils.logInfo("Registering commands");
        initFindItemCmd();
        initFindItemAdminCmd();

    }

    private void initBukkitEventListeners() {

        LoggerUtils.logInfo("Registering Bukkit event listeners");
        this.getServer().getPluginManager().registerEvents(new PluginEnableEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerCommandSendEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);

    }

    public static ConfigProvider getConfigProvider() {

        return configProvider;

    }

    public static void initConfigProvider() {

        configProvider = new ConfigProvider();

    }

    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {

        final PlayerMenuUtility playerMenuUtility;
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

        final List<String> alias;
        if (StringUtils.isEmpty(FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE) || StringUtils
                .containsIgnoreCase(FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE, " "))
        {

            alias = Arrays.asList("shopsearch", "searchshop", "searchitem");

        } else {

            alias = FindItemAddOnOG.getConfigProvider().FIND_ITEM_COMMAND_ALIAS;

        }

        LoggerUtils.logInfo("Registering /find command");
        registerBukkitCommand("find", alias, "Search for items from all shops using an interactive GUI", "/find",
                new FindCommandExecutor(alias), new FindCommandExecutor(alias));
        LoggerUtils.logInfo("Registered /find command");

    }

    private void initFindItemAdminCmd() {

        final List<String> alias = List.of("fiadmin");
        LoggerUtils.logInfo("Registering /finditemadmin command");
        registerBukkitCommand("finditemadmin", alias, "Admin command for Shop Search addon", "/finditemadmin",
                new FindItemAdminCommandExecutor(alias), new FindItemAdminCommandExecutor(alias));
        LoggerUtils.logInfo("Registered /finditemadmin command");

    }

    private void registerBukkitCommand(String name, List<String> aliases, String description, String usage,
            CommandExecutor executor, TabCompleter completer)
    {

        PluginCommand cmd = this.getCommand(name);
        if (cmd == null) {

            cmd = registerDynamicPluginCommand(name);

        }

        if (cmd == null) {

            LoggerUtils.logError("Failed to register command: " + name);
            return;

        }

        cmd.setDescription(description);
        cmd.setUsage(usage);

        if (aliases != null) {

            cmd.setAliases(aliases);

        }

        cmd.setExecutor(executor);
        cmd.setTabCompleter(completer);

    }

    private PluginCommand registerDynamicPluginCommand(String name) {

        try {

            final CommandMap commandMap = getCommandMap();
            if (commandMap == null) {

                return null;

            }

            final Constructor<PluginCommand> ctor = PluginCommand.class.getDeclaredConstructor(String.class,
                    Plugin.class);
            ctor.setAccessible(true);

            final PluginCommand cmd = ctor.newInstance(name, this);
            commandMap.register(this.getPluginMeta().getName(), cmd);
            return cmd;

        } catch (Throwable t) {

            LoggerUtils.logError(t.getMessage());
            t.printStackTrace();
            return null;

        }

    }

    private CommandMap getCommandMap() {

        try {

            final Method m = Bukkit.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) m.invoke(Bukkit.getServer());

        } catch (Throwable t) {

            LoggerUtils.logError(t.getMessage());
            t.printStackTrace();
            return null;

        }

    }

    private record FindCommandExecutor(List<String> aliases) implements CommandExecutor, TabCompleter {

        private FindCommandExecutor(List<String> aliases) {

            this.aliases = aliases == null ? Collections.emptyList() : aliases;

        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {

                sendHelp(sender);
                return true;

            }

            final String sub = args[0].toLowerCase();
            final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            final SellSubCmd sellSubCmd = new SellSubCmd();
            final BuySubCmd buySubCmd = new BuySubCmd();
            final HideShopSubCmd hideShopSubCmd = new HideShopSubCmd();
            final RevealShopSubCmd revealShopSubCmd = new RevealShopSubCmd();

            if (matchesSubCommand(sub, "sell", sellSubCmd)) {

                return invokeSubCommand(sellSubCmd, sender, command, label, subArgs);

            }

            if (matchesSubCommand(sub, "buy", buySubCmd)) {

                return invokeSubCommand(buySubCmd, sender, command, label, subArgs);

            }

            if (matchesSubCommand(sub, "hide", hideShopSubCmd) || "hideshop".equals(sub)) {

                return invokeSubCommand(hideShopSubCmd, sender, command, label, subArgs);

            }

            if (matchesSubCommand(sub, "reveal", revealShopSubCmd) || "revealshop".equals(sub)) {

                return invokeSubCommand(revealShopSubCmd, sender, command, label, subArgs);

            }

            sendHelp(sender);
            return true;

        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

            final SellSubCmd sellSubCmd = new SellSubCmd();
            final BuySubCmd buySubCmd = new BuySubCmd();
            final HideShopSubCmd hideShopSubCmd = new HideShopSubCmd();
            final RevealShopSubCmd revealShopSubCmd = new RevealShopSubCmd();

            if (args.length == 1) {

                return partialMatches(args[0], Arrays.asList(getSubCommandName(sellSubCmd), getSubCommandName(buySubCmd),
                        getSubCommandName(hideShopSubCmd), getSubCommandName(revealShopSubCmd), "sell", "buy", "hide",
                        "reveal", "help", "hideshop", "revealshop"));

            }

            if (args.length >= 2) {

                final String sub = args[0].toLowerCase();
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                if (matchesSubCommand(sub, "sell", sellSubCmd)) {

                    return invokeTabComplete(sellSubCmd, sender, command, alias, subArgs);

                }

                if (matchesSubCommand(sub, "buy", buySubCmd)) {

                    return invokeTabComplete(buySubCmd, sender, command, alias, subArgs);

                }

                if (matchesSubCommand(sub, "hide", hideShopSubCmd) || "hideshop".equals(sub)) {

                    return invokeTabComplete(hideShopSubCmd, sender, command, alias, subArgs);

                }

                if (matchesSubCommand(sub, "reveal", revealShopSubCmd) || "revealshop".equals(sub)) {

                    return invokeTabComplete(revealShopSubCmd, sender, command, alias, subArgs);

                }

            }

            return Collections.emptyList();

        }

        private void sendHelp(CommandSender commandSender) {

            commandSender.sendMessage(UtilitiesOG.trueogColorize(""));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("&7------------------------"));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("&6&lShop Search Commands"));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("&7------------------------"));

            final List<Object> subCommands = Arrays.asList(new SellSubCmd(), new BuySubCmd(), new HideShopSubCmd(),
                    new RevealShopSubCmd());

            subCommands.forEach(subCommand -> commandSender.sendMessage(UtilitiesOG.trueogColorize("<#ff9933>"
                    + getSubCommandSyntax(subCommand) + " <#a3a3c2>" + getSubCommandDescription(subCommand))));

            commandSender.sendMessage(UtilitiesOG.trueogColorize(""));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("<#b3b300>Command alias:"));
            aliases.forEach(
                    alias_i -> commandSender.sendMessage(UtilitiesOG.trueogColorize("&8&l» <#2db300>/" + alias_i)));
            commandSender.sendMessage(UtilitiesOG.trueogColorize(""));

        }

    }

    private record FindItemAdminCommandExecutor(List<String> aliases) implements CommandExecutor, TabCompleter {

        private FindItemAdminCommandExecutor(List<String> aliases) {

            this.aliases = aliases == null ? Collections.emptyList() : aliases;

        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            if (!((sender.isOp()) || (!sender.isOp() && (sender.hasPermission(PlayerPerms.FINDITEM_ADMIN.value())
                    || sender.hasPermission(PlayerPerms.FINDITEM_RELOAD.value())))))
            {

                return true;

            }

            if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {

                sendHelp(sender);
                return true;

            }

            final String sub = args[0].toLowerCase();
            final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

            if ("reload".equals(sub)) {

                return invokeSubCommand(new ReloadSubCmd(), sender, command, label, subArgs);

            }

            sendHelp(sender);
            return true;

        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

            if (!((sender.isOp()) || (!sender.isOp() && (sender.hasPermission(PlayerPerms.FINDITEM_ADMIN.value())
                    || sender.hasPermission(PlayerPerms.FINDITEM_RELOAD.value())))))
            {

                return Collections.emptyList();

            }

            if (args.length == 1) {

                return partialMatches(args[0], Arrays.asList("reload", "help"));

            }

            if (args.length >= 2) {

                final String sub = args[0].toLowerCase();
                final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

                if ("reload".equals(sub)) {

                    return invokeTabComplete(new ReloadSubCmd(), sender, command, alias, subArgs);

                }

            }

            return Collections.emptyList();

        }

        private void sendHelp(CommandSender commandSender) {

            commandSender.sendMessage(UtilitiesOG.trueogColorize(""));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("&7-----------------------------"));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("&6&lShop Search Admin Commands"));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("&7-----------------------------"));

            final List<Object> subCommands = Collections.singletonList(new ReloadSubCmd());

            subCommands.forEach(subCommand -> commandSender.sendMessage(UtilitiesOG.trueogColorize("<#ff1a1a>"
                    + getSubCommandSyntax(subCommand) + " <#a3a3c2>" + getSubCommandDescription(subCommand))));

            commandSender.sendMessage(UtilitiesOG.trueogColorize(""));
            commandSender.sendMessage(UtilitiesOG.trueogColorize("<#b3b300>Command alias:"));
            aliases.forEach(
                    alias_i -> commandSender.sendMessage(UtilitiesOG.trueogColorize("&8&l» <#2db300>/" + alias_i)));
            commandSender.sendMessage(UtilitiesOG.trueogColorize(""));

        }

    }

    private static boolean invokeSubCommand(Object subCommand, CommandSender sender, Command command, String label,
            String[] args)
    {

        if (subCommand instanceof CommandExecutor) {

            return ((CommandExecutor) subCommand).onCommand(sender, command, label, args);

        }

        try {

            final Method m = subCommand.getClass().getMethod("execute", CommandSender.class, String[].class);
            final Object res = m.invoke(subCommand, sender, args);
            if (res instanceof Boolean) {

                return (Boolean) res;

            }

            return true;

        } catch (Throwable ignored) {

        }

        try {

            final Method m = subCommand.getClass().getMethod("execute", CommandSender.class, Command.class,
                    String.class, String[].class);
            final Object res = m.invoke(subCommand, sender, command, label, args);
            if (res instanceof Boolean) {

                return (Boolean) res;

            }

            return true;

        } catch (Throwable ignored) {

        }

        try {

            final Method m = subCommand.getClass().getMethod("onCommand", CommandSender.class, Command.class,
                    String.class, String[].class);
            final Object res = m.invoke(subCommand, sender, command, label, args);
            if (res instanceof Boolean) {

                return (Boolean) res;

            }

            return true;

        } catch (Throwable t) {

            LoggerUtils.logError(t.getMessage());
            t.printStackTrace();
            return true;

        }

    }

    private static List<String> invokeTabComplete(Object subCommand, CommandSender sender, Command command,
            String label, String[] args)
    {

        if (subCommand instanceof TabCompleter) {

            final List<String> res = ((TabCompleter) subCommand).onTabComplete(sender, command, label, args);
            return res == null ? Collections.emptyList() : res;

        }

        try {

            final Method m = subCommand.getClass().getMethod("onTabComplete", CommandSender.class, Command.class,
                    String.class, String[].class);
            @SuppressWarnings("unchecked")
            final List<String> res = (List<String>) m.invoke(subCommand, sender, command, label, args);
            return res == null ? Collections.emptyList() : res;

        } catch (Throwable ignored) {

        }

        return Collections.emptyList();

    }

    private static String getSubCommandSyntax(Object subCommand) {

        try {

            final Method m = subCommand.getClass().getMethod("getSyntax");
            final Object res = m.invoke(subCommand);
            return res == null ? "" : String.valueOf(res);

        } catch (Throwable ignored) {

        }

        return "";

    }

    private static String getSubCommandName(Object subCommand) {

        try {

            final Method m = subCommand.getClass().getMethod("getName");
            final Object res = m.invoke(subCommand);
            return res == null ? "" : String.valueOf(res);

        } catch (Throwable ignored) {

        }

        return "";

    }

    private static boolean matchesSubCommand(String input, String fallbackCommand, Object subCommand) {

        if (fallbackCommand.equalsIgnoreCase(input)) {

            return true;

        }

        final String subCommandName = getSubCommandName(subCommand);
        return !subCommandName.isBlank() && subCommandName.equalsIgnoreCase(input);

    }

    private static String getSubCommandDescription(Object subCommand) {

        try {

            final Method m = subCommand.getClass().getMethod("getDescription");
            final Object res = m.invoke(subCommand);
            return res == null ? "" : String.valueOf(res);

        } catch (Throwable ignored) {

        }

        return "";

    }

    private static List<String> partialMatches(String token, List<String> options) {

        if (token == null || token.isEmpty()) {

            return options;

        }

        final String lower = token.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();

    }

    public static boolean isQSReremakeInstalled() {

        return qSReremakeInstalled;

    }

    public static boolean isQSHikariInstalled() {

        return qSHikariInstalled;

    }

    public static void setQSReremakeInstalled(boolean qSReremakeInstalled) {

        FindItemAddOnOG.qSReremakeInstalled = qSReremakeInstalled;

    }

    public static void setQSHikariInstalled(boolean qSHikariInstalled) {

        FindItemAddOnOG.qSHikariInstalled = qSHikariInstalled;

    }

    @SuppressWarnings("rawtypes")
    public static QSApi getQsApiInstance() {

        return qsApi;

    }

}
