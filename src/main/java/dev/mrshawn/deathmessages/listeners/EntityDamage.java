package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.EntityDeathMessages;
import dev.mrshawn.deathmessages.enums.MobType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Set;

import static dev.mrshawn.deathmessages.utils.Assets.classSimple;


public class EntityDamage implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        EntityManager entity;
        if (e.isCancelled()) return;

        ConfigurationSection entitiesSection = EntityDeathMessages.getInstance().getConfig().getConfigurationSection("Entities");
        ConfigurationSection mmEntitiesSection = EntityDeathMessages.getInstance().getConfig().getConfigurationSection("Mythic-Mobs-Entities");
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (Bukkit.getOnlinePlayers().contains(p)) {
                PlayerManager pm = PlayerManager.getPlayer(p);
                pm.setLastDamageCause(e.getCause());
            }
        } else if (!(e.getEntity() instanceof Player) && entitiesSection != null) {
            Set<String> listenedMobs = entitiesSection.getKeys(false);
            if (mmEntitiesSection != null && DeathMessages.getInstance().mythicmobsEnabled) {
                listenedMobs.addAll(mmEntitiesSection.getKeys(false));
            }
            if (listenedMobs.isEmpty()) {
                return;
            }
            for (String listened : listenedMobs) {
                if (listened.contains(classSimple(e.getEntity()))) {
                    if (EntityManager.getEntity(e.getEntity().getUniqueId()) == null) {
                        MobType mobType = MobType.VANILLA;
                        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getEntity().getUniqueId())) {
                            mobType = MobType.MYTHIC_MOB;
                        }
                        entity = new EntityManager(e.getEntity(), e.getEntity().getUniqueId(), mobType);
                    } else {
                        entity = EntityManager.getEntity(e.getEntity().getUniqueId());
                    }
                    if (entity != null) entity.setLastDamageCause(e.getCause());
                }
            }
        }
    }
}
