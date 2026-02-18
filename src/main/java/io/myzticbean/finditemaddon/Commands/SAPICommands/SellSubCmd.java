package io.myzticbean.finditemaddon.Commands.SAPICommands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import io.myzticbean.finditemaddon.Handlers.CommandHandler.CmdExecutorHandler;
import net.trueog.utilitiesog.UtilitiesOG;

/**
 * Sub Command Handler for /find TO_SELL
 * 
 * @author myzticbean
 */
public class SellSubCmd implements CommandExecutor, TabCompleter {

    private final String sellSubCommand;
    private final List<String> itemsList = new ArrayList<>();
    private final CmdExecutorHandler cmdExecutor;

    public SellSubCmd() {

        // to-sell
        if (StringUtils.isBlank(FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE)) {

            sellSubCommand = "TO_SELL";

        } else {

            sellSubCommand = FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE;

        }

        if (itemsList.isEmpty()) {

            for (Material mat : Material.values()) {

                itemsList.add(mat.name());

            }

        }

        cmdExecutor = new CmdExecutorHandler();

    }

    public String getName() {

        return sellSubCommand;

    }

    public List<String> getAliases() {

        return null;

    }

    public String getDescription() {

        return "Find shops that sell a specific item";

    }

    public String getSyntax() {

        return "/find " + sellSubCommand + " {item type | item name}";

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        final String itemArg;
        if (args.length == 1) {

            itemArg = args[0];

        } else if (args.length == 2 && sellSubCommand.equalsIgnoreCase(args[0])) {

            itemArg = args[1];

        } else {

            commandSender.sendMessage(UtilitiesOG.trueogColorize(FindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                    + FindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INCORRECT_USAGE_MSG));
            return true;

        }

        cmdExecutor.handleShopSearch(sellSubCommand, commandSender, itemArg);
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        final String token;
        if (args.length == 1) {

            token = args[0];

        } else if (args.length == 2 && sellSubCommand.equalsIgnoreCase(args[0])) {

            token = args[1];

        } else {

            return List.of();

        }

        final String lower = token == null ? "" : token.toLowerCase();
        return new ArrayList<>(
                itemsList.stream().filter(a -> a.toLowerCase().startsWith(lower)).collect(Collectors.toList()));

    }

}