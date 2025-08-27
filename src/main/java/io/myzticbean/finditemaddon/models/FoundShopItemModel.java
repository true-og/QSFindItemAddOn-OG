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
package io.myzticbean.finditemaddon.models;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Model for the Shop Item lore that will be shown in the search GUI
 *
 * @author myzticbean
 */
public class FoundShopItemModel {

    public FoundShopItemModel(double shopPrice, int remainingStockOrSpace, UUID shopOwner, Location shopLocation,
            ItemStack item, boolean toBuy)
    {

        super();
        this.shopPrice = shopPrice;
        this.remainingStockOrSpace = remainingStockOrSpace;
        this.shopOwner = shopOwner;
        this.shopLocation = shopLocation;
        this.item = item;
        this.toBuy = toBuy;

    }

    public double getShopPrice() {

        return shopPrice;

    }

    public int getRemainingStockOrSpace() {

        return remainingStockOrSpace;

    }

    public UUID getShopOwner() {

        return shopOwner;

    }

    public Location getShopLocation() {

        return shopLocation;

    }

    public ItemStack getItem() {

        return item;

    }

    public boolean isToBuy() {

        return toBuy;

    }

    private double shopPrice;
    private int remainingStockOrSpace;
    private UUID shopOwner;
    private Location shopLocation;
    private ItemStack item;
    private boolean toBuy;

}
