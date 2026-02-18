package io.myzticbean.finditemaddon.Commands.SAPICommands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.myzticbean.finditemaddon.Handlers.CommandHandler.CmdExecutorHandler;

/**
 * Sub Command Handler for /finditemadmin reload
 * 
 * @author myzticbean
 */
public class ReloadSubCmd implements CommandExecutor, TabCompleter {

    private final CmdExecutorHandler cmdExecutor;

    public ReloadSubCmd() {

        cmdExecutor = new CmdExecutorHandler();

    }

    public String getName() {

        return "reload";

    }

    public List<String> getAliases() {

        return null;

    }

    public String getDescription() {

        return "Reloads config.yml";

    }

    public String getSyntax() {

        return "/finditemadmin reload";

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        cmdExecutor.handlePluginReload(commandSender);
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return List.of();

    }

}