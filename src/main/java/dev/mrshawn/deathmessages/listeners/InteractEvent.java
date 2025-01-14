package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.ExplosionManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.api.events.DMBlockExplodeEvent;
import dev.mrshawn.deathmessages.enums.MobType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class InteractEvent implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent e) {
        EntityManager entity;
        EntityManager entity2;
        Block b = e.getClickedBlock();
        if (b == null || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || b.getType().equals(Material.AIR)) {
            return;
        }
        World.Environment environment = b.getWorld().getEnvironment();
        if (environment.equals(World.Environment.NETHER) || environment.equals(World.Environment.THE_END)) {
            if (b.getType().name().contains("BED") && !b.getType().equals(Material.BEDROCK)) {
                List<UUID> effected = new ArrayList<>();
                for (Player p : e.getClickedBlock().getWorld().getPlayers()) {
                    PlayerManager effect = PlayerManager.getPlayer(p);
                    if (p.getLocation().distanceSquared(b.getLocation()) < 100.0d) {
                        effected.add(p.getUniqueId());
                        effect.setLastEntityDamager(e.getPlayer());
                    }
                }
                for (Entity ent : e.getClickedBlock().getWorld().getEntities()) {
                    if (!(ent instanceof Player) && ent.getLocation().distanceSquared(b.getLocation()) < 100.0d) {
                        if (EntityManager.getEntity(ent.getUniqueId()) == null) {
                            entity = new EntityManager(ent, ent.getUniqueId(), MobType.VANILLA);
                        } else {
                            entity = EntityManager.getEntity(ent.getUniqueId());
                        }
                        effected.add(ent.getUniqueId());
                        if (entity != null) entity.setLastPlayerDamager(PlayerManager.getPlayer(e.getPlayer()));
                    }
                }
                new ExplosionManager(e.getPlayer().getUniqueId(), b.getType(), b.getLocation(), effected);
                DMBlockExplodeEvent explodeEvent = new DMBlockExplodeEvent(e.getPlayer(), b);
                Bukkit.getPluginManager().callEvent(explodeEvent);
            }
        } else if (!b.getWorld().getEnvironment().equals(World.Environment.NETHER) && DeathMessages.majorVersion() >= 16 && b.getType().name().toUpperCase().equals("RESPAWN_ANCHOR")) {
            RespawnAnchor anchor = (RespawnAnchor) b.getBlockData();
            if (anchor.getCharges() != anchor.getMaximumCharges() && !e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GLOWSTONE)) {
                return;
            }
            List<UUID> effected2 = new ArrayList<>();
            for (Player p2 : e.getClickedBlock().getWorld().getPlayers()) {
                if (p2.getLocation().distanceSquared(b.getLocation()) < 100.0d) {
                    PlayerManager effect2 = PlayerManager.getPlayer(p2);
                    effected2.add(p2.getUniqueId());
                    effect2.setLastEntityDamager(e.getPlayer());
                }
            }
            for (Entity ent2 : e.getClickedBlock().getWorld().getEntities()) {
                if (!(ent2 instanceof Player) && ent2.getLocation().distanceSquared(b.getLocation()) < 100.0d) {
                    if (EntityManager.getEntity(ent2.getUniqueId()) == null) {
                        entity2 = new EntityManager(ent2, ent2.getUniqueId(), MobType.VANILLA);
                    } else {
                        entity2 = EntityManager.getEntity(ent2.getUniqueId());
                    }
                    effected2.add(ent2.getUniqueId());
                    if (entity2 != null) entity2.setLastPlayerDamager(PlayerManager.getPlayer(e.getPlayer()));
                }
            }
            new ExplosionManager(e.getPlayer().getUniqueId(), b.getType(), b.getLocation(), effected2);
            DMBlockExplodeEvent explodeEvent2 = new DMBlockExplodeEvent(e.getPlayer(), b);
            Bukkit.getPluginManager().callEvent(explodeEvent2);
        }
    }
}
