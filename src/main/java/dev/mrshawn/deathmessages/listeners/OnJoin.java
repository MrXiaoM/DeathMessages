package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoin implements Listener {
    private final DeathMessages plugin;
    public OnJoin(DeathMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        plugin.getScheduler().runAsync((t) -> PlayerManager.getPlayer(p));
        if (DeathMessages.bungeeInit && DeathMessages.bungeeServerNameRequest) {
            plugin.getScheduler().runLater(() -> {
                if (DeathMessages.bungeeServerNameRequest) {
                    PluginMessaging.sendServerNameRequest(p);
                }
            }, 5L);
        }
    }
}
