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
package io.myzticbean.finditemaddon.commands.simpapi;

import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.handlers.command.CmdExecutorHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.trueog.utilitiesog.UtilitiesOG;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class SellSubCmd implements CommandExecutor, TabCompleter {

    private final String sellSubCommand;
    private final List<String> itemsList = new ArrayList<>();
    private final CmdExecutorHandler cmdExecutor;

    public SellSubCmd() {
        if (StringUtils.isBlank(FindItemAddOn.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE)) {
            sellSubCommand = "TO_SELL";
        } else {
            sellSubCommand = FindItemAddOn.getConfigProvider().FIND_ITEM_TO_SELL_AUTOCOMPLETE;
        }
        if (itemsList.isEmpty()) {
            itemsList.addAll(Arrays.stream(Material.values())
                    .filter(mat -> !FindItemAddOn.getConfigProvider()
                            .getBlacklistedMaterials()
                            .contains(mat))
                    .map(Material::name)
                    .toList());
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
        return "/finditem " + sellSubCommand + " {item type | item name}";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase(sellSubCommand)) return false;
        if (!(sender instanceof Player player)) {
            UtilitiesOG.logToConsole("[FindItem]", "This command can only be used by players.");
            return true;
        }
        if (args.length != 2) {
            UtilitiesOG.trueogMessage(
                    player,
                    FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                            + FindItemAddOn.getConfigProvider().FIND_ITEM_CMD_INCORRECT_USAGE_MSG);
        } else {
            cmdExecutor.handleShopSearch(sellSubCommand, player, args[1]);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sellSubCommand.toLowerCase().startsWith(args[0].toLowerCase()))
                return Collections.singletonList(sellSubCommand);
        } else if (args.length == 2 && args[0].equalsIgnoreCase(sellSubCommand)) {
            List<String> result = new ArrayList<>();
            for (String a : itemsList) {
                if (a.toLowerCase().startsWith(args[1].toLowerCase())) result.add(a);
            }
            return result;
        }
        return Collections.emptyList();
    }

    public List<String> getSubcommandArguments(Player player, String[] args) {
        return null;
    }
}
