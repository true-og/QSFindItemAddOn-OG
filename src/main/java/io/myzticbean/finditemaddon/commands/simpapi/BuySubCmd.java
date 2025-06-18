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
import java.util.List;
import net.trueog.utilitiesog.UtilitiesOG;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * Sub Command Handler for /finditem TO_BUY
 * @author myzticbean
 */
public final class BuySubCmd implements TabExecutor {

    private final String subName;
    private final List<String> materials = new ArrayList<>();
    private final CmdExecutorHandler executor = new CmdExecutorHandler();

    public BuySubCmd() {
        var cfg = FindItemAddOn.getConfigProvider();
        subName = StringUtils.isBlank(cfg.FIND_ITEM_TO_BUY_AUTOCOMPLETE)
                        || StringUtils.containsAny(cfg.FIND_ITEM_TO_BUY_AUTOCOMPLETE, ' ')
                ? "TO_BUY"
                : cfg.FIND_ITEM_TO_BUY_AUTOCOMPLETE;
        materials.addAll(Arrays.stream(Material.values())
                .filter(mat -> !cfg.getBlacklistedMaterials().contains(mat))
                .map(Material::name)
                .toList());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            if (args.length != 2 || !args[0].equalsIgnoreCase(subName)) {

                UtilitiesOG.trueogMessage(
                        (Player) sender,
                        FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                                + FindItemAddOn.getConfigProvider().FIND_ITEM_CMD_INCORRECT_USAGE_MSG);

                return true;
            }

            executor.handleShopSearch(subName, sender, args[1]);

            return true;

        } else {

            UtilitiesOG.logToConsole(
                    "QSFindItemAddOn-OG", "&4ERROR: That command can only be run by a player in-game.");

            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return List.of();
        if (args.length == 1) {
            return subName.toLowerCase().startsWith(args[0].toLowerCase()) ? List.of(subName) : List.of();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(subName)) {
            String partial = args[1].toLowerCase();
            List<String> out = new ArrayList<>();
            for (String m : materials) {
                if (m.toLowerCase().startsWith(partial)) out.add(m);
            }
            return out;
        }
        return List.of();
    }

    public String getSubName() {
        return subName;
    }
}
