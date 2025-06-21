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
 * Sub Command Handler for /finditem hideshop
 *
 * @author myzticbean
 */
public class HideShopSubCmd implements CommandExecutor, TabCompleter {

    private final String hideSubCommand;
    private final CmdExecutorHandler cmdExecutor;

    public HideShopSubCmd() {
        if (StringUtils.isBlank(QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_HIDESHOP_AUTOCOMPLETE)) {
            hideSubCommand = "hideshop";
        } else {
            hideSubCommand = QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_HIDESHOP_AUTOCOMPLETE;
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
        return "/finditem " + hideSubCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase(hideSubCommand)) {
            cmdExecutor.handleHideShop(sender);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && hideSubCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
            return Collections.singletonList(hideSubCommand);
        }
        return Collections.emptyList();
    }

    public List<String> getSubcommandArguments(Player player, String[] strings) {
        return null;
    }
}
