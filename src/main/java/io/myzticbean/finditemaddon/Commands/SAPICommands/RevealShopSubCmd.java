package io.myzticbean.finditemaddon.Commands.SAPICommands;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import io.myzticbean.finditemaddon.Handlers.CommandHandler.CmdExecutorHandler;

/**
 * Sub Command Handler for /finditemadmin revealshop
 * 
 * @author myzticbean
 */
public class RevealShopSubCmd implements CommandExecutor, TabCompleter {

    private final String revealShopSubCommand;
    private final CmdExecutorHandler cmdExecutor;

    public RevealShopSubCmd() {

        if (StringUtils.isEmpty(FindItemAddOnOG.getConfigProvider().FIND_ITEM_REVEALSHOP_AUTOCOMPLETE) || StringUtils
                .containsIgnoreCase(FindItemAddOnOG.getConfigProvider().FIND_ITEM_REVEALSHOP_AUTOCOMPLETE, " "))
        {

            revealShopSubCommand = "revealshop";

        } else {

            revealShopSubCommand = FindItemAddOnOG.getConfigProvider().FIND_ITEM_REVEALSHOP_AUTOCOMPLETE;

        }

        cmdExecutor = new CmdExecutorHandler();

    }

    public String getName() {

        return revealShopSubCommand;

    }

    public List<String> getAliases() {

        return null;

    }

    public String getDescription() {

        return "Run this command while looking at a hidden shop to make it public again";

    }

    public String getSyntax() {

        return "/find " + revealShopSubCommand;

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        cmdExecutor.handleRevealShop(commandSender);
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return List.of();

    }

}