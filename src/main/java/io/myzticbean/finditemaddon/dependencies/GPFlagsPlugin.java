package io.myzticbean.finditemaddon.dependencies;

import org.bukkit.Bukkit;

import io.myzticbean.finditemaddon.FindItemAddOn;
import me.ryanhamshire.GPFlags.GPFlags;

public class GPFlagsPlugin {

	private static GPFlags gpFlags;

	public static void setup() {
		if(Bukkit.getPluginManager().isPluginEnabled("GPFlags")) {
			gpFlags = (GPFlags) Bukkit.getServer().getPluginManager().getPlugin("GPFlags");
			if(gpFlags != null) {
				FindItemAddOn.logger("Found GPFlags");
			}
		}
	}

	public static boolean isEnabled() {
		return gpFlags.isEnabled();
	}

	public static GPFlags getAPI() {
		return gpFlags;
	}

}
