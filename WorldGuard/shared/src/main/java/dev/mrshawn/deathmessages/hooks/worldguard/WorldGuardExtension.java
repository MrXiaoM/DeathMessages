package dev.mrshawn.deathmessages.hooks.worldguard;

import org.bukkit.entity.Player;

import java.util.logging.Logger;

public interface WorldGuardExtension {
    Flag BROADCAST_PLAYER = new Flag("broadcast-deathmessage-player", true);
    Flag BROADCAST_MOBS = new Flag("broadcast-deathmessage-mobs", true);
    Flag BROADCAST_NATURAL = new Flag("broadcast-deathmessage-natural", true);
    Flag BROADCAST_ENTITY = new Flag("broadcast-deathmessage-entity", true);

    /**
     * @return one of <code>""</code>, <code>"ALLOW"</code>, <code>"DENY"</code>
     */
    String getRegionState(Player player, String type);
    boolean isInRegion(Player player, String regionID);
    void registerFlags(Logger logger);
}
