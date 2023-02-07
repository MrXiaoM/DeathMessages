package dev.mrshawn.deathmessages.hooks;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.PlayerManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class PlaceholderAPIExtension extends PlaceholderExpansion {
    private final DeathMessages plugin;

    public PlaceholderAPIExtension(DeathMessages plugin) {
        this.plugin = plugin;
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    @NotNull
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    @NotNull
    public String getIdentifier() {
        return "deathmessages";
    }

    @NotNull
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        PlayerManager pm = PlayerManager.getPlayer(player);
        if (identifier.equals("messages_enabled")) {
            return String.valueOf(pm.getMessagesEnabled());
        }
        if (identifier.equals("is_blacklisted")) {
            return String.valueOf(pm.isBlacklisted());
        }
        if (identifier.equals("victim_name")) {
            return pm.getPlayer().getName();
        }
        if (identifier.equals("victim_display_name")) {
            return pm.getPlayer().getDisplayName();
        }
        if (identifier.equals("killer_name")) {
            if (pm.getLastEntityDamager() == null) {
                return "null";
            }
            return pm.getLastEntityDamager().getName();
        } else if (identifier.equals("killer_display_name")) {
            if (pm.getLastEntityDamager() == null) {
                return "null";
            }
            return pm.getLastEntityDamager().getCustomName();
        } else {
            return null;
        }
    }
}
