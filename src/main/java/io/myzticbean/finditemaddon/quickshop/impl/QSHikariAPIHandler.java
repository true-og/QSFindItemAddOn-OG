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
package io.myzticbean.finditemaddon.quickshop.impl;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.util.Util;
import io.myzticbean.finditemaddon.QSFindItemAddOnOG;
import io.myzticbean.finditemaddon.commands.quickshop.subcommands.FindItemCmdHikariImpl;
import io.myzticbean.finditemaddon.models.CachedShop;
import io.myzticbean.finditemaddon.models.FoundShopItemModel;
import io.myzticbean.finditemaddon.models.ShopSearchActivityModel;
import io.myzticbean.finditemaddon.quickshop.QSApi;
import io.myzticbean.finditemaddon.utils.enums.PlayerPermsEnum;
import io.myzticbean.finditemaddon.utils.json.HiddenShopStorageUtil;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of QSApi for Hikari
 *
 * @author myzticbean
 */
public class QSHikariAPIHandler implements QSApi<QuickShop, Shop> {

    public static final String IS_MAIN_THREAD = "Is MAIN Thread? ";

    private final QuickShopAPI api;
    private final String pluginVersion;
    private final ConcurrentMap<Long, CachedShop> shopCache;
    private final int SHOP_CACHE_TIMEOUT_SECONDS = 5 * 60;
    private final boolean isQSHikariShopCacheImplemented;

    public QSHikariAPIHandler() {

        api = QuickShopAPI.getInstance();
        pluginVersion = Bukkit.getPluginManager().getPlugin("QuickShop-OG").getPluginMeta().getVersion();
        QSFindItemAddOnOG.logger("Initializing Shop caching");
        shopCache = new ConcurrentHashMap<>();
        isQSHikariShopCacheImplemented = checkIfQSHikariShopCacheImplemented();

    }

    public List<FoundShopItemModel> findItemBasedOnTypeFromAllShops(ItemStack item, boolean toBuy,
            Player searchingPlayer)
    {

        QSFindItemAddOnOG.logger(IS_MAIN_THREAD + Bukkit.isPrimaryThread());
        var begin = Instant.now();
        List<FoundShopItemModel> shopsFoundList = new ArrayList<>();
        List<Shop> allShops = fetchAllShopsFromQS();
        QSFindItemAddOnOG.logger(QS_TOTAL_SHOPS_ON_SERVER + allShops.size());
        for (Shop shopIterator : allShops) {

            // check for Quickshop-Hikari internal per-shop based search permission
            if (shopIterator.playerAuthorize(searchingPlayer.getUniqueId(), BuiltInShopPermission.SEARCH)
                    // check for blacklisted worlds
                    && (!QSFindItemAddOnOG.getConfigProvider().getBlacklistedWorlds()
                            .contains(shopIterator.getLocation().getWorld())
                            && shopIterator.getItem().getType().equals(item.getType())
                            && (toBuy ? shopIterator.isSelling() : shopIterator.isBuying()))
                    // check for shop if hidden
                    && (!HiddenShopStorageUtil.isShopHidden(shopIterator)))
            {

                processPotentialShopMatchAndAddToFoundList(toBuy, shopIterator, shopsFoundList, searchingPlayer);

            }

        }

        List<FoundShopItemModel> sortedShops = handleShopSorting(toBuy, shopsFoundList);
        QSApi.logTimeTookMsg(begin);
        return sortedShops;

    }

    /**
     * Checks if the shop owner has enough balance to buy at least one item
     *
     * @param shop The shop to check
     * @return true if owner has enough balance, false otherwise
     */
    private static boolean isOwnerHavingEnoughBalance(@NotNull Shop shop) {

        if (shop.getOwner().getUniqueIdOptional().isEmpty()) {

            return true;

        }

        double pricePerTransaction = shop.getPrice() * shop.getItem().getAmount();

        UUID ownerId = shop.getOwner().getUniqueIdIfRealPlayer().orElse(null);
        if (ownerId == null) {

            return true;

        }

        // TODO: Replace Vault here with DiamondBank-OG API.
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);

        if (!owner.hasPlayedBefore()) {

            QSFindItemAddOnOG.logger("Shop owner has never joined: " + shop.getOwner().getUsername());
            return true;

        }

        Economy econ = QSFindItemAddOnOG.getEconomy();
        if (econ == null || !econ.isEnabled()) {

            QSFindItemAddOnOG.logger("Economy provider unavailable; " + "skipping balance check.");
            return true;

        }

        String worldName = shop.getLocation().getWorld().getName();
        double balance = econ.getBalance(owner, worldName);

        return balance >= pricePerTransaction;

    }

    @NotNull
    static List<FoundShopItemModel> handleShopSorting(boolean toBuy, @NotNull List<FoundShopItemModel> shopsFoundList) {

        if (!shopsFoundList.isEmpty()) {

            int sortingMethod = 2;
            try {

                sortingMethod = QSFindItemAddOnOG.getConfigProvider().SHOP_SORTING_METHOD;

            } catch (Exception error) {

                QSFindItemAddOnOG.logger("Invalid value in config.yml : 'shop-sorting-method'");
                QSFindItemAddOnOG.logger("Defaulting to sorting by prices method");

            }

            return QSApi.sortShops(sortingMethod, shopsFoundList, toBuy);

        }

        return shopsFoundList;

    }

    public List<FoundShopItemModel> findItemBasedOnDisplayNameFromAllShops(String displayName, boolean toBuy,
            Player searchingPlayer)
    {

        QSFindItemAddOnOG.logger(IS_MAIN_THREAD + Bukkit.isPrimaryThread());
        var begin = Instant.now();
        List<FoundShopItemModel> shopsFoundList = new ArrayList<>();
        List<Shop> allShops = fetchAllShopsFromQS();
        QSFindItemAddOnOG.logger(QS_TOTAL_SHOPS_ON_SERVER + allShops.size());
        for (Shop shopIterator : allShops) {

            ItemStack item = shopIterator.getItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {

                Component displayNameComponent = meta.displayName();
                String plainText = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);
                String lowerCaseName = plainText.toLowerCase();
                // check for QuickShop-Hikari internal per-shop based search permission
                if (shopIterator.playerAuthorize(searchingPlayer.getUniqueId(), BuiltInShopPermission.SEARCH)
                        // check for blacklisted worlds
                        && !QSFindItemAddOnOG.getConfigProvider().getBlacklistedWorlds()
                                .contains(shopIterator.getLocation().getWorld())
                        // match the item based on query
                        && shopIterator.getItem().hasItemMeta()
                        && Objects.requireNonNull(shopIterator.getItem().getItemMeta()).hasDisplayName()
                        && (lowerCaseName.contains(displayName.toLowerCase())
                                && (toBuy ? shopIterator.isSelling() : shopIterator.isBuying()))
                        // check for shop if hidden
                        && !HiddenShopStorageUtil.isShopHidden(shopIterator))
                {

                    processPotentialShopMatchAndAddToFoundList(toBuy, shopIterator, shopsFoundList, searchingPlayer);

                }

            }

        }

        List<FoundShopItemModel> sortedShops = handleShopSorting(toBuy, shopsFoundList);
        QSApi.logTimeTookMsg(begin);
        return sortedShops;

    }

    public List<FoundShopItemModel> fetchAllItemsFromAllShops(boolean toBuy, Player searchingPlayer) {

        QSFindItemAddOnOG.logger(IS_MAIN_THREAD + Bukkit.isPrimaryThread());
        var begin = Instant.now();
        List<FoundShopItemModel> shopsFoundList = new ArrayList<>();
        List<Shop> allShops = fetchAllShopsFromQS();
        QSFindItemAddOnOG.logger(QS_TOTAL_SHOPS_ON_SERVER + allShops.size());
        for (Shop shopIterator : allShops) {

            // check for QuickShop-Hikari internal per-shop based search permission
            if (shopIterator.playerAuthorize(searchingPlayer.getUniqueId(), BuiltInShopPermission.SEARCH)
                    // check for blacklisted worlds
                    && (!QSFindItemAddOnOG.getConfigProvider().getBlacklistedWorlds()
                            .contains(shopIterator.getLocation().getWorld())
                            && (toBuy ? shopIterator.isSelling() : shopIterator.isBuying()))
                    // check for shop if hidden
                    && (!HiddenShopStorageUtil.isShopHidden(shopIterator)))
            {

                processPotentialShopMatchAndAddToFoundList(toBuy, shopIterator, shopsFoundList, searchingPlayer);

            }

        }

        List<FoundShopItemModel> sortedShops = new ArrayList<>(shopsFoundList);
        if (!shopsFoundList.isEmpty()) {

            int sortingMethod = 1;
            sortedShops = QSApi.sortShops(sortingMethod, shopsFoundList, toBuy);

        }

        QSApi.logTimeTookMsg(begin);
        return sortedShops;

    }

    private List<Shop> fetchAllShopsFromQS() {

        List<Shop> allShops;
        if (QSFindItemAddOnOG.getConfigProvider().SEARCH_LOADED_SHOPS_ONLY) {

            allShops = new ArrayList<>(api.getShopManager().getLoadedShops());

        } else {

            allShops = getAllShops();

        }

        return allShops;

    }

    public Material getShopSignMaterial() {

        return com.ghostchu.quickshop.util.Util.getSignMaterial();

    }

    // public Shop findShopAtLocation(Block block) {
    // Location loc = new Location(block.getWorld(), block.getX(), block.getY(),
    // block.getZ());
    // return api.getShopManager().getShop(loc);
    // }

    public Shop findShopAtLocation(Block block) {

        Location loc = block.getLocation(); // Simpler way to get location

        // Try getting shop directly first
        Shop shop = api.getShopManager().getShopIncludeAttached(loc);

        // If no shop found and block is a chest, check if it's a double chest
        if (shop == null && block.getType() == Material.CHEST) {

            Block secondHalf = Util.getSecondHalf(block);
            if (secondHalf != null) {

                shop = api.getShopManager().getShopIncludeAttached(secondHalf.getLocation());

            }

        }

        return shop;

    }

    public boolean isShopOwnerCommandRunner(Player player, Shop shop) {

        QSFindItemAddOnOG.logger("Shop owner: " + shop.getOwner() + " | Player: " + player.getUniqueId());
        return shop.getOwner().getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString());

    }

    @Override
    public List<Shop> getAllShops() {

        return api.getShopManager().getAllShops();

    }

    @Override
    public List<ShopSearchActivityModel> syncShopsListForStorage(List<ShopSearchActivityModel> globalShopsList) {

        long start = System.currentTimeMillis();
        // copy all shops from shops list in API to a temp globalShopsList
        // now check shops from temp globalShopsList in current globalShopsList and pull
        // playerVisit data
        List<ShopSearchActivityModel> tempGlobalShopsList = new ArrayList<>();
        for (Shop shop_i : getAllShops()) {

            Location shopLoc = shop_i.getLocation();
            tempGlobalShopsList.add(new ShopSearchActivityModel(shopLoc.getWorld().getName(), shopLoc.getX(),
                    shopLoc.getY(), shopLoc.getZ(), shopLoc.getPitch(), shopLoc.getYaw(),
                    convertQUserToUUID(shop_i.getOwner()).toString(), new ArrayList<>(), false));

        }

        for (ShopSearchActivityModel shop_temp : tempGlobalShopsList) {

            ShopSearchActivityModel tempShopToRemove = null;
            for (ShopSearchActivityModel shop_global : globalShopsList) {

                if (shop_temp.getWorldName().equalsIgnoreCase(shop_global.getWorldName())
                        && shop_temp.getX() == shop_global.getX() && shop_temp.getY() == shop_global.getY()
                        && shop_temp.getZ() == shop_global.getZ()
                        && shop_temp.getShopOwnerUUID().equalsIgnoreCase(shop_global.getShopOwnerUUID()))
                {

                    shop_temp.setPlayerVisitList(shop_global.getPlayerVisitList());
                    shop_temp.setHiddenFromSearch(shop_global.isHiddenFromSearch());
                    tempShopToRemove = shop_global;
                    break;

                }

            }

            if (tempShopToRemove != null)
                globalShopsList.remove(tempShopToRemove);

        }

        QSFindItemAddOnOG
                .logger("Shops List sync complete. Time took: " + (System.currentTimeMillis() - start) + "ms.");
        return tempGlobalShopsList;

    }

    /**
     * Register finditem sub-command for /qs Unregister /qs find
     */
    @Override
    public void registerSubCommand() {

        QSFindItemAddOnOG.logger("Unregistered find sub-command for /qs");
        for (CommandContainer cmdContainer : api.getCommandManager().getRegisteredCommands()) {

            if (cmdContainer.getPrefix().equalsIgnoreCase("find")) {

                api.getCommandManager().unregisterCmd(cmdContainer);
                break;

            }

        }

        QSFindItemAddOnOG.logger("Registered finditem sub-command for /qs");
        api.getCommandManager().registerCmd(CommandContainer.builder().prefix("finditem")
                .permission(PlayerPermsEnum.FINDITEM_USE.value()).hidden(false)
                .description(locale -> Component.text("Search for items from all shops using an interactive GUI"))
                .executor(new FindItemCmdHikariImpl()).build());

    }

    @Override
    public boolean isQSShopCacheImplemented() {

        return isQSHikariShopCacheImplemented;

    }

    @Override
    public int processUnknownStockSpace(Location shopLoc, boolean toBuy) {

        // This process needs to run in MAIN thread!
        Util.ensureThread(false);
        QSFindItemAddOnOG.logger("Fetching stock/space from MAIN thread...");
        Shop qsShop = api.getShopManager().getShop(shopLoc);
        if (qsShop != null) {

            return (toBuy ? qsShop.getRemainingStock() : qsShop.getRemainingSpace());

        } else {

            return -2;

        }

    }

    private UUID convertQUserToUUID(QUser qUser) {

        Optional<UUID> uuid = qUser.getUniqueIdOptional();
        if (uuid.isPresent()) {

            return uuid.get();

        }

        String username = qUser.getUsernameOptional().orElse("Unknown");
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));

    }

    public UUID convertNameToUuid(String playerName) {

        return api.getPlayerFinder().name2Uuid(playerName);

    }

    /**
     * If IGNORE_EMPTY_CHESTS is true -> do not add empty stock or space If to buy
     * -> If shop has no stock -> based on ignore flag, decide to include it or not
     * If to sell -> If shop has no space -> based on ignore flag, decide to include
     * it or not
     *
     * @param stockOrSpace
     * @return If shop needs to be ignored from list
     */
    private boolean isShopToBeIgnoredForFullOrEmpty(int stockOrSpace) {

        boolean ignoreEmptyChests = QSFindItemAddOnOG.getConfigProvider().IGNORE_EMPTY_CHESTS;
        if (ignoreEmptyChests) {

            return stockOrSpace == 0;

        }

        return false;

    }

    private int getRemainingStockOrSpaceFromShopCache(Shop shop, boolean fetchRemainingStock) {

        String mainVersionStr = pluginVersion.split("\\.")[0];
        int mainVersion = Integer.parseInt(mainVersionStr);
        if (mainVersion >= 6) {

            // New feature available
            Util.ensureThread(true);
            int stockOrSpace = (fetchRemainingStock ? shop.getRemainingStock() : shop.getRemainingSpace());
            QSFindItemAddOnOG.logger("Stock/Space from cache: " + stockOrSpace);
            return stockOrSpace;

        } else {

            // Show warning.
            QSFindItemAddOnOG
                    .logger("Update recommended to QuickShop-Hikari v6+! You are still using v" + pluginVersion);
            // PREPARE FOR LAG.
            CachedShop cachedShop = shopCache.get(shop.getShopId());
            if (cachedShop == null || QSApi.isTimeDifferenceGreaterThanSeconds(cachedShop.getLastFetched(), new Date(),
                    SHOP_CACHE_TIMEOUT_SECONDS))
            {

                cachedShop = CachedShop.builder().shopId(shop.getShopId()).remainingStock(shop.getRemainingStock())
                        .remainingSpace(shop.getRemainingSpace()).lastFetched(new Date()).build();
                shopCache.put(cachedShop.getShopId(), cachedShop);
                QSFindItemAddOnOG.logger("Adding to ShopCache: " + shop.getShopId());

            }

            return (fetchRemainingStock ? cachedShop.getRemainingStock() : cachedShop.getRemainingSpace());

        }

    }

    private boolean checkIfQSHikariShopCacheImplemented() {

        String mainVersionStr = pluginVersion.split("\\.")[0];
        int mainVersion = Integer.parseInt(mainVersionStr);
        return mainVersion >= 6;

    }

    private void processPotentialShopMatchAndAddToFoundList(boolean toBuy, Shop shopIterator,
            List<FoundShopItemModel> shopsFoundList, Player searchingPlayer)
    {

        QSFindItemAddOnOG.logger("Shop match found: " + shopIterator.getLocation());
        // check for stock / space
        int stockOrSpace = (toBuy ? getRemainingStockOrSpaceFromShopCache(shopIterator, true)
                : getRemainingStockOrSpaceFromShopCache(shopIterator, false));
        if (isShopToBeIgnoredForFullOrEmpty(stockOrSpace)) {

            return;

        }

        // check if owner has enough balance for buying shops
        if (!toBuy && !isOwnerHavingEnoughBalance(shopIterator)) {

            QSFindItemAddOnOG.logger("Shop Owner is poor");
            return;

        }

        shopsFoundList.add(new FoundShopItemModel(shopIterator.getPrice(), QSApi.processStockOrSpace(stockOrSpace),
                shopIterator.getOwner().getUniqueIdOptional().orElse(new UUID(0, 0)), shopIterator.getLocation(),
                shopIterator.getItem(), toBuy));

    }

}
