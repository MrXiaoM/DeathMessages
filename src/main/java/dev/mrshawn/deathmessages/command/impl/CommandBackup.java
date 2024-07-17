package dev.mrshawn.deathmessages.command.impl;

import dev.mrshawn.deathmessages.command.ICommand;
import dev.mrshawn.deathmessages.config.ConfigManager;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.enums.Permission;
import org.bukkit.command.CommandSender;


public class CommandBackup implements ICommand {
    @Override
    public String command() {
        return "backup";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.DEATHMESSAGES_COMMAND_BACKUP.getValue())) {
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.No-Permission"));
        } else if (args.length == 0) {
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.Sub-Commands.Backup.Usage"));
        } else {
            boolean b = Boolean.parseBoolean(args[0]);
            String code = ConfigManager.getInstance().backup(b);
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.Sub-Commands.Backup.Backed-Up").replaceAll("%backup-code%", code));
        }
    }
}
