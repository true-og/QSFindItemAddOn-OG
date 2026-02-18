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
 * Sub Command Handler for /find hideshop
 * 
 * @author myzticbean
 */
public class HideShopSubCmd implements CommandExecutor, TabCompleter {

    private final String hideSubCommand;
    private final CmdExecutorHandler cmdExecutor;

    public HideShopSubCmd() {

        if (StringUtils.isBlank(FindItemAddOnOG.getConfigProvider().FIND_ITEM_HIDESHOP_AUTOCOMPLETE)) {

            hideSubCommand = "hideshop";

        } else {

            hideSubCommand = FindItemAddOnOG.getConfigProvider().FIND_ITEM_HIDESHOP_AUTOCOMPLETE;

        }

        cmdExecutor = new CmdExecutorHandler();

    }

    public String getName() {

        return hideSubCommand;

    }

    public List<String> getAliases() {

        return null;

    }

    public String getDescription() {

        return "Run this command while looking at the shop (NOT the shop sign) you wish to hide and it will no"
                + " longer appear in searches";

    }

    public String getSyntax() {

        return "/find " + hideSubCommand;

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        cmdExecutor.handleHideShop(commandSender);
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        return List.of();

    }

}