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

/**
 * @author myzticbean
 */
public class HiddenShopModel {

    private String worldName;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private String shopOwnerUUID;

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

}
