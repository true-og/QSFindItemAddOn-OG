package io.myzticbean.finditemaddon.utils;

import io.myzticbean.finditemaddon.FindItemAddOn;
import lombok.experimental.UtilityClass;
import me.kodysimpson.simpapi.colors.ColorTranslator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class PlayerUtil {
    public void sendMessage(Player player, String message) {
        Bukkit.getScheduler().runTask(FindItemAddOn.getInstance(), () -> player.sendMessage(ColorTranslator.translateColorCodes(message)));
    }
}
