package dev.mrshawn.deathmessages.utils.messages;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class Chat {
    @NotNull
    public static final Chat INSTANCE = new Chat();

    private Chat() {
    }

    public void tell(@Nullable CommandSender toWhom, @Nullable String message) {
        if (toWhom != null && message != null) {
            toWhom.sendMessage(colorize(message));
        }
    }

    public void tell(@NotNull CommandSender toWhom, @NotNull String[] messages) {
        Chat $this$tell_u24lambda_u2d0 = this;
        for (String message : messages) {
            $this$tell_u24lambda_u2d0.tell(toWhom, message);
        }
    }

    public void tell(@NotNull CommandSender toWhom, @NotNull ArrayList<String> arrayList) {
        for (String message : arrayList) {
            tell(toWhom, message);
        }
    }

    public void log(@Nullable String message) {
        if (message != null) {
            Bukkit.getConsoleSender().sendMessage(colorize(message));
        }
    }

    public void error(@Nullable String message) {
        if (message != null) {
            log("&4[ERROR] " + message);
        }
    }

    public void broadcast(@Nullable String message) {
        if (message != null) {
            Bukkit.broadcastMessage(colorize(message));
        }
    }

    public void clearChat() {
        for (int i = 0; i < 101; i++) {
            Bukkit.broadcastMessage(" ");
        }
    }

    @NotNull
    public String colorize(@Nullable String message) {
        if (message != null) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return "";
    }
}
