package io.myzticbean.finditemaddon.Commands.SAPICommands;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import io.myzticbean.finditemaddon.Handlers.CommandHandler.CmdExecutorHandler;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import me.kodysimpson.simpapi.command.SubCommand;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub Command Handler for /finditem TO_BUY
 * 
 * @author myzticbean
 */
public class BuySubCmd extends SubCommand {

    private final String buySubCommand;
    private final List<String> itemsList = new ArrayList<>();
    private final CmdExecutorHandler cmdExecutor;

    public BuySubCmd() {

        // to-buy
        if (StringUtils.isEmpty(FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_BUY_AUTOCOMPLETE) || StringUtils
                .containsIgnoreCase(FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_BUY_AUTOCOMPLETE, " "))
        {

            buySubCommand = "TO_BUY";

        } else {

            buySubCommand = FindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_BUY_AUTOCOMPLETE;

        }

        if (itemsList.isEmpty()) {

            for (Material mat : Material.values()) {

                itemsList.add(mat.name());

            }

        }

        cmdExecutor = new CmdExecutorHandler();

    }

    @Override
    public String getName() {

        return buySubCommand;

    }

    @Override
    public List<String> getAliases() {

        return null;

    }

    @Override
    public String getDescription() {

        return "Find shops that buy a specific item";

    }

    @Override
    public String getSyntax() {

        return "/finditem " + buySubCommand + " {item type | item name}";

    }

    @Override
    public void perform(CommandSender commandSender, String[] args) {

        if (args.length != 2)
            commandSender
                    .sendMessage(ColorTranslator.translateColorCodes(FindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                            + FindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INCORRECT_USAGE_MSG));
        else
            cmdExecutor.handleShopSearch(buySubCommand, commandSender, args[1]);

    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {

        List<String> result = new ArrayList<>();
        for (String a : itemsList) {

            if (a.toLowerCase().startsWith(args[1].toLowerCase())) {

                result.add(a);

            }

        }

        return result;

    }

}
