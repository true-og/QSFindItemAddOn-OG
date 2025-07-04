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
package io.myzticbean.finditemaddon.handlers.command;

import io.myzticbean.finditemaddon.QSFindItemAddOnOG;
import io.myzticbean.finditemaddon.config.ConfigSetup;
import io.myzticbean.finditemaddon.handlers.gui.menus.FoundShopsMenu;
import io.myzticbean.finditemaddon.models.FoundShopItemModel;
import io.myzticbean.finditemaddon.utils.enums.PlayerPermsEnum;
import io.myzticbean.finditemaddon.utils.json.HiddenShopStorageUtil;
import io.myzticbean.finditemaddon.utils.warp.WarpUtils;
import java.util.List;
import net.trueog.utilitiesog.UtilitiesOG;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.api.shop.Shop;

/**
 * Handler for different parameters of /finditem command
 *
 * @author myzticbean
 */
public class CmdExecutorHandler {

    private static final String THIS_COMMAND_CAN_ONLY_BE_RUN_FROM_IN_GAME = "This command can only be run from in game";

    /**
     * Handles the main shop search process
     *
     * @param buySellSubCommand Whether player is buying or selling
     * @param commandSender     Who is the command sender: console or player
     * @param itemArg           Specifies Item ID or Item name
     */
    public void handleShopSearch(String buySellSubCommand, CommandSender commandSender, String itemArg) {
        if (!(commandSender instanceof Player player)) {
            QSFindItemAddOnOG.logger(THIS_COMMAND_CAN_ONLY_BE_RUN_FROM_IN_GAME);
            return;
        }
        if (!player.hasPermission(PlayerPermsEnum.FINDITEM_USE.value())) {
            UtilitiesOG.trueogMessage(player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&cNo permission!");
            return;
        }

        // Show searching... message
        if (!StringUtils.isEmpty(QSFindItemAddOnOG.getConfigProvider().SHOP_SEARCH_LOADING_MSG)) {
            UtilitiesOG.trueogMessage(
                    player,
                    (QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                            + QSFindItemAddOnOG.getConfigProvider().SHOP_SEARCH_LOADING_MSG));
        }

        boolean isBuying;
        if (StringUtils.isEmpty(QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_BUY_AUTOCOMPLETE)
                || StringUtils.containsIgnoreCase(
                        QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_BUY_AUTOCOMPLETE, " ")) {
            isBuying = buySellSubCommand.equalsIgnoreCase("to_buy");
        } else {
            isBuying = buySellSubCommand.equalsIgnoreCase(
                    QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_TO_BUY_AUTOCOMPLETE);
        }

        if (itemArg.equalsIgnoreCase("*")
                && !QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_DISABLE_SEARCH_ALL_SHOPS) {
            // If QS Hikari installed and Shop Cache feature available (>6), then run in
            // async thread (Fix for Issue
            // #12)
            if (!QSFindItemAddOnOG.isQSReremakeInstalled()
                    && QSFindItemAddOnOG.getQsApiInstance().isQSShopCacheImplemented()) {
                QSFindItemAddOnOG.logger("Should run in async thread...");
                Bukkit.getScheduler().runTaskAsynchronously(QSFindItemAddOnOG.getInstance(), () -> {
                    @SuppressWarnings("unchecked")
                    List<FoundShopItemModel> searchResultList =
                            QSFindItemAddOnOG.getQsApiInstance().fetchAllItemsFromAllShops(isBuying, player);
                    this.openShopMenu(
                            player, searchResultList, true, QSFindItemAddOnOG.getConfigProvider().NO_SHOP_FOUND_MSG);
                });
            } else {
                // Else run in MAIN thread
                @SuppressWarnings("unchecked")
                List<FoundShopItemModel> searchResultList =
                        QSFindItemAddOnOG.getQsApiInstance().fetchAllItemsFromAllShops(isBuying, player);
                this.openShopMenu(
                        player, searchResultList, false, QSFindItemAddOnOG.getConfigProvider().NO_SHOP_FOUND_MSG);
            }
        } else {
            Material mat = Material.getMaterial(itemArg.toUpperCase());
            if (this.checkMaterialBlacklist(mat)) {
                UtilitiesOG.trueogMessage(
                        player,
                        QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&cThis material is not allowed.");
                return;
            }
            if (mat != null && mat.isItem()) {
                QSFindItemAddOnOG.logger("Material found: " + mat);
                // If QS Hikari installed and Shop Cache feature available (>6), then run in
                // async thread (Fix for Issue
                // #12)
                if (!QSFindItemAddOnOG.isQSReremakeInstalled()
                        && QSFindItemAddOnOG.getQsApiInstance().isQSShopCacheImplemented()) {
                    Bukkit.getScheduler().runTaskAsynchronously(QSFindItemAddOnOG.getInstance(), () -> {
                        @SuppressWarnings("unchecked")
                        List<FoundShopItemModel> searchResultList = QSFindItemAddOnOG.getQsApiInstance()
                                .findItemBasedOnTypeFromAllShops(new ItemStack(mat), isBuying, player);
                        this.openShopMenu(
                                player,
                                searchResultList,
                                true,
                                QSFindItemAddOnOG.getConfigProvider().NO_SHOP_FOUND_MSG);
                    });
                } else {
                    @SuppressWarnings("unchecked")
                    List<FoundShopItemModel> searchResultList = QSFindItemAddOnOG.getQsApiInstance()
                            .findItemBasedOnTypeFromAllShops(new ItemStack(mat), isBuying, player);
                    this.openShopMenu(
                            player, searchResultList, false, QSFindItemAddOnOG.getConfigProvider().NO_SHOP_FOUND_MSG);
                }
            } else {
                QSFindItemAddOnOG.logger("Material not found! Performing query based search..");
                // If QS Hikari installed and Shop Cache feature available (>6), then run in
                // async thread (Fix for Issue
                // #12)
                if (!QSFindItemAddOnOG.isQSReremakeInstalled()
                        && QSFindItemAddOnOG.getQsApiInstance().isQSShopCacheImplemented()) {
                    Bukkit.getScheduler().runTaskAsynchronously(QSFindItemAddOnOG.getInstance(), () -> {
                        @SuppressWarnings("unchecked")
                        List<FoundShopItemModel> searchResultList = QSFindItemAddOnOG.getQsApiInstance()
                                .findItemBasedOnDisplayNameFromAllShops(itemArg, isBuying, player);
                        this.openShopMenu(
                                player,
                                searchResultList,
                                true,
                                QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INVALID_MATERIAL_MSG);
                    });
                } else {
                    @SuppressWarnings("unchecked")
                    List<FoundShopItemModel> searchResultList = QSFindItemAddOnOG.getQsApiInstance()
                            .findItemBasedOnDisplayNameFromAllShops(itemArg, isBuying, player);
                    this.openShopMenu(
                            player,
                            searchResultList,
                            false,
                            QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INVALID_MATERIAL_MSG);
                }
            }
        }
    }

    private void openShopMenu(
            Player player, List<FoundShopItemModel> searchResultList, boolean synchronize, String errorMsg) {
        if (!searchResultList.isEmpty()) {
            if (synchronize) {
                Bukkit.getScheduler().runTask(QSFindItemAddOnOG.getInstance(), () -> {
                    FoundShopsMenu menu =
                            new FoundShopsMenu(QSFindItemAddOnOG.getPlayerMenuUtility(player), searchResultList);
                    menu.open(searchResultList);
                });
            } else {
                FoundShopsMenu menu =
                        new FoundShopsMenu(QSFindItemAddOnOG.getPlayerMenuUtility(player), searchResultList);
                menu.open(searchResultList);
            }
        } else {
            if (!StringUtils.isEmpty(errorMsg)) {
                UtilitiesOG.trueogMessage(player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + errorMsg);
            }
        }
    }

    private boolean checkMaterialBlacklist(Material mat) {
        return QSFindItemAddOnOG.getConfigProvider().getBlacklistedMaterials().contains(mat);
    }

    /**
     * Handles the shop hiding feature
     *
     * @param commandSender Who is the command sender: console or player
     */
    public void handleHideShop(CommandSender commandSender) {
        if (commandSender instanceof Player player) {
            if (player.hasPermission(PlayerPermsEnum.FINDITEM_HIDESHOP.value())) {
                Block playerLookAtBlock = player.getTargetBlock(null, 3);
                QSFindItemAddOnOG.logger("TargetBlock found: " + playerLookAtBlock.getType());
                if (QSFindItemAddOnOG.isQSReremakeInstalled()) {
                    hideReremakeShop(
                            (Shop) QSFindItemAddOnOG.getQsApiInstance().findShopAtLocation(playerLookAtBlock), player);
                } else {
                    hideHikariShop(
                            (com.ghostchu.quickshop.api.shop.Shop)
                                    QSFindItemAddOnOG.getQsApiInstance().findShopAtLocation(playerLookAtBlock),
                            player);
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&cNo permission!");
            }
        } else {
            QSFindItemAddOnOG.logger(THIS_COMMAND_CAN_ONLY_BE_RUN_FROM_IN_GAME);
        }
    }

    /**
     * Handles the shop reveal feature
     *
     * @param commandSender Who is the command sender: console or player
     */
    public void handleRevealShop(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            QSFindItemAddOnOG.logger(THIS_COMMAND_CAN_ONLY_BE_RUN_FROM_IN_GAME);
        } else {
            Player player = (Player) commandSender;
            if (player.hasPermission(PlayerPermsEnum.FINDITEM_HIDESHOP.value())) {
                Block playerLookAtBlock = player.getTargetBlock(null, 5);
                if (playerLookAtBlock != null) {
                    QSFindItemAddOnOG.logger("TargetBlock found: " + playerLookAtBlock.getType());
                    if (QSFindItemAddOnOG.isQSReremakeInstalled()) {
                        revealShop(
                                (Shop) QSFindItemAddOnOG.getQsApiInstance().findShopAtLocation(playerLookAtBlock),
                                player);
                    } else {
                        revealShop(
                                (com.ghostchu.quickshop.api.shop.Shop)
                                        QSFindItemAddOnOG.getQsApiInstance().findShopAtLocation(playerLookAtBlock),
                                player);
                    }
                } else {
                    QSFindItemAddOnOG.logger("TargetBlock is null!");
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&cNo permission!");
            }
        }
    }

    /**
     * Handles plugin reload
     *
     * @param commandSender Who is the command sender: console or player
     */
    public void handlePluginReload(CommandSender commandSender) {
        if (!(commandSender instanceof Player player)) {
            ConfigSetup.reloadConfig();
            ConfigSetup.checkForMissingProperties();
            ConfigSetup.saveConfig();
            QSFindItemAddOnOG.initConfigProvider();
            @SuppressWarnings("unchecked")
            List<Shop> allServerShops = QSFindItemAddOnOG.getQsApiInstance().getAllShops();
            if (allServerShops.isEmpty()) {
                QSFindItemAddOnOG.logger(
                        "&6Found &e0 &6shops on the server. If you ran &e/qs reload &6recently, please restart your server!");
            } else {
                QSFindItemAddOnOG.logger("&aFound &e" + allServerShops.size() + " &ashops on the server.");
            }
            WarpUtils.updateWarps();
        } else {
            if (player.hasPermission(PlayerPermsEnum.FINDITEM_RELOAD.value())
                    || player.hasPermission(PlayerPermsEnum.FINDITEM_ADMIN.value())) {
                ConfigSetup.reloadConfig();
                ConfigSetup.checkForMissingProperties();
                ConfigSetup.saveConfig();
                QSFindItemAddOnOG.initConfigProvider();
                UtilitiesOG.trueogMessage(
                        player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&aConfig reloaded!");
                List<?> allServerShops = QSFindItemAddOnOG.getQsApiInstance().getAllShops();
                if (allServerShops.isEmpty()) {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + "&6Found &e0 &6shops on the server. If you ran &e/qs reload &6recently, please restart your server!");
                } else {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&aFound &e" + allServerShops.size()
                                    + " &ashops on the server.");
                }
                WarpUtils.updateWarps();
            } else {
                UtilitiesOG.trueogMessage(
                        player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&cNo permission!");
            }
        }
    }

    /**
     * @deprecated Handles plugin restart
     * @param commandSender Who is the command sender: console or player
     */
    @Deprecated(forRemoval = true)
    public void handlePluginRestart(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            Bukkit.getPluginManager().disablePlugin(QSFindItemAddOnOG.getInstance());
            Bukkit.getPluginManager().enablePlugin(QSFindItemAddOnOG.getPlugin(QSFindItemAddOnOG.class));
            QSFindItemAddOnOG.logger("&aPlugin restarted!");
            List<?> allServerShops = QSFindItemAddOnOG.getQsApiInstance().getAllShops();
            if (allServerShops.size() == 0) {
                QSFindItemAddOnOG.logger(
                        "&6Found &e0 &6shops on the server. If you ran &e/qs reload &6recently, please restart your server!");
            } else {
                QSFindItemAddOnOG.logger("&aFound &e" + allServerShops.size() + " &ashops on the server.");
            }
        } else {
            Player player = (Player) commandSender;
            if (player.hasPermission(PlayerPermsEnum.FINDITEM_RESTART.value())
                    || player.hasPermission(PlayerPermsEnum.FINDITEM_ADMIN.value())) {
                Bukkit.getPluginManager().disablePlugin(QSFindItemAddOnOG.getInstance());
                Bukkit.getPluginManager().enablePlugin(QSFindItemAddOnOG.getPlugin(QSFindItemAddOnOG.class));
                UtilitiesOG.trueogMessage(
                        player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&aPlugin restarted!");
                List<?> allServerShops = QSFindItemAddOnOG.getQsApiInstance().getAllShops();
                if (allServerShops.size() == 0) {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + "&6Found &e0 &6shops on the server. If you ran &e/qs reload &6recently, please restart your server!");
                } else {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&aFound &e" + allServerShops.size()
                                    + " &ashops on the server.");
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player, QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX + "&cNo permission!");
            }
        }
    }

    /**
     * Handles hide shop for QuickShop Reremake
     *
     * @param shop
     * @param player
     */
    @SuppressWarnings("unchecked")
    private void hideReremakeShop(org.maxgamer.quickshop.api.shop.Shop shop, Player player) {
        if (shop != null) {
            // check if command runner same as shop owner
            if (QSFindItemAddOnOG.getQsApiInstance().isShopOwnerCommandRunner(player, shop)) {
                if (!HiddenShopStorageUtil.isShopHidden(shop)) {
                    HiddenShopStorageUtil.handleShopSearchVisibilityAsync(shop, true);
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_HIDE_SUCCESS_MSG);
                } else {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_ALREADY_HIDDEN_MSG);
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player,
                        QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_HIDING_SHOP_OWNER_INVALID_MSG);
            }
        } else {
            UtilitiesOG.trueogMessage(
                    player,
                    QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                            + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INVALID_SHOP_BLOCK_MSG);
        }
    }

    /**
     * Handles hide shop for QuickShop Hikari
     *
     * @param shop
     * @param player
     */
    @SuppressWarnings("unchecked")
    private void hideHikariShop(com.ghostchu.quickshop.api.shop.Shop shop, Player player) {
        if (shop != null) {
            // check if command runner same as shop owner
            if (QSFindItemAddOnOG.getQsApiInstance().isShopOwnerCommandRunner(player, shop)) {
                if (!HiddenShopStorageUtil.isShopHidden(shop)) {
                    HiddenShopStorageUtil.handleShopSearchVisibilityAsync(shop, true);
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_HIDE_SUCCESS_MSG);
                } else {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_ALREADY_HIDDEN_MSG);
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player,
                        QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_HIDING_SHOP_OWNER_INVALID_MSG);
            }
        } else {
            UtilitiesOG.trueogMessage(
                    player,
                    QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                            + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INVALID_SHOP_BLOCK_MSG);
        }
    }

    /**
     * Handles reveal shop for QuickShop Reremake
     *
     * @param shop
     * @param player
     */
    @SuppressWarnings("unchecked")
    private void revealShop(Shop shop, Player player) {
        if (shop != null) {
            // check if command runner same as shop owner
            if (QSFindItemAddOnOG.getQsApiInstance().isShopOwnerCommandRunner(player, shop)) {
                if (HiddenShopStorageUtil.isShopHidden(shop)) {
                    HiddenShopStorageUtil.handleShopSearchVisibilityAsync(shop, false);
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_REVEAL_SUCCESS_MSG);
                } else {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_ALREADY_PUBLIC_MSG);
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player,
                        QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_HIDING_SHOP_OWNER_INVALID_MSG);
            }
        } else {
            UtilitiesOG.trueogMessage(
                    player,
                    QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                            + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INVALID_SHOP_BLOCK_MSG);
        }
    }

    /**
     * Handles reveal shop for QuickShop Hikari
     *
     * @param shop
     * @param player
     */
    @SuppressWarnings("unchecked")
    private void revealShop(com.ghostchu.quickshop.api.shop.Shop shop, Player player) {
        if (shop != null) {
            // check if command runner same as shop owner
            if (QSFindItemAddOnOG.getQsApiInstance().isShopOwnerCommandRunner(player, shop)) {
                if (HiddenShopStorageUtil.isShopHidden(shop)) {
                    HiddenShopStorageUtil.handleShopSearchVisibilityAsync(shop, false);
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_REVEAL_SUCCESS_MSG);
                } else {
                    UtilitiesOG.trueogMessage(
                            player,
                            QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                    + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_SHOP_ALREADY_PUBLIC_MSG);
                }
            } else {
                UtilitiesOG.trueogMessage(
                        player,
                        QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                                + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_HIDING_SHOP_OWNER_INVALID_MSG);
            }
        } else {
            UtilitiesOG.trueogMessage(
                    player,
                    QSFindItemAddOnOG.getConfigProvider().PLUGIN_PREFIX
                            + QSFindItemAddOnOG.getConfigProvider().FIND_ITEM_CMD_INVALID_SHOP_BLOCK_MSG);
        }
    }
}
