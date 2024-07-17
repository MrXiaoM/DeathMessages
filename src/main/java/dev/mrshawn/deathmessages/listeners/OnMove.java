package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.utils.DeathResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;


public class OnMove implements Listener {
    boolean falling;
    Material lastBlock;
    boolean message;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo() != null && DeathResolver.isClimbable(e.getTo().getBlock())) {
            PlayerManager.getPlayer(p).setLastClimbing(e.getTo().getBlock().getType());
            this.lastBlock = e.getTo().getBlock().getType();
        } else if (p.getFallDistance() > 0.0f) {
            this.message = true;
            if (!this.falling) {
                this.falling = true;
                this.message = false;
            }
        } else if (this.message) {
            PlayerManager.getPlayer(p).setLastClimbing(null);
            this.falling = false;
            this.message = false;
        }
    }
}
