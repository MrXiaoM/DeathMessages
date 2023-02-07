package dev.mrshawn.deathmessages.command.deathmessages;

import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandToggle extends DeathMessagesCommand {
    @Override
    public String command() {
        return "toggle";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Player-Only-Command"));
            return;
        }
        Player p = (Player) sender;
        if (!p.hasPermission(Permission.DEATHMESSAGES_COMMAND_TOGGLE.getValue())) {
            p.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
            return;
        }
        PlayerManager pm = PlayerManager.getPlayer(p);
        boolean b = pm.getMessagesEnabled();
        if (b) {
            pm.setMessagesEnabled(false);
            p.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-Off"));
            return;
        }
        pm.setMessagesEnabled(true);
        p.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-On"));
    }
}
