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
package io.myzticbean.finditemaddon.dependencies;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.olziedev.playerwarps.api.PlayerWarpsAPI;
import com.olziedev.playerwarps.api.warp.Warp;

import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.utils.enums.NearestWarpModeEnum;
import lombok.experimental.UtilityClass;

/**
 * @author myzticbean
 */
@UtilityClass
public class PlayerWarpsPlugin {

	private static boolean isEnabled = false;
	private static List<Warp> allWarpsList = null;
	private static PlayerWarpsAPI playerWarpsAPI = null;
	private static final String ALL_WARPS_LIST_CLASSPATH = PlayerWarpsPlugin.class.getCanonicalName() + ".allWarpsList";

	public static void setup() {
		if(Bukkit.getPluginManager().isPluginEnabled("PlayerWarps")) {
			FindItemAddOn.logger("Found PlayerWarps");
			PlayerWarpsAPI.getInstance(api -> {
				playerWarpsAPI = api;
				isEnabled = true;
			});
		}
	}

	public static boolean getIsEnabled() { return isEnabled; }

	/**
	 * Issue #24 Fix: Changing all api get references to callback, making this method deprecated
	 * @return PlayerWarpsAPI api
	 */
	@Deprecated
	public static PlayerWarpsAPI getAPI() {
		return playerWarpsAPI;
	}

	public static List<Warp> getAllWarps() {
		if(!isEnabled) {
			return Collections.emptyList();
		}
		PlayerWarpsAPI.getInstance(api -> allWarpsList = api.getPlayerWarps(false));
		return allWarpsList;
	}

	public static void updateAllWarpsFromAPI() {
		if(isEnabled) {
			long start = System.currentTimeMillis();
			// Issue #24 Fix: Changing api instance to callback
			PlayerWarpsAPI.getInstance(api -> {
				allWarpsList = api.getPlayerWarps(false);
				FindItemAddOn.logger("Update complete for PlayerWarps list! Found " + getAllWarps().size() + " warps. Time took: " + (System.currentTimeMillis() - start) + "ms.");
			});
		}
	}

	public static void updateWarpsOnEventCall(Warp warp, boolean isRemoved) {
		FindItemAddOn.logger("Got a PlayerWarps event call... checking nearest-warp-mode");
		if(FindItemAddOn.getConfigProvider().NEAREST_WARP_MODE == NearestWarpModeEnum.PLAYER_WARPS.value()) {
			FindItemAddOn.logger("'nearest-warp-mode' found set to 2");
			if (getIsEnabled()) {
				FindItemAddOn.logger("PlayerWarps plugin is enabled");
				tryUpdateWarps(warp, isRemoved, 1);
			}
		}
		else {
			FindItemAddOn.logger("No update required to '" + ALL_WARPS_LIST_CLASSPATH + "' as PlayerWarps integration is disabled.");
		}
	}

	private static void tryUpdateWarps(Warp warp, boolean isRemoved, int updateTrialSequence) {
		// Issue #21 Fix: Adding a NPE check
		if(allWarpsList != null) {
			if(isRemoved) {
				if(allWarpsList.contains(warp)) {
					allWarpsList.remove(warp);
					FindItemAddOn.logger("Warp removed from allWarpsList: " + warp.getWarpName());
				} else {
					FindItemAddOn.logger("Error occurred while updating '" + ALL_WARPS_LIST_CLASSPATH + "'. Warp name: '" + warp.getWarpName() + "' does not exist!");
				}
			}
			else {
				allWarpsList.add(warp);
				FindItemAddOn.logger("New warp added to allWarpsList: " + warp.getWarpName());
			}
		}
		else {
			// Issue #21 Fix: forcing update of allWarpsList
			if(updateTrialSequence == 1) {
				updateAllWarpsFromAPI();
				tryUpdateWarps(warp, isRemoved, 2);
			} else {
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("Error occurred while updating '").append(ALL_WARPS_LIST_CLASSPATH).append("' as it is null! ")
				.append("Please install PlayerWarps by Olzie-12 if you would like to use 'nearest-warp-mode' as 2. ")
				.append("If PlayerWarps plugin is installed and issue persists, please contact the developer!");
				FindItemAddOn.logger(errorMsg.toString());
			}
		}
	}

	public static boolean isWarpLocked(Player player, String warpName) {
		Warp warp = playerWarpsAPI.getPlayerWarp(warpName, player);
		if(warp == null)
			return false;
		return warp.isWarpLocked();
	}
}
