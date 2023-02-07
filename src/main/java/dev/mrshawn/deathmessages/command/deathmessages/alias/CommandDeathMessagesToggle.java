package dev.mrshawn.deathmessages.command.deathmessages.alias;

import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.UserData;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class CommandDeathMessagesToggle implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmdLabel, @NotNull String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission(Permission.DEATHMESSAGES_COMMAND.getValue())) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
            return false;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Player-Only-Command"));
            return false;
        } else {
            Player player = (Player) sender;
            if (!player.hasPermission(Permission.DEATHMESSAGES_COMMAND_TOGGLE.getValue())) {
                player.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
                return false;
            }
            PlayerManager pm = PlayerManager.getPlayer(player);
            boolean b = UserData.getInstance().getConfig().getBoolean(player.getUniqueId() + ".messages-enabled");
            if (b) {
                pm.setMessagesEnabled(false);
                player.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-Off"));
                return false;
            }
            pm.setMessagesEnabled(true);
            player.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-On"));
            return false;
        }
    }
}
