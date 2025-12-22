package io.myzticbean.finditemaddon.Utils.WarpUtils;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import io.myzticbean.finditemaddon.Dependencies.EssentialsXPlugin;

public class WarpUtils {

    public static void updateWarps() {

        final boolean condition = FindItemAddOnOG.getConfigProvider().shopGUIItemLoreHasKey("{NEAREST_WARP}")
                && FindItemAddOnOG.getConfigProvider().NEAREST_WARP_MODE == 1 && EssentialsXPlugin.isEnabled();
        if (condition) {

            EssentialsXPlugin.updateAllWarps();

        }

    }

}