package dev.mrshawn.deathmessages.command;

import com.google.common.collect.Lists;
import dev.mrshawn.deathmessages.command.impl.*;
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
import java.util.Arrays;
import java.util.List;


public class CommandManager implements CommandExecutor, TabCompleter {
    private final List<ICommand> commands = Lists.newArrayList(
            new CommandBackup(),
            new CommandBlacklist(),
            new CommandReload(),
            new CommandRestore(),
            new CommandToggle(),
            new CommandVersion()
    );

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmdLabel, @NotNull String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission(Permission.DEATHMESSAGES_COMMAND.getValue())) {
            sender.sendMessage(Messages.formatMessage("Commands.DeathMessages.No-Permission"));
            return false;
        } else if (args.length == 0) {
            for (String s : Messages.formatMessage(Messages.getInstance().getConfig().getStringList("Commands.DeathMessages.Help"))) {
                sender.sendMessage(s);
            }
            return false;
        } else {
            ICommand cmd = get(args[0]);
            if (cmd != null) {
                ArrayList<String> a = new ArrayList<>(Arrays.asList(args));
                a.remove(0);
                cmd.onCommand(sender, a.toArray(new String[0]));
                return false;
            }
            for (String s2 : Messages.formatMessage(Messages.getInstance().getConfig().getStringList("Commands.DeathMessages.Help"))) {
                sender.sendMessage(s2);
            }
            return false;
        }
    }

    private ICommand get(String name) {
        for (ICommand cmd : this.commands) {
            if (cmd.command().equalsIgnoreCase(name)) {
                return cmd;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            String arg0 = args[0].toLowerCase();
            for (ICommand cmd : this.commands) {
                if (cmd.command().startsWith(arg0))
                    list.add(cmd.command());
            }
        }
        return list;
    }
}
