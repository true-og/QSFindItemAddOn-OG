package io.myzticbean.finditemaddon.Commands.SAPICommands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.myzticbean.finditemaddon.Handlers.CommandHandler.CmdExecutorHandler;

/**
 * Sub Command Handler for /find restart
 * 
 * @deprecated No longer used, will be removed in future versions
 * @author myzticbean
 */
@Deprecated
public class RestartSubCmd implements CommandExecutor, TabCompleter {

    private final CmdExecutorHandler cmdExecutor;

    public RestartSubCmd() {

        cmdExecutor = new CmdExecutorHandler();

    }

    public String getName() {

        return "restart";

    }

    public List<String> getAliases() {

        return null;

    }

    public String getDescription() {

        return "Restarts the plugin (NOT recommended in most cases, restart server if necessary)";

    }

    public String getSyntax() {

        return "/finditemadmin restart";

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        cmdExecutor.handlePluginRestart(commandSender);
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return List.of();

    }

}