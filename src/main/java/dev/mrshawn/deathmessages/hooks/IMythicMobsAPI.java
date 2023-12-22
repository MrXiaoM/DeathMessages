package dev.mrshawn.deathmessages.hooks;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface IMythicMobsAPI {
    boolean isMythicMob(UUID uuid);
    String getMobType(Entity mob);
}
