package dev.mrshawn.deathmessages.listeners.api;

import com.sk89q.worldguard.protection.flags.StateFlag;
import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.api.events.BroadcastDeathMessageEvent;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.enums.MessageType;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.listeners.PluginMessaging;
import dev.mrshawn.deathmessages.utils.DeathResolver;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.regex.Matcher;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class BroadcastPlayerDeathListener implements Listener {
    private static final FileSettings<Config> config = FileSettings.CONFIG;

    @EventHandler
    public void broadcastListener(BroadcastDeathMessageEvent e) {
        if (!e.isCancelled()) {
            if (Messages.getInstance().getConfig().getBoolean("Console.Enabled")) {
                String message = DeathResolver.playerDeathPlaceholders(Messages.getInstance().getConfig().getString("Console.Message"), PlayerManager.getPlayer(e.getPlayer()), e.getLivingEntity());
                Bukkit.getConsoleSender().sendMessage(message.replaceAll("%message%", Matcher.quoteReplacement(e.getTextComponent().toLegacyText())));
            }
            PlayerManager pm = PlayerManager.getPlayer(e.getPlayer());
            if (pm.isInCooldown()) {
                return;
            }
            pm.setCooldown();
            boolean privatePlayer = config.getBoolean(Config.PRIVATE_MESSAGES_PLAYER);
            boolean privateMobs = config.getBoolean(Config.PRIVATE_MESSAGES_MOBS);
            boolean privateNatural = config.getBoolean(Config.PRIVATE_MESSAGES_NATURAL);
            for (World w : e.getBroadcastedWorlds()) {
                if (!config.getStringList(Config.DISABLED_WORLDS).contains(w.getName())) {
                    for (Player pls : w.getPlayers()) {
                        PlayerManager pms = PlayerManager.getPlayer(pls);
                        if (e.getMessageType().equals(MessageType.PLAYER)) {
                            if (privatePlayer && (e.getPlayer().getUniqueId().equals(pms.getUUID()) || e.getLivingEntity().getUniqueId().equals(pms.getUUID()))) {
                                normal(e, pms, pls, e.getBroadcastedWorlds());
                            } else if (!privatePlayer) {
                                normal(e, pms, pls, e.getBroadcastedWorlds());
                            }
                        } else if (e.getMessageType().equals(MessageType.MOB)) {
                            if (privateMobs && e.getPlayer().getUniqueId().equals(pms.getUUID())) {
                                normal(e, pms, pls, e.getBroadcastedWorlds());
                            } else if (!privateMobs) {
                                normal(e, pms, pls, e.getBroadcastedWorlds());
                            }
                        } else if (e.getMessageType().equals(MessageType.NATURAL)) {
                            if (privateNatural && e.getPlayer().getUniqueId().equals(pms.getUUID())) {
                                normal(e, pms, pls, e.getBroadcastedWorlds());
                            } else if (!privateNatural) {
                                normal(e, pms, pls, e.getBroadcastedWorlds());
                            }
                        }
                    }
                }
            }
            PluginMessaging.sendPluginMSG(e.getPlayer(), ComponentSerializer.toString(e.getTextComponent()));
        }
    }

    private void normal(BroadcastDeathMessageEvent e, PlayerManager pms, Player pls, List<World> worlds) {
        if (DeathMessages.worldGuardExtension != null && (DeathMessages.worldGuardExtension.getRegionState(pls, e.getMessageType().getValue()).equals(StateFlag.State.DENY) || DeathMessages.worldGuardExtension.getRegionState(e.getPlayer(), e.getMessageType().getValue()).equals(StateFlag.State.DENY))) {
            return;
        }
        try {
            if (pms.getMessagesEnabled()) {
                pls.spigot().sendMessage(e.getTextComponent());
            }
        } catch (NullPointerException e1) {
            warn(e1);
        }
    }
}
