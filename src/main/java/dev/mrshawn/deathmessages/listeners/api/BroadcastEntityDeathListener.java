package dev.mrshawn.deathmessages.listeners.api;

import com.sk89q.worldguard.protection.flags.StateFlag;
import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.api.events.BroadcastEntityDeathMessageEvent;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.listeners.PluginMessaging;
import dev.mrshawn.deathmessages.utils.DeathResolver;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;


public class BroadcastEntityDeathListener implements Listener {
    private static final FileSettings<Config> config = FileSettings.CONFIG;

    @EventHandler
    public void broadcastListener(BroadcastEntityDeathMessageEvent e) {
        PlayerManager pm = e.getPlayer();
        boolean hasOwner = false;
        Entity entity = e.getEntity();
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.getOwner() != null) {
                hasOwner = true;
            }
        }
        if (!e.isCancelled()) {
            if (Messages.getInstance().getConfig().getBoolean("Console.Enabled")) {
                String message = DeathResolver.entityDeathPlaceholders(Messages.getInstance().getConfig().getString("Console.Message", ""), pm.getPlayer(), e.getEntity(), hasOwner);
                Bukkit.getConsoleSender().sendMessage(message.replaceAll("%message%", Matcher.quoteReplacement(e.getTextComponent().toLegacyText())));
            }
            if (pm.isInCooldown()) {
                return;
            }
            pm.setCooldown();
            boolean privateTameable = config.getBoolean(Config.PRIVATE_MESSAGES_MOBS);
            for (World w : e.getBroadcastedWorlds()) {
                for (Player pls : w.getPlayers()) {
                    if (!config.getStringList(Config.DISABLED_WORLDS).contains(w.getName())) {
                        PlayerManager pms = PlayerManager.getPlayer(pls);
                        if (privateTameable && pms.getUUID().equals(pm.getPlayer().getUniqueId())) {
                            if (pms.getMessagesEnabled()) {
                                pls.spigot().sendMessage(e.getTextComponent());
                            }
                        } else {
                            if (pms.getMessagesEnabled()) {
                                if (DeathMessages.worldGuardExtension != null && DeathMessages.worldGuardExtension.getRegionState(pls, e.getMessageType().getValue()).equals(StateFlag.State.DENY)) {
                                    return;
                                }
                                pls.spigot().sendMessage(e.getTextComponent());
                                PluginMessaging.sendPluginMSG(pms.getPlayer(), e.getTextComponent().toString());
                            }
                        }
                    }
                }
            }
            PluginMessaging.sendPluginMSG(e.getPlayer().getPlayer(), ComponentSerializer.toString(e.getTextComponent()));
        }
        EntityManager em = EntityManager.getEntity(e.getEntity().getUniqueId());
        if (em != null) em.destroy();
    }
}
