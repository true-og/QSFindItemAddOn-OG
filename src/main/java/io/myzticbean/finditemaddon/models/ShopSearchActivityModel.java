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

import java.util.List;

/**
 * @author myzticbean
 */
public class ShopSearchActivityModel {

	public ShopSearchActivityModel(String worldName, double x, double y, double z, float pitch, float yaw,
		String shopOwnerUUID, List<PlayerShopVisitModel> playerVisitList, boolean isHiddenFromSearch) {
		super();
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.shopOwnerUUID = shopOwnerUUID;
		this.playerVisitList = playerVisitList;
		this.isHiddenFromSearch = isHiddenFromSearch;
	}

	private String worldName;
	private double x;
	private double y;
	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public String getShopOwnerUUID() {
		return shopOwnerUUID;
	}

	public void setShopOwnerUUID(String shopOwnerUUID) {
		this.shopOwnerUUID = shopOwnerUUID;
	}

	public List<PlayerShopVisitModel> getPlayerVisitList() {
		return playerVisitList;
	}

	public void setPlayerVisitList(List<PlayerShopVisitModel> playerVisitList) {
		this.playerVisitList = playerVisitList;
	}

	public boolean isHiddenFromSearch() {
		return isHiddenFromSearch;
	}

	public void setHiddenFromSearch(boolean isHiddenFromSearch) {
		this.isHiddenFromSearch = isHiddenFromSearch;
	}

	private double z;
	private float pitch;
	private float yaw;
	private String shopOwnerUUID;
	private List<PlayerShopVisitModel> playerVisitList;
	private boolean isHiddenFromSearch;

	public boolean compareWith(String targetWorldName, double targetX, double targetY, double targetZ, String targetShopOwnerUUID) {
		return this.getWorldName().equalsIgnoreCase(targetWorldName)
				&& this.getX() == targetX
				&& this.getY() == targetY
				&& this.getZ() == targetZ
				&& this.getShopOwnerUUID().equalsIgnoreCase(targetShopOwnerUUID);
	}

	public boolean compareWith(String targetWorldName, double targetX, double targetY, double targetZ) {
		return this.getWorldName().equalsIgnoreCase(targetWorldName)
				&& this.getX() == targetX
				&& this.getY() == targetY
				&& this.getZ() == targetZ;
	}

}
