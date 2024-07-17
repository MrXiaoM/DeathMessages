package dev.mrshawn.deathmessages.command;

import org.bukkit.command.CommandSender;


public interface ICommand {
    String command();

    void onCommand(CommandSender sender, String[] args);
}
