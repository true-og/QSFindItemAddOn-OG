package io.myzticbean.finditemaddon.Listeners;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginEnableEventListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {

        if (!FindItemAddOnOG.isQSReremakeInstalled() && !FindItemAddOnOG.isQSHikariInstalled()) {

//            if(!event.getPlugin().getName().equalsIgnoreCase("QuickShop")
//                    && !event.getPlugin().getName().equalsIgnoreCase("QuickShop-Hikari")) {
//                // do nothing
//            }
            if (event.getPlugin().getName().equalsIgnoreCase("QuickShop") && !FindItemAddOnOG.isQSHikariInstalled()) {

                FindItemAddOnOG.setQSReremakeInstalled(true);

            } else if (event.getPlugin().getName().equalsIgnoreCase("QuickShop-Hikari")
                    && !FindItemAddOnOG.isQSReremakeInstalled())
            {

                FindItemAddOnOG.setQSHikariInstalled(true);

            }

        }

    }

}
