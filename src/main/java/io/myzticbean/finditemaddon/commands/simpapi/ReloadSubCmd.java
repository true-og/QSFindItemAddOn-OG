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

import io.myzticbean.finditemaddon.handlers.command.CmdExecutorHandler;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * Sub Command Handler for /finditemadmin reload
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            cmdExecutor.handlePluginReload(sender);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }

    public List<String> getSubcommandArguments(Player player, String[] strings) {
        return null;
    }
}
