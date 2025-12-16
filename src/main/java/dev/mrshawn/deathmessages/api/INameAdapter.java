package dev.mrshawn.deathmessages.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface INameAdapter {
    default int priority() {
        return 1000;
    }
    @Nullable String getName(Player player);
}
