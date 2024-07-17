package dev.mrshawn.deathmessages.command.impl;

import dev.mrshawn.deathmessages.command.ICommand;
import dev.mrshawn.deathmessages.config.ConfigManager;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.enums.Permission;
import org.bukkit.command.CommandSender;


public class CommandReload implements ICommand {
    @Override
    public String command() {
        return "reload";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.DEATHMESSAGES_COMMAND_RELOAD.getValue())) {
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.No-Permission"));
            return;
        }
        ConfigManager.getInstance().reload();
        sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.Sub-Commands.Reload.Reloaded"));
    }
}
