package dev.mrshawn.deathmessages.command.deathmessages;

import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CommandManager implements CommandExecutor {
    private final List<DeathMessagesCommand> commands = new ArrayList<>();

    public void initializeSubCommands() {
        this.commands.add(new CommandBackup());
        this.commands.add(new CommandBlacklist());
        this.commands.add(new CommandReload());
        this.commands.add(new CommandRestore());
        this.commands.add(new CommandToggle());
        this.commands.add(new CommandVersion());
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmdLabel, @NotNull String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission(Permission.DEATHMESSAGES_COMMAND.getValue())) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
            return false;
        } else if (args.length == 0) {
            for (String s : Assets.formatMessage(Messages.getInstance().getConfig().getStringList("Commands.DeathMessages.Help"))) {
                sender.sendMessage(s);
            }
            return false;
        } else {
            DeathMessagesCommand cmd = get(args[0]);
            if (cmd != null) {
                ArrayList<String> a = new ArrayList<>(Arrays.asList(args));
                a.remove(0);
                cmd.onCommand(sender, a.toArray(new String[0]));
                return false;
            }
            for (String s2 : Assets.formatMessage(Messages.getInstance().getConfig().getStringList("Commands.DeathMessages.Help"))) {
                sender.sendMessage(s2);
            }
            return false;
        }
    }

    private DeathMessagesCommand get(String name) {
        for (DeathMessagesCommand cmd : this.commands) {
            if (cmd.command().equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }
}
