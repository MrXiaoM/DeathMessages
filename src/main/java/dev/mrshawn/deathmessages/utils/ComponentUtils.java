package dev.mrshawn.deathmessages.utils;

import de.tr7zw.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ComponentUtils {
    private static boolean is1_21_5;
    private static boolean supportAdventure;
    public static void init() {
        try {
            supportAdventure = Bukkit.getConsoleSender() instanceof Audience;
        } catch (Throwable ignored) {
            supportAdventure = false;
        }
        try {
            is1_21_5 = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4);
        } catch (Throwable ignored) {
            is1_21_5 = false;
        }
    }

    public static boolean is1_21_5() {
        return is1_21_5;
    }

    public static void send(CommandSender sender, BaseComponent... message) {
        if (supportAdventure) {
            // Paper 从 1.16 开始内置 adventure api
            if (AdventureUtils.send(sender, message)) return;
        }

        // 兼容 1.8
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(message);
        }
        try {
            sender.spigot().sendMessage(message);
        } catch (Throwable t) {
            TextComponent text = new TextComponent(message);
            sender.sendMessage(text.toLegacyText());
        }
    }
}
