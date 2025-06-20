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

import io.myzticbean.finditemaddon.QSFindItemAddOnOG;
import io.myzticbean.finditemaddon.handlers.command.CmdExecutorHandler;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Sub Command Handler for /finditemadmin revealshop
 * @author myzticbean
 */
public class RevealShopSubCmd implements CommandExecutor, TabCompleter {

    private final String revealShopSubCommand;
    private final CmdExecutorHandler cmdExecutor;

    public RevealShopSubCmd() {
        if (StringUtils.isEmpty(QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_REVEALSHOP_AUTOCOMPLETE)
                || StringUtils.containsIgnoreCase(
                        QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_REVEALSHOP_AUTOCOMPLETE, " ")) {
            revealShopSubCommand = "revealshop";
        } else {
            revealShopSubCommand = QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_REVEALSHOP_AUTOCOMPLETE;
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
        return "/finditem " + revealShopSubCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase(revealShopSubCommand)) {
            cmdExecutor.handleRevealShop(sender);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && revealShopSubCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
            return Collections.singletonList(revealShopSubCommand);
        }
        return Collections.emptyList();
    }

    public List<String> getSubcommandArguments(Player player, String[] strings) {
        return null;
    }
}
