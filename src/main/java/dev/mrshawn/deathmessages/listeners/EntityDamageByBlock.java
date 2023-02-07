package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.EntityDeathMessages;
import dev.mrshawn.deathmessages.enums.MobType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

import java.util.Set;

import static dev.mrshawn.deathmessages.utils.Assets.classSimple;


public class EntityDamageByBlock implements Listener {
    public static void d(Entity e, String msg) {
        if (e.getName().equalsIgnoreCase("LittleCatX"))
            e.sendMessage(msg);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDamageByBlockEvent e) {
        d(e.getEntity(), "方块伤害");
        EntityManager entity;
        ConfigurationSection entitiesSection = EntityDeathMessages.getInstance().getConfig().getConfigurationSection("Entities");
        ConfigurationSection mmEntitiesSection = EntityDeathMessages.getInstance().getConfig().getConfigurationSection("Mythic-Mobs-Entities");
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (Bukkit.getOnlinePlayers().contains(p)) {
                PlayerManager pm = PlayerManager.getPlayer(p);
                pm.setLastDamageCause(e.getCause());
                d(p, "已设置最后伤害原因为 "+e.getCause());
            }
            else d(p, "玩家不在线?");
        } else if (entitiesSection != null) {
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
                        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.getAPIHelper().isMythicMob(e.getEntity().getUniqueId())) {
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
