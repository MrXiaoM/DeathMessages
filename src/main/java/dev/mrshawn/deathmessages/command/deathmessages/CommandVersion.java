package dev.mrshawn.deathmessages.command.deathmessages;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.CommandSender;


public class CommandVersion extends DeathMessagesCommand {
    @Override
    public String command() {
        return "version";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.DEATHMESSAGES_COMMAND_VERSION.getValue())) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
            return;
        }
        String message = Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Version");
        sender.sendMessage(message.replaceAll("%version%", DeathMessages.getInstance().getDescription().getVersion()));
    }
}
