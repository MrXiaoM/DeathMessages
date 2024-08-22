package dev.mrshawn.deathmessages.command.impl;

import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.command.ICommand;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.enums.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class CommandToggle implements ICommand {
    @Override
    public String command() {
        return "toggle";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        toggle(sender);
    }

    private static void toggle(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.Player-Only-Command"));
            return;
        }
        Player p = (Player) sender;
        if (!p.hasPermission(Permission.DEATHMESSAGES_COMMAND_TOGGLE.getValue())) {
            p.sendMessage(Messages.formatMessage("Commands.DeathMessages.No-Permission"));
            return;
        }
        PlayerManager pm = PlayerManager.getPlayer(p);
        boolean b = pm.getMessagesEnabled();
        if (b) {
            pm.setMessagesEnabled(false);
            p.sendMessage(Messages.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-Off"));
            return;
        }
        pm.setMessagesEnabled(true);
        p.sendMessage(Messages.formatMessage("Commands.DeathMessages.Sub-Commands.Toggle.Toggle-On"));
    }

    public static class Alias implements CommandExecutor, TabCompleter {
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmdLabel, @NotNull String[] args) {
            if ((sender instanceof Player) && !sender.hasPermission(Permission.DEATHMESSAGES_COMMAND.getValue())) {
                sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.No-Permission"));
                return true;
            }
            toggle(sender);
            return true;
        }

        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return new ArrayList<>();
        }
    }
}
