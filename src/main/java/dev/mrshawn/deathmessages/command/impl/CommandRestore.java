package dev.mrshawn.deathmessages.command.impl;

import dev.mrshawn.deathmessages.command.DeathMessagesCommand;
import dev.mrshawn.deathmessages.config.ConfigManager;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.CommandSender;


public class CommandRestore extends DeathMessagesCommand {
    @Override
    public String command() {
        return "restore";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.DEATHMESSAGES_COMMAND_RESTORE.getValue())) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
        } else if (args.length <= 1) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Restore.Usage"));
        } else {
            String code = args[0];
            boolean excludeUserData = Boolean.parseBoolean(args[1]);
            if (ConfigManager.getInstance().restore(code, excludeUserData)) {
                sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Restore.Restored").replaceAll("%backup-code%", code));
            } else {
                sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Restore.Backup-Not-Found"));
            }
        }
    }
}
