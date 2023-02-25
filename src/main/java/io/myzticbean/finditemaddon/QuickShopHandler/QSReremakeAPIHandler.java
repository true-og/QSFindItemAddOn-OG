package io.myzticbean.finditemaddon.QuickShopHandler;

import io.myzticbean.finditemaddon.Commands.QSSubCommands.FindItemCmdReremakeImpl;
import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.Models.FoundShopItemModel;
import io.myzticbean.finditemaddon.Models.ShopSearchActivityModel;
import io.myzticbean.finditemaddon.Utils.Defaults.PlayerPerms;
import io.myzticbean.finditemaddon.Utils.JsonStorageUtils.HiddenShopStorageUtil;
import io.myzticbean.finditemaddon.Utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.api.command.CommandContainer;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of QSApi for Reremake
 * @author ronsane
 */
public class QSReremakeAPIHandler implements QSApi<QuickShop, Shop> {

    private final QuickShopAPI api;
    private final String QS_REREMAKE_PLUGIN_NAME = "QuickShop";

    public QSReremakeAPIHandler() {
        api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin(QS_REREMAKE_PLUGIN_NAME);
    }

    @Override
    public List<FoundShopItemModel> findItemBasedOnTypeFromAllShops(ItemStack item, boolean toBuy, Player searchingPlayer) {
        List<FoundShopItemModel> shopsFoundList = new ArrayList<>();
        List<Shop> allShops;
        if(FindItemAddOn.getConfigProvider().SEARCH_LOADED_SHOPS_ONLY)
            allShops = new ArrayList<>(api.getShopManager().getLoadedShops());
        else
            allShops = api.getShopManager().getAllShops();
        LoggerUtils.logDebugInfo(QS_TOTAL_SHOPS_ON_SERVER + allShops.size());
        allShops.forEach(shopIterator -> {
            // check for blacklisted worlds
            if(!FindItemAddOn.getConfigProvider().getBlacklistedWorlds().contains(shopIterator.getLocation().getWorld())
                && shopIterator.getItem().getType().equals(item.getType())
                && (toBuy ? shopIterator.getRemainingStock() != 0 : shopIterator.getRemainingSpace() != 0)
                && (toBuy ? shopIterator.isSelling() : shopIterator.isBuying())
                // check for shop if hidden
                && (!HiddenShopStorageUtil.isShopHidden(shopIterator))) {
                    shopsFoundList.add(new FoundShopItemModel(
                        shopIterator.getPrice(),
                        QSApi.processStockOrSpace((toBuy ? shopIterator.getRemainingStock() : shopIterator.getRemainingSpace())),
                        shopIterator.getOwner(),
                        shopIterator.getLocation(),
                        shopIterator.getItem()
                    ));
            }
        });
        if(!shopsFoundList.isEmpty()) {
            int sortingMethod = 2;
            try {
                sortingMethod = FindItemAddOn.getConfigProvider().SHOP_SORTING_METHOD;
            }
            catch(Exception e) {
                LoggerUtils.logError("Invalid value in config.yml : 'shop-sorting-method'");
                LoggerUtils.logError("Defaulting to sorting by prices method");
            }
            return QSApi.sortShops(sortingMethod, shopsFoundList, toBuy);
        }
        return shopsFoundList;
    }

    @Override
    public List<FoundShopItemModel> findItemBasedOnDisplayNameFromAllShops(String displayName, boolean toBuy, Player searchingPlayer) {
        List<FoundShopItemModel> shopsFoundList = new ArrayList<>();
        List<Shop> allShops;
        if(FindItemAddOn.getConfigProvider().SEARCH_LOADED_SHOPS_ONLY)
            allShops = new ArrayList<>(api.getShopManager().getLoadedShops());
        else
            allShops = api.getShopManager().getAllShops();

        LoggerUtils.logDebugInfo(QS_TOTAL_SHOPS_ON_SERVER + allShops.size());
        for(Shop shopIterator : allShops) {
            if(!FindItemAddOn.getConfigProvider().getBlacklistedWorlds().contains(shopIterator.getLocation().getWorld())
                && shopIterator.getItem().hasItemMeta()
                && Objects.requireNonNull(shopIterator.getItem().getItemMeta()).hasDisplayName()
                && (shopIterator.getItem().getItemMeta().getDisplayName().toLowerCase().contains(displayName.toLowerCase())
                    && (toBuy ? shopIterator.getRemainingStock() != 0 : shopIterator.getRemainingSpace() != 0)
                    && (toBuy ? shopIterator.isSelling() : shopIterator.isBuying()))
                // check for shop if hidden
                && !HiddenShopStorageUtil.isShopHidden(shopIterator)) {
                    shopsFoundList.add(new FoundShopItemModel(
                        shopIterator.getPrice(),
                        QSApi.processStockOrSpace((toBuy ? shopIterator.getRemainingStock() : shopIterator.getRemainingSpace())),
                        shopIterator.getOwner(),
                        shopIterator.getLocation(),
                        shopIterator.getItem()
                    ));
            }
        }
        if(!shopsFoundList.isEmpty()) {
            int sortingMethod = 2;
            try {
                sortingMethod = FindItemAddOn.getConfigProvider().SHOP_SORTING_METHOD;
            }
            catch(Exception e) {
                LoggerUtils.logError("Invalid value in config.yml : 'shop-sorting-method'");
                LoggerUtils.logError("Defaulting to sorting by prices method");
            }
            return QSApi.sortShops(sortingMethod, shopsFoundList, toBuy);
        }
        return shopsFoundList;
    }

    @Override
    public List<FoundShopItemModel> fetchAllItemsFromAllShops(boolean toBuy, Player searchingPlayer) {
        List<FoundShopItemModel> shopsFoundList = new ArrayList<>();
        List<Shop> allShops;
        if(FindItemAddOn.getConfigProvider().SEARCH_LOADED_SHOPS_ONLY)
            allShops = new ArrayList<>(api.getShopManager().getLoadedShops());
        else
            allShops = api.getShopManager().getAllShops();

        LoggerUtils.logDebugInfo(QS_TOTAL_SHOPS_ON_SERVER + allShops.size());
        allShops.forEach(shopIterator -> {
            // check for blacklisted worlds
            if(!FindItemAddOn.getConfigProvider().getBlacklistedWorlds().contains(shopIterator.getLocation().getWorld())
                && (toBuy ? shopIterator.getRemainingStock() != 0 : shopIterator.getRemainingSpace() != 0)
                && (toBuy ? shopIterator.isSelling() : shopIterator.isBuying())
                // check for shop if hidden
                && !HiddenShopStorageUtil.isShopHidden(shopIterator)) {
                    shopsFoundList.add(new FoundShopItemModel(
                        shopIterator.getPrice(),
                        QSApi.processStockOrSpace((toBuy ? shopIterator.getRemainingStock() : shopIterator.getRemainingSpace())),
                        shopIterator.getOwner(),
                        shopIterator.getLocation(),
                        shopIterator.getItem()
                    ));
            }
        });
        if(!shopsFoundList.isEmpty()) {
            int sortingMethod = 1;
            return QSApi.sortShops(sortingMethod, shopsFoundList, toBuy);
        }
        return shopsFoundList;
    }

    @Override
    public Material getShopSignMaterial() {
        return org.maxgamer.quickshop.util.Util.getSignMaterial();
    }

    @Override
    public Shop findShopAtLocation(Block block) {
        Location loc = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());
        return api.getShopManager().getShop(loc);
    }

    @Override
    public boolean isShopOwnerCommandRunner(Player player, Shop shop) {
        return shop.getOwner().toString().equalsIgnoreCase(player.getUniqueId().toString());
    }

    @Override
    public List<Shop> getAllShops() {
        return api.getShopManager().getAllShops();
    }

    public List<ShopSearchActivityModel> syncShopsListForStorage(List<ShopSearchActivityModel> globalShopsList) {

        // copy all shops from shops list in API to a temp globalShopsList
        // now check shops from temp globalShopsList in current globalShopsList and pull playerVisit data
        List<ShopSearchActivityModel> tempGlobalShopsList = new ArrayList<>();

        for(Shop shop_i : getAllShops()) {
            Location shopLoc = shop_i.getLocation();
            tempGlobalShopsList.add(new ShopSearchActivityModel(
                    shopLoc.getWorld().getName(),
                    shopLoc.getX(),
                    shopLoc.getY(),
                    shopLoc.getZ(),
                    shopLoc.getPitch(),
                    shopLoc.getYaw(),
                    shop_i.getOwner().toString(),
                    new ArrayList<>(),
                    false
            ));
        }

        for(ShopSearchActivityModel shop_temp : tempGlobalShopsList) {
            ShopSearchActivityModel tempShopToRemove = null;
            for(ShopSearchActivityModel shop_global : globalShopsList) {
                if(shop_temp.getWorldName().equalsIgnoreCase(shop_global.getWorldName())
                        && shop_temp.getX() == shop_global.getX()
                        && shop_temp.getY() == shop_global.getY()
                        && shop_temp.getZ() == shop_global.getZ()
                        && shop_temp.getShopOwnerUUID().equalsIgnoreCase(shop_global.getShopOwnerUUID())
                ) {
                    shop_temp.setPlayerVisitList(shop_global.getPlayerVisitList());
                    shop_temp.setHiddenFromSearch(shop_global.isHiddenFromSearch());
                    tempShopToRemove = shop_global;
                    break;
                }
            }
            if(tempShopToRemove != null)
                globalShopsList.remove(tempShopToRemove);
        }

        return tempGlobalShopsList;
    }

    /**
     * Register finditem sub-command for /qs
     */
    @Override
    public void registerSubCommand() {
        LoggerUtils.logInfo("Unregistered find sub-command for /qs");
        for(CommandContainer cmdContainer : api.getCommandManager().getRegisteredCommands()) {
            if(cmdContainer.getPrefix().equalsIgnoreCase("find")) {
                api.getCommandManager().unregisterCmd(cmdContainer);
                break;
            }
        }
        LoggerUtils.logInfo("Registered finditem sub-command for /qs");
        api.getCommandManager().registerCmd(
            CommandContainer.builder()
                .prefix("finditem")
                .permission(PlayerPerms.FINDITEM_USE.value())
                .hidden(false)
                .description("Search for items from all shops using an interactive GUI")
                .executor(new FindItemCmdReremakeImpl())
                .build());
    }
}
