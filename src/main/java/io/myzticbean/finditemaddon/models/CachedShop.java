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

import java.util.Date;

import org.bukkit.Location;

public class CachedShop {
	private long shopId;
	private Location shopLocation;
	private int remainingStock;
	private int remainingSpace;
	private Date lastFetched;

	// Private constructor to enforce usage of Builder
	private CachedShop(Builder builder) {
		this.shopId = builder.shopId;
		this.shopLocation = builder.shopLocation;
		this.remainingStock = builder.remainingStock;
		this.remainingSpace = builder.remainingSpace;
		this.lastFetched = builder.lastFetched;
	}

	// Getters
	public long getShopId() { return shopId; }
	public Location getShopLocation() { return shopLocation; }
	public int getRemainingStock() { return remainingStock; }
	public int getRemainingSpace() { return remainingSpace; }
	public Date getLastFetched() { return lastFetched; }

	// Builder class
	public static class Builder {
		private long shopId;
		private Location shopLocation;
		private int remainingStock;
		private int remainingSpace;
		private Date lastFetched;

		public static Builder builder() {
			return new Builder();
		}

		public Builder shopId(long shopId) {
			this.shopId = shopId;
			return this;
		}

		public Builder shopLocation(Location shopLocation) {
			this.shopLocation = shopLocation;
			return this;
		}

		public Builder remainingStock(int remainingStock) {
			this.remainingStock = remainingStock;
			return this;
		}

		public Builder remainingSpace(int remainingSpace) {
			this.remainingSpace = remainingSpace;
			return this;
		}

		public Builder lastFetched(Date lastFetched) {
			this.lastFetched = lastFetched;
			return this;
		}

		public CachedShop build() {
			return new CachedShop(this);
		}
	}

	// Static method to start the builder
	public static Builder builder() {
		return new Builder();
	}
}
