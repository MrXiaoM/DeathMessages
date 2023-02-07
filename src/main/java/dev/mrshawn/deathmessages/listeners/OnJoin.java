package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;


public class OnJoin implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        new BukkitRunnable() {
            public void run() {
                PlayerManager.getPlayer(p);
            }
        }.runTaskAsynchronously(DeathMessages.getInstance());
        if (DeathMessages.bungeeInit) {
            new BukkitRunnable() {
                public void run() {
                    if (DeathMessages.bungeeServerNameRequest) {
                        PluginMessaging.sendServerNameRequest(p);
                    }
                }
            }.runTaskLater(DeathMessages.getInstance(), 5L);
        }
    }
}
