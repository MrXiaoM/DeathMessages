package dev.mrshawn.deathmessages.listeners;

import dev.mrshawn.deathmessages.config.EntityDeathMessages;
import dev.mrshawn.deathmessages.config.PlayerDeathMessages;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;


public class OnChat implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (Assets.addingMessage.containsKey(p.getName())) {
            e.setCancelled(true);
            String args = Assets.addingMessage.get(p.getName());
            Assets.addingMessage.remove(p.getName());
            String[] spl = args.split(":");
            if (spl[0].equalsIgnoreCase("Gang") || spl[0].equalsIgnoreCase("Solo")) {
                String mode = spl[0];
                String mobName = spl[1];
                String damageType = spl[2];
                List<String> list = PlayerDeathMessages.getInstance().getConfig().getStringList("Mobs." + mobName + "." + mode + "." + damageType);
                list.add(e.getMessage());
                PlayerDeathMessages.getInstance().getConfig().set("Mobs." + mobName + "." + mode + "." + damageType, list);
                PlayerDeathMessages.getInstance().save();
                PlayerDeathMessages.getInstance().reload();
                p.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Added-Message").replaceAll("%message%", e.getMessage()).replaceAll("%mob_name%", mobName).replaceAll("%mode%", mode).replaceAll("%damage_type%", damageType));
                return;
            }
            String mobName2 = spl[0];
            String damageType2 = spl[1];
            List<String> list2 = EntityDeathMessages.getInstance().getConfig().getStringList("Entities." + mobName2 + "." + damageType2);
            list2.add(e.getMessage());
            EntityDeathMessages.getInstance().getConfig().set("Entities." + mobName2 + "." + damageType2, list2);
            EntityDeathMessages.getInstance().save();
            EntityDeathMessages.getInstance().reload();
            p.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Added-Message").replaceAll("%message%", e.getMessage()).replaceAll("%mob_name%", mobName2).replaceAll("%damage_type%", damageType2));
        }
    }
}
