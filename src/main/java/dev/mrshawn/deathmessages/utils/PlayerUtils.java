package dev.mrshawn.deathmessages.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public final class PlayerUtils {


    public boolean isPlayer(@NotNull String name) {
        return Bukkit.getPlayer(name) != null;
    }

    @Nullable
    public Player getPlayer(@NotNull String name) {
        return Bukkit.getPlayer(name);
    }
}
