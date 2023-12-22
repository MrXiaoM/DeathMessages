package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.ExplosionManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.api.events.BroadcastDeathMessageEvent;
import dev.mrshawn.deathmessages.api.events.BroadcastEntityDeathMessageEvent;
import dev.mrshawn.deathmessages.config.Gangs;
import dev.mrshawn.deathmessages.config.Settings;
import dev.mrshawn.deathmessages.enums.MessageType;
import dev.mrshawn.deathmessages.enums.MobType;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.utils.Assets;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.mrshawn.deathmessages.utils.Assets.classSimple;


public class EntityDeath implements Listener {
    private static final FileSettings<Config> config = FileSettings.CONFIG;

    synchronized void onEntityDeath(EntityDeathEvent e) {
        EntityManager em;
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (Bukkit.getOnlinePlayers().contains(p)) {
                PlayerManager pm = PlayerManager.getPlayer(p);

                if (e.getEntity().getLastDamageCause() == null) {
                    pm.setLastDamageCause(EntityDamageEvent.DamageCause.CUSTOM);
                } else {
                    pm.setLastDamageCause(e.getEntity().getLastDamageCause().getCause());
                }
                if (pm.isBlacklisted()) {
                    return;
                }
                if (!(pm.getLastEntityDamager() instanceof LivingEntity) || pm.getLastEntityDamager() == e.getEntity()) {
                    if (pm.getLastExplosiveEntity() instanceof EnderCrystal) {
                        TextComponent tx = Assets.getNaturalDeath(pm, "End-Crystal");
                        if (tx == null) {
                            return;
                        }
                        BroadcastDeathMessageEvent event = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, tx, getWorlds(p), false);
                        Bukkit.getPluginManager().callEvent(event);
                        return;
                    } else if (pm.getLastExplosiveEntity() instanceof TNTPrimed) {
                        TextComponent tx2 = Assets.getNaturalDeath(pm, "TNT");
                        if (tx2 == null) {
                            return;
                        }
                        BroadcastDeathMessageEvent event2 = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, tx2, getWorlds(p), false);
                        Bukkit.getPluginManager().callEvent(event2);
                        return;
                    } else if (pm.getLastExplosiveEntity() instanceof Firework) {
                        TextComponent tx3 = Assets.getNaturalDeath(pm, "Firework");
                        if (tx3 == null) {
                            return;
                        }
                        BroadcastDeathMessageEvent event3 = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, tx3, getWorlds(p), false);
                        Bukkit.getPluginManager().callEvent(event3);
                        return;
                    } else if (pm.getLastClimbing() != null && pm.getLastDamage().equals(EntityDamageEvent.DamageCause.FALL)) {
                        TextComponent tx4 = Assets.getNaturalDeath(pm, "Climbable");
                        if (tx4 == null) {
                            return;
                        }
                        BroadcastDeathMessageEvent event4 = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, tx4, getWorlds(p), false);
                        Bukkit.getPluginManager().callEvent(event4);
                        return;
                    } else if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                        ExplosionManager explosionManager = ExplosionManager.getManagerIfEffected(p.getUniqueId());
                        if (explosionManager == null) {
                            return;
                        }
                        TextComponent tx5 = null;
                        if (explosionManager.getMaterial().name().contains("BED")) {
                            tx5 = Assets.getNaturalDeath(pm, "Bed");
                        }
                        if (DeathMessages.majorVersion() >= 16 && explosionManager.getMaterial().equals(Material.RESPAWN_ANCHOR)) {
                            tx5 = Assets.getNaturalDeath(pm, "Respawn-Anchor");
                        }
                        if (tx5 == null) {
                            return;
                        }
                        BroadcastDeathMessageEvent event5 = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, tx5, getWorlds(p), false);
                        Bukkit.getPluginManager().callEvent(event5);
                        return;
                    } else {
                        if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                            BroadcastDeathMessageEvent event6 = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, Assets.getNaturalDeath(pm, Assets.getSimpleProjectile(pm.getLastProjectileEntity())), getWorlds(p), false);
                            Bukkit.getPluginManager().callEvent(event6);
                            return;
                        }
                        TextComponent tx6 = Assets.getNaturalDeath(pm, Assets.getSimpleCause(pm.getLastDamage()));
                        if (tx6 == null) {
                            return;
                        }
                        BroadcastDeathMessageEvent event7 = new BroadcastDeathMessageEvent(p, null, MessageType.NATURAL, tx6, getWorlds(p), false);
                        Bukkit.getPluginManager().callEvent(event7);
                        return;
                    }
                }
                Entity ent = pm.getLastEntityDamager();
                String mobName = classSimple(ent);
                int radius = Gangs.getInstance().getConfig().getInt("Gang.Mobs." + mobName + ".Radius");
                int amount = Gangs.getInstance().getConfig().getInt("Gang.Mobs." + mobName + ".Amount");
                boolean gangKill = false;
                if (Gangs.getInstance().getConfig().getBoolean("Gang.Enabled")) {
                    int totalMobEntities = 0;
                    for (Entity entities : p.getNearbyEntities(radius, radius, radius)) {
                        if (entities.getType().equals(ent.getType())) {
                            totalMobEntities++;
                        }
                    }
                    if (totalMobEntities >= amount) {
                        gangKill = true;
                    }
                }
                TextComponent tx7 = Assets.playerDeathMessage(pm, gangKill);
                if (tx7 == null) {
                    return;
                }
                if (ent instanceof Player) {
                    BroadcastDeathMessageEvent event8 = new BroadcastDeathMessageEvent(p, (LivingEntity) pm.getLastEntityDamager(), MessageType.PLAYER, tx7, getWorlds(p), gangKill);
                    Bukkit.getPluginManager().callEvent(event8);
                    return;
                }
                BroadcastDeathMessageEvent event9 = new BroadcastDeathMessageEvent(p, (LivingEntity) pm.getLastEntityDamager(), MessageType.MOB, tx7, getWorlds(p), gangKill);
                Bukkit.getPluginManager().callEvent(event9);
                return;
            }
        }
        MobType mobType = MobType.VANILLA;
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getEntity().getUniqueId())) {
            mobType = MobType.MYTHIC_MOB;
        }
        if (EntityManager.getEntity(e.getEntity().getUniqueId()) == null || (em = EntityManager.getEntity(e.getEntity().getUniqueId())) == null || em.getLastPlayerDamager() == null) {
            return;
        }
        PlayerManager damager = em.getLastPlayerDamager();
        TextComponent tx8 = Assets.entityDeathMessage(em, mobType);
        if (tx8 == null) {
            return;
        }
        BroadcastEntityDeathMessageEvent event10 = new BroadcastEntityDeathMessageEvent(damager, e.getEntity(), MessageType.ENTITY, tx8, getWorlds(e.getEntity()));
        Bukkit.getPluginManager().callEvent(event10);
    }

    public static List<World> getWorlds(Entity e) {
        List<World> broadcastWorlds = new ArrayList<>();
        if (config.getStringList(Config.DISABLED_WORLDS).contains(e.getWorld().getName())) {
            return broadcastWorlds;
        }
        ConfigurationSection worldGroups = Settings.getInstance().getConfig().getConfigurationSection("World-Groups");
        if (config.getBoolean(Config.PER_WORLD_MESSAGES)) {
            if (worldGroups != null) {
                for (String groups : worldGroups.getKeys(false)) {
                    List<String> worlds = Settings.getInstance().getConfig().getStringList("World-Groups." + groups);
                    if (worlds.contains(e.getWorld().getName())) {
                        for (String single : worlds) {
                            broadcastWorlds.add(Bukkit.getWorld(single));
                        }
                    }
                }
            }
            if (broadcastWorlds.isEmpty()) {
                broadcastWorlds.add(e.getWorld());
            }
            return broadcastWorlds;
        }
        return Bukkit.getWorlds();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath_LOWEST(EntityDeathEvent e) {
        if (DeathMessages.getEventPriority().equals(EventPriority.LOWEST)) {
            onEntityDeath(e);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeath_LOW(EntityDeathEvent e) {
        if (DeathMessages.getEventPriority().equals(EventPriority.LOW)) {
            onEntityDeath(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath_NORMAL(EntityDeathEvent e) {
        if (DeathMessages.getEventPriority().equals(EventPriority.NORMAL)) {
            onEntityDeath(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath_HIGH(EntityDeathEvent e) {
        if (DeathMessages.getEventPriority().equals(EventPriority.HIGH)) {
            onEntityDeath(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath_HIGHEST(EntityDeathEvent e) {
        if (DeathMessages.getEventPriority().equals(EventPriority.HIGHEST)) {
            onEntityDeath(e);
        }
    }
}
