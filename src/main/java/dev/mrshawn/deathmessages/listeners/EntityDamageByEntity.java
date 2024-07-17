package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.EntityDeathMessages;
import dev.mrshawn.deathmessages.enums.MobType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static dev.mrshawn.deathmessages.config.Messages.classSimple;


public class EntityDamageByEntity implements Listener {
    public static final Map<UUID, Entity> explosions = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void entityDamageByEntity(EntityDamageByEntityEvent e) {
        EntityManager em;
        ConfigurationSection entitiesSection = EntityDeathMessages.getInstance().getConfig().getConfigurationSection("Entities");
        ConfigurationSection mmEntitiesSection = EntityDeathMessages.getInstance().getConfig().getConfigurationSection("Mythic-Mobs-Entities");
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (Bukkit.getOnlinePlayers().contains(p)) {
                PlayerManager pm = PlayerManager.getPlayer(p);
                if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                    if ((e.getDamager() instanceof EnderCrystal) && explosions.containsKey(e.getDamager().getUniqueId())) {
                        pm.setLastEntityDamager(explosions.get(e.getDamager().getUniqueId()));
                        pm.setLastExplosiveEntity(e.getDamager());
                    } else if (e.getDamager() instanceof TNTPrimed) {
                        TNTPrimed tnt = (TNTPrimed) e.getDamager();
                        if (tnt.getSource() instanceof LivingEntity) {
                            pm.setLastEntityDamager(tnt.getSource());
                        }
                        pm.setLastExplosiveEntity(e.getDamager());
                    } else if (e.getDamager() instanceof Firework) {
                        Firework firework = (Firework) e.getDamager();
                        try {
                            if (firework.getShooter() instanceof LivingEntity) {
                                pm.setLastEntityDamager((Entity) firework.getShooter());
                            }
                            pm.setLastExplosiveEntity(e.getDamager());
                        } catch (NoSuchMethodError ignored) {
                        }
                    } else {
                        pm.setLastEntityDamager(e.getDamager());
                        pm.setLastExplosiveEntity(e.getDamager());
                    }
                } else if (e.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile) e.getDamager();
                    if (projectile.getShooter() instanceof LivingEntity) {
                        pm.setLastEntityDamager((Entity) projectile.getShooter());
                    }
                    pm.setLastProjectileEntity(projectile);
                } else if (e.getDamager() instanceof FallingBlock) {
                    pm.setLastEntityDamager(e.getDamager());
                } else if (e.getDamager().getType().isAlive()) {
                    pm.setLastEntityDamager(e.getDamager());
                } else if (DeathMessages.majorVersion() >= 11 && (e.getDamager() instanceof EvokerFangs)) {
                    EvokerFangs evokerFangs = (EvokerFangs) e.getDamager();
                    pm.setLastEntityDamager(evokerFangs.getOwner());
                }
            }
        } else if (!(e.getEntity() instanceof Player) && (e.getDamager() instanceof Player)) {
            if (entitiesSection == null) {
                return;
            }
            Set<String> listenedMobs = entitiesSection.getKeys(false);
            if (mmEntitiesSection != null && DeathMessages.getInstance().mythicmobsEnabled) {
                listenedMobs.addAll(mmEntitiesSection.getKeys(false));
            }
            if (listenedMobs.isEmpty()) {
                return;
            }
            for (String listened : listenedMobs) {
                if (listened.contains(classSimple(e.getEntity())) || (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getEntity().getUniqueId()))) {
                    if (EntityManager.getEntity(e.getEntity().getUniqueId()) == null) {
                        MobType mobType = MobType.VANILLA;
                        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getEntity().getUniqueId())) {
                            mobType = MobType.MYTHIC_MOB;
                        }
                        em = new EntityManager(e.getEntity(), e.getEntity().getUniqueId(), mobType);
                    } else {
                        em = EntityManager.getEntity(e.getEntity().getUniqueId());
                    }
                    if (em != null) {
                        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                            if ((e.getDamager() instanceof EnderCrystal) && explosions.containsKey(e.getDamager().getUniqueId())) {
                                if (explosions.get(e.getDamager().getUniqueId()) instanceof Player) {
                                    em.setLastPlayerDamager(PlayerManager.getPlayer((Player) explosions.get(e.getDamager().getUniqueId())));
                                    em.setLastExplosiveEntity(e.getDamager());
                                }
                            } else {
                                Entity damager = e.getDamager();
                                if (damager instanceof TNTPrimed) {
                                    TNTPrimed tnt2 = (TNTPrimed) damager;
                                    if (tnt2.getSource() instanceof Player) {
                                        em.setLastPlayerDamager(PlayerManager.getPlayer((Player) tnt2.getSource()));
                                    }
                                    em.setLastExplosiveEntity(e.getDamager());
                                } else if (e.getDamager() instanceof Firework) {
                                    Firework firework2 = (Firework) e.getDamager();
                                    try {
                                        if (firework2.getShooter() instanceof Player) {
                                            em.setLastPlayerDamager(PlayerManager.getPlayer((Player) firework2.getShooter()));
                                        }
                                        em.setLastExplosiveEntity(e.getDamager());
                                    } catch (NoSuchMethodError ignored) {
                                    }
                                } else {
                                    em.setLastPlayerDamager(PlayerManager.getPlayer((Player) e.getDamager()));
                                    em.setLastExplosiveEntity(e.getDamager());
                                }
                            }
                        } else if (e.getDamager() instanceof Projectile) {
                            Projectile projectile2 = (Projectile) e.getDamager();
                            if (projectile2.getShooter() instanceof Player) {
                                em.setLastPlayerDamager(PlayerManager.getPlayer((Player) projectile2.getShooter()));
                            }
                            em.setLastProjectileEntity(projectile2);
                        } else if (e.getDamager() instanceof Player) {
                            em.setLastPlayerDamager(PlayerManager.getPlayer((Player) e.getDamager()));
                        }
                    }
                }
            }
        }
        if (e.getEntity() instanceof EnderCrystal) {
            if (e.getDamager().getType().isAlive()) {
                explosions.put(e.getEntity().getUniqueId(), e.getDamager());
            } else if (e.getDamager() instanceof Projectile) {
                Projectile projectile3 = (Projectile) e.getDamager();
                if (projectile3.getShooter() instanceof LivingEntity) {
                    explosions.put(e.getEntity().getUniqueId(), (Entity) projectile3.getShooter());
                }
            }
        }
    }
}
