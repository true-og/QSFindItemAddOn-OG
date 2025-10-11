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
import me.kodysimpson.simpapi.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Sub Command Handler for /finditemadmin debug-mode
 * @author myzticbean
 */
public class DebugSubCmd extends SubCommand {

    private final CmdExecutorHandler cmdExecutor;

    public DebugSubCmd() {
        cmdExecutor = new CmdExecutorHandler();
    }

    @Override
    public String getName() {
        return "debug-mode";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Enables / disables debug mode";
    }

    @Override
    public String getSyntax() {
        return "/finditemadmin debug-mode {enable | disable}";
    }

    @Override
    public void perform(CommandSender commandSender, String[] args) {
        cmdExecutor.handleDebugMode(commandSender, args);
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] strings) {
        return List.of("enable", "disable");
    }
}
