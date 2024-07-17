package dev.mrshawn.deathmessages.command.impl;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.command.ICommand;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.enums.Permission;
import org.bukkit.command.CommandSender;


public class CommandVersion implements ICommand {
    @Override
    public String command() {
        return "version";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.DEATHMESSAGES_COMMAND_VERSION.getValue())) {
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.No-Permission"));
            return;
        }
        String message = Messages.formatMessage("Commands.DeathMessages.Sub-Commands.Version");
        sender.sendMessage(message.replaceAll("%version%", DeathMessages.getInstance().getDescription().getVersion()));
    }
}
