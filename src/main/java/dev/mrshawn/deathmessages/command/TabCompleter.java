package dev.mrshawn.deathmessages.command;

import dev.mrshawn.deathmessages.enums.DamageTypes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        EntityType[] values;
        if (!args[0].equalsIgnoreCase("edit")) {
            return null;
        }
        if (args.length == 2) {
            List<String> arguments = new ArrayList<>();
            arguments.add("player");
            arguments.add("entity");
            return arguments;
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("entity")) {
                List<String> mobNames = new ArrayList<>();
                for (EntityType entityType : EntityType.values()) {
                    Class<?> clazz = entityType.getEntityClass();
                    if (clazz != null && entityType.isAlive()) {
                        mobNames.add(clazz.getSimpleName().toLowerCase());
                    }
                }
                return mobNames;
            }
            return null;
        } else if (args.length == 4) {
            if (args[1].equalsIgnoreCase("player")) {
                List<String> arguments2 = new ArrayList<>();
                arguments2.add("solo");
                arguments2.add("gang");
                return arguments2;
            } else if (args[1].equalsIgnoreCase("entity")) {
                return DamageTypes.getFriendlyNames();
            } else {
                return null;
            }
        } else if (args.length == 5) {
            if (args[1].equalsIgnoreCase("player")) {
                return DamageTypes.getFriendlyNames();
            }
            if (args[1].equalsIgnoreCase("entity")) {
                List<String> arguments3 = new ArrayList<>();
                arguments3.add("add");
                arguments3.add("remove");
                arguments3.add("list");
                return arguments3;
            }
            return null;
        } else if (args.length == 6 && args[1].equalsIgnoreCase("player")) {
            List<String> arguments4 = new ArrayList<>();
            arguments4.add("add");
            arguments4.add("remove");
            arguments4.add("list");
            return arguments4;
        } else {
            return null;
        }
    }
}
