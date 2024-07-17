package dev.mrshawn.deathmessages.listeners.mythicmobs;

import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.api.events.BroadcastEntityDeathMessageEvent;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.config.Settings;
import dev.mrshawn.deathmessages.enums.MessageType;
import dev.mrshawn.deathmessages.enums.MobType;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.utils.DeathResolver;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MobDeath4 implements Listener {
    private static final FileSettings<Config> config = FileSettings.CONFIG;

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent e) {
        ConfigurationSection mmEntitiesSection = getEntityDeathMessages().getConfigurationSection("Mythic-Mobs-Entities");
        Set<String> mmKeys = mmEntitiesSection == null ? null : mmEntitiesSection.getKeys(false);
        if (mmKeys == null || mmKeys.isEmpty()) return;

        for (String customMobs : mmKeys) {
            if (e.getMob().getType().getInternalName().equals(customMobs)) {
                EntityManager em = EntityManager.getEntity(e.getEntity().getUniqueId());
                if (em == null || em.getLastPlayerDamager() == null) return;

                PlayerManager damager = em.getLastPlayerDamager();
                TextComponent tx = DeathResolver.entityDeathMessage(em, MobType.MYTHIC_MOB);
                if (tx == null) return;

                BroadcastEntityDeathMessageEvent event = new BroadcastEntityDeathMessageEvent(damager, e.getEntity(), MessageType.ENTITY, tx, getWorlds(e.getEntity()));
                Bukkit.getPluginManager().callEvent(event);
            }
        }
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

    public static FileConfiguration getEntityDeathMessages() {
        return Messages.getEntityDeathMessages();
    }
}
