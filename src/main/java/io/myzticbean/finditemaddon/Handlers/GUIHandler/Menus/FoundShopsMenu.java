package io.myzticbean.finditemaddon.Handlers.GUIHandler.Menus;

import io.myzticbean.finditemaddon.Dependencies.EssentialsXPlugin;
import io.myzticbean.finditemaddon.Dependencies.PlayerWarpsPlugin;
import io.myzticbean.finditemaddon.Dependencies.WGPlugin;
import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.Handlers.GUIHandler.PaginatedMenu;
import io.myzticbean.finditemaddon.Handlers.GUIHandler.PlayerMenuUtility;
import io.myzticbean.finditemaddon.Models.FoundShopItemModel;
import io.myzticbean.finditemaddon.Utils.Defaults.PlayerPerms;
import io.myzticbean.finditemaddon.Utils.Defaults.ShopLorePlaceholders;
import io.myzticbean.finditemaddon.Utils.JsonStorageUtils.ShopSearchActivityStorageUtil;
import io.myzticbean.finditemaddon.Utils.LocationUtils;
import io.myzticbean.finditemaddon.Utils.LoggerUtils;
import io.myzticbean.finditemaddon.Utils.WarpUtils.EssentialWarpsUtil;
import io.myzticbean.finditemaddon.Utils.WarpUtils.PlayerWarpsUtil;
import io.myzticbean.finditemaddon.Utils.WarpUtils.WGRegionUtils;
import io.papermc.lib.PaperLib;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Handler class for FoundShops GUI
 * @author ronsane
 */
public class FoundShopsMenu extends PaginatedMenu {

    private final String NO_WARP_NEAR_SHOP_ERROR_MSG = "No Warp near this shop";
    private final String NO_WG_REGION_NEAR_SHOP_ERROR_MSG = "No WG Region near this shop";

    public FoundShopsMenu(PlayerMenuUtility playerMenuUtility, List<FoundShopItemModel> searchResult) {
        super(playerMenuUtility, searchResult);
    }

    @Override
    public String getMenuName() {
        if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_SEARCH_GUI_TITLE)) {
            return ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().SHOP_SEARCH_GUI_TITLE);
        }
        else {
            return ColorTranslator.translateColorCodes("&l» &rShops");
        }
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent event) {
        if(event.getSlot() == 45) {
            handlePrevPageClick(event);
        }
        else if(event.getSlot() == 46) {
            handleFirstPageClick(event);
        }
        else if(event.getSlot() == 52) {
            handleLastPageClick(event);
        }
        else if(event.getSlot() == 53) {
            handleNextPageClick(event);
        }
        else if(event.getCurrentItem().getType().equals(Material.BARRIER) && event.getSlot() == 49) {
            handleCloseInvClick(event);
        }
        else if(event.getCurrentItem().getType().equals(Material.AIR)) {
            LoggerUtils.logDebugInfo(event.getWhoClicked().getName() + " just clicked on AIR!");
        }
        else {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            ItemMeta meta = item.getItemMeta();
            NamespacedKey key = new NamespacedKey(FindItemAddOn.getInstance(), "locationData");
            if(!meta.getPersistentDataContainer().isEmpty() && meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                String locData = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                List<String> locDataList = Arrays.asList(locData.split("\\s*,\\s*"));
                if(FindItemAddOn.getConfigProvider().TP_PLAYER_DIRECTLY_TO_SHOP) {
                    if(playerMenuUtility.getOwner().hasPermission(PlayerPerms.FINDITEM_SHOPTP.value())) {
                        World world = Bukkit.getWorld(locDataList.get(0));
                        int locX = Integer.parseInt(locDataList.get(1));
                        int locY = Integer.parseInt(locDataList.get(2));
                        int locZ = Integer.parseInt(locDataList.get(3));
                        Location shopLocation = new Location(world, locX, locY, locZ);
                        Location locToTeleport = LocationUtils.findSafeLocationAroundShop(shopLocation);
                        if(locToTeleport != null) {
                            // Add Player Visit Entry
                            ShopSearchActivityStorageUtil.addPlayerVisitEntryAsync(shopLocation, player);

                            // Add Short Blindness effect... maybe?
                            // TODO: 16/06/22 Make this an option in config -> player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 0, false, false, false));

                            // If EssentialsX is enabled, register last location before teleporting to make /back work
                            if(EssentialsXPlugin.isEnabled()) {
                                EssentialsXPlugin.getAPI().getUser(player).setLastLocation();
                            }
                            // Teleport
                            PaperLib.teleportAsync(player, locToTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }
                        else {
                            if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().UNSAFE_SHOP_AREA_MSG)) {
                                player.sendMessage(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().PLUGIN_PREFIX + FindItemAddOn.getConfigProvider().UNSAFE_SHOP_AREA_MSG));
                            }
                        }
                    }
                    else if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_TP_NO_PERMISSION_MSG)) {
                        playerMenuUtility.getOwner()
                                .sendMessage(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().PLUGIN_PREFIX + FindItemAddOn.getConfigProvider().SHOP_TP_NO_PERMISSION_MSG));
                        event.getWhoClicked().closeInventory();
                    }
                    player.closeInventory();
                }
                else if(FindItemAddOn.getConfigProvider().TP_PLAYER_TO_NEAREST_WARP
                    // if list size = 1, it contains Warp name
                    && locDataList.size() == 1) {
                        String warpName = locDataList.get(0);
                        if(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE == 1) {
                            Bukkit.dispatchCommand(player, "essentials:warp " + warpName);
                        }
                        else if(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE == 2) {
                            PlayerWarpsPlugin.executeWarpPlayer(player, warpName);
                        }
                }
            }
            else {
                LoggerUtils.logError("PersistentDataContainer doesn't have the right kind of data!");
            }
        }
    }

    private void handlePrevPageClick(InventoryClickEvent event) {
        if(page == 0) {
            if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_NAV_FIRST_PAGE_ALERT_MSG)) {
                event.getWhoClicked().sendMessage(
                    ColorTranslator.translateColorCodes(
                        FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                        + FindItemAddOn.getConfigProvider().SHOP_NAV_FIRST_PAGE_ALERT_MSG));
            }
        }
        else {
            page = page - 1;
            super.open(super.playerMenuUtility.getPlayerShopSearchResult());
        }
    }

    private void handleNextPageClick(InventoryClickEvent event) {
        if(!((index + 1) >= super.playerMenuUtility.getPlayerShopSearchResult().size())) {
            page = page + 1;
            super.open(super.playerMenuUtility.getPlayerShopSearchResult());
        }
        else {
            if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_NAV_LAST_PAGE_ALERT_MSG)) {
                event.getWhoClicked().sendMessage(
                    ColorTranslator.translateColorCodes(
                        FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                            + FindItemAddOn.getConfigProvider().SHOP_NAV_LAST_PAGE_ALERT_MSG));
            }
        }
    }

    private void handleFirstPageClick(InventoryClickEvent event) {
        if(page == 0) {
            if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_NAV_FIRST_PAGE_ALERT_MSG)) {
                event.getWhoClicked().sendMessage(
                        ColorTranslator.translateColorCodes(
                                FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                                        + FindItemAddOn.getConfigProvider().SHOP_NAV_FIRST_PAGE_ALERT_MSG));
            }
        } else {
            page = 0;
            super.open(super.playerMenuUtility.getPlayerShopSearchResult());
        }
    }

    private void handleLastPageClick(InventoryClickEvent event) {
        int listSize = super.playerMenuUtility.getPlayerShopSearchResult().size();
        if(!((index + 1) >= listSize)) {
            double totalPages = listSize / maxItemsPerPage;
            if(totalPages % 10 == 0) {
                page = (int) Math.floor(totalPages);
                LoggerUtils.logDebugInfo("Floor page value: " + page);
            }
            else {
                page = (int) Math.ceil(totalPages);
                LoggerUtils.logDebugInfo("Ceiling page value: " + page);
            }
            super.open(super.playerMenuUtility.getPlayerShopSearchResult());
        }
        else {
            if(!StringUtils.isEmpty(FindItemAddOn.getConfigProvider().SHOP_NAV_LAST_PAGE_ALERT_MSG)) {
                event.getWhoClicked().sendMessage(
                        ColorTranslator.translateColorCodes(
                                FindItemAddOn.getConfigProvider().PLUGIN_PREFIX
                                        + FindItemAddOn.getConfigProvider().SHOP_NAV_LAST_PAGE_ALERT_MSG));
            }
        }
    }

    private void handleCloseInvClick(InventoryClickEvent event) {
        event.getWhoClicked().closeInventory();
    }

    /**
     * Empty method in case we need to handle static GUI icons in future
     */
    @Override
    public void setMenuItems() {
        //
    }

    /**
     * Sets the slots in the search result GUI
     * @param foundShops List of found shops
     */
    @Override
    public void setMenuItems(List<FoundShopItemModel> foundShops) {
        addMenuBottomBar();
        if(foundShops != null && !foundShops.isEmpty()) {
            int guiSlotCounter = 0;
            while(guiSlotCounter < super.maxItemsPerPage) {
                index = super.maxItemsPerPage * page + guiSlotCounter;

                if(index >= foundShops.size())  break;

                if(foundShops.get(index) != null) {
                    // Place Search Results here
                    FoundShopItemModel foundShop_i = foundShops.get(index);
                    NamespacedKey key = new NamespacedKey(FindItemAddOn.getInstance(), "locationData");
                    ItemStack item = new ItemStack(foundShop_i.getItem().getType());
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore;
                    lore = new ArrayList<>();
                    com.olziedev.playerwarps.api.warp.Warp nearestPlayerWarp = null;
                    String nearestEWarp = null;

                    if(foundShop_i.getItem().hasItemMeta()) {
                        meta = foundShop_i.getItem().getItemMeta();
                        if(foundShop_i.getItem().getItemMeta().hasLore()) {
                            for(String s : foundShop_i.getItem().getItemMeta().getLore()) {
                                lore.add(ColorTranslator.translateColorCodes(s));
                            }
                        }
                    }
                    List<String> shopItemLore = FindItemAddOn.getConfigProvider().SHOP_GUI_ITEM_LORE;
                    for(String shopItemLore_i : shopItemLore) {
                        if(shopItemLore_i.contains(ShopLorePlaceholders.NEAREST_WARP.value())) {
                            switch(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE) {
                                case 1:
                                    // EssentialWarp: Check nearest warp
                                    if(EssentialsXPlugin.isEnabled()) {
                                        nearestEWarp = new EssentialWarpsUtil().findNearestWarp(foundShop_i.getShopLocation());
                                        if(nearestEWarp != null && !StringUtils.isEmpty(nearestEWarp)) {
                                            lore.add(ColorTranslator.translateColorCodes(shopItemLore_i.replace(ShopLorePlaceholders.NEAREST_WARP.value(), nearestEWarp)));
                                        }
                                        else {
                                            lore.add(ColorTranslator.translateColorCodes(shopItemLore_i.replace(ShopLorePlaceholders.NEAREST_WARP.value(), NO_WARP_NEAR_SHOP_ERROR_MSG)));
                                        }
                                    }
                                    break;
                                case 2:
                                    // PlayerWarp: Check nearest warp
                                    if(PlayerWarpsPlugin.getIsEnabled()) {
                                        nearestPlayerWarp = new PlayerWarpsUtil().findNearestWarp(foundShop_i.getShopLocation());
                                        if(nearestPlayerWarp != null) {
                                            lore.add(ColorTranslator.translateColorCodes(shopItemLore_i.replace(ShopLorePlaceholders.NEAREST_WARP.value(), nearestPlayerWarp.getWarpName())));
                                        }
                                        else {
                                            lore.add(ColorTranslator.translateColorCodes(shopItemLore_i.replace(ShopLorePlaceholders.NEAREST_WARP.value(), NO_WARP_NEAR_SHOP_ERROR_MSG)));
                                        }
                                    }
                                    break;
                                case 3:
                                    // WG Region: Check nearest WG Region
                                    if(WGPlugin.isEnabled()) {
                                        String nearestWGRegion = new WGRegionUtils().findNearestWGRegion((foundShop_i.getShopLocation()));
                                        if(nearestWGRegion != null && !StringUtils.isEmpty(nearestWGRegion)) {
                                            lore.add(ColorTranslator.translateColorCodes(shopItemLore_i.replace(ShopLorePlaceholders.NEAREST_WARP.value(), nearestWGRegion)));
                                        }
                                        else {
                                            lore.add(ColorTranslator.translateColorCodes(shopItemLore_i.replace(ShopLorePlaceholders.NEAREST_WARP.value(), NO_WG_REGION_NEAR_SHOP_ERROR_MSG)));
                                        }
                                    }
                                    break;
                                default:
                                    LoggerUtils.logDebugInfo("Invalid value in 'nearest-warp-mode' in config.yml!");
                            }
                        }
                        else {
                            lore.add(ColorTranslator.translateColorCodes(replaceLorePlaceholders(shopItemLore_i, foundShop_i)));
                        }
                    }

                    if(FindItemAddOn.getConfigProvider().TP_PLAYER_DIRECTLY_TO_SHOP
                        && playerMenuUtility.getOwner().hasPermission(PlayerPerms.FINDITEM_SHOPTP.value())) {
                            lore.add(ColorTranslator.translateColorCodes(FindItemAddOn.getConfigProvider().CLICK_TO_TELEPORT_MSG));
                    }
                    assert meta != null;
                    meta.setLore(lore);

                    // storing location data in item persistent storage
                    String locData = StringUtils.EMPTY;
                    // store the coordinates
                    if(FindItemAddOn.getConfigProvider().TP_PLAYER_DIRECTLY_TO_SHOP) {
                        locData = Objects.requireNonNull(foundShop_i.getShopLocation().getWorld()).getName() + ","
                                + foundShop_i.getShopLocation().getBlockX() + ","
                                + foundShop_i.getShopLocation().getBlockY() + ","
                                + foundShop_i.getShopLocation().getBlockZ();
                    }
                    else if(FindItemAddOn.getConfigProvider().TP_PLAYER_TO_NEAREST_WARP) {
                        // if Nearest Warp is set to EssentialsX Warps, store the warp name
                        if(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE == 1) {
                            if(nearestEWarp != null) {
                                locData = nearestEWarp;
                            }
                        }
                        // if Nearest Warp is set to PlayerWarps, store the warp name
                        else if(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE == 2
                                && nearestPlayerWarp != null) {
                            locData = nearestPlayerWarp.getWarpName();
                        }
                    }
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, locData);

                    // handling custom model data
                    if(Objects.requireNonNull(foundShop_i.getItem().getItemMeta()).hasCustomModelData()) {
                        meta.setCustomModelData(foundShop_i.getItem().getItemMeta().getCustomModelData());
                    }
                    item.setItemMeta(meta);
                    inventory.addItem(item);
                }
                guiSlotCounter++;
            }
        }
    }

    /**
     * Replaces all the placeholders in the Shop item lore in GUI
     * @param text Line of lore
     * @param shop Shop instance
     * @return Line of lore replaced with placeholder values
     */
    private String replaceLorePlaceholders(String text, FoundShopItemModel shop) {

        if(text.contains(ShopLorePlaceholders.ITEM_PRICE.value())) {
            text = text.replace(ShopLorePlaceholders.ITEM_PRICE.value(), String.valueOf(shop.getShopPrice()));
        }
        if(text.contains(ShopLorePlaceholders.SHOP_STOCK.value())) {
            if(shop.getRemainingStockOrSpace() == Integer.MAX_VALUE) {
                text = text.replace(ShopLorePlaceholders.SHOP_STOCK.value(), "Unlimited");
            }
            else {
                text = text.replace(ShopLorePlaceholders.SHOP_STOCK.value(), String.valueOf(shop.getRemainingStockOrSpace()));
            }
        }
        if(text.contains(ShopLorePlaceholders.SHOP_OWNER.value())) {
            OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(shop.getShopOwner());
            if(shopOwner.getName() != null) {
                text = text.replace(ShopLorePlaceholders.SHOP_OWNER.value(), shopOwner.getName());
            }
            else {
                // set a generic name for shops with no owner name
                text = text.replace(ShopLorePlaceholders.SHOP_OWNER.value(), "Admin");
            }
        }
        if(text.contains(ShopLorePlaceholders.SHOP_LOCATION.value())) {
            text = text.replace(ShopLorePlaceholders.SHOP_LOCATION.value(),
                    shop.getShopLocation().getBlockX() + ", "
                    + shop.getShopLocation().getBlockY() + ", "
                    + shop.getShopLocation().getBlockZ());
        }
        if(text.contains(ShopLorePlaceholders.SHOP_WORLD.value())) {
            text = text.replace(ShopLorePlaceholders.SHOP_WORLD.value(), Objects.requireNonNull(shop.getShopLocation().getWorld()).getName());
        }
        // Added in v2.0
        if(text.contains(ShopLorePlaceholders.SHOP_VISITS.value())) {
            text = text.replace(ShopLorePlaceholders.SHOP_VISITS.value(), String.valueOf(ShopSearchActivityStorageUtil.getPlayerVisitCount(shop.getShopLocation())));
        }
        return text;
    }
}
