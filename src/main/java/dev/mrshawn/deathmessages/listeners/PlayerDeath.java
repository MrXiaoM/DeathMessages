package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;


public class PlayerDeath implements Listener {
    private final FileSettings<Config> config = FileSettings.CONFIG;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (config == null) {
            e.getEntity().sendMessage("config == null");
            return;
        }
        if (this.config.getBoolean(Config.DISABLE_DEFAULT_MESSAGES)) {
            e.setDeathMessage(null);
        }
    }
}
