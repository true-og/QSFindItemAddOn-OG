package io.myzticbean.finditemaddon.utils;

import io.myzticbean.finditemaddon.FindItemAddOn;
import io.papermc.lib.PaperLib;
import lombok.experimental.UtilityClass;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.ExecutionException;

@UtilityClass
public class PlayerUtil {
    public void sendMessage(HumanEntity player, String message) {
        Bukkit.getScheduler().runTask(FindItemAddOn.getInstance(), () -> player.sendMessage(ColorTranslator.translateColorCodes(message)));
    }

    public void teleport(Player player, Location locToTeleport) {
        PaperLib.teleportAsync(player, locToTeleport, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public boolean hasPermission(Player player, String permission) {
        if (Bukkit.isPrimaryThread()) {
            return player.hasPermission(permission);
        } else {
            try {
                return Bukkit
                        .getScheduler()
                        .callSyncMethod(FindItemAddOn.getInstance(), () -> player.hasPermission(permission))
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
