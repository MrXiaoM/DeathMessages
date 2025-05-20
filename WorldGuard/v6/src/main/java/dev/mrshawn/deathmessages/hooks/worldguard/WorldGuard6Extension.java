package dev.mrshawn.deathmessages.hooks.worldguard;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorldGuard6Extension implements WorldGuardExtension {
    public final StateFlag STATE_PLAYER = stateFlag(BROADCAST_PLAYER);
    public final StateFlag STATE_MOBS = stateFlag(BROADCAST_MOBS);
    public final StateFlag STATE_NATURAL = stateFlag(BROADCAST_NATURAL);
    public final StateFlag STATE_ENTITY = stateFlag(BROADCAST_ENTITY);

    private static StateFlag stateFlag(Flag flag) {
        return new StateFlag(flag.name, flag.def);
    }

    private static String handleState(State state) {
        if (state != null) {
            return state.name().toUpperCase();
        }
        return "";
    }

    @Override
    public String getRegionState(Player p, String type) {
        Location loc = p.getLocation();
        RegionContainer rc = WorldGuardPlugin.inst().getRegionContainer();
        ApplicableRegionSet set = rc.createQuery().getApplicableRegions(loc);
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        switch (type) {
            case "player":
                return handleState(set.queryState(lp, STATE_PLAYER));
            case "mob":
                return handleState(set.queryState(lp, STATE_MOBS));
            case "natural":
                return handleState(set.queryState(lp, STATE_NATURAL));
            case "entity":
                return handleState(set.queryState(lp, STATE_ENTITY));
            default:
                return handleState(State.ALLOW);
        }
    }

    @Override
    public boolean isInRegion(Player p, String regionID) {
        Location loc = p.getLocation();
        RegionContainer rc = WorldGuardPlugin.inst().getRegionContainer();
        ApplicableRegionSet set = rc.createQuery().getApplicableRegions(loc);
        Iterator<ProtectedRegion> i = set.iterator();

        ProtectedRegion region;
        do {
            if (!i.hasNext()) {
                return false;
            }

            region = i.next();
        } while (!region.getId().equals(regionID));

        return true;
    }

    @Override
    public void registerFlags(Logger logger) {
        FlagRegistry registry = WorldGuardPlugin.inst().getFlagRegistry();
        try {
            registry.register(STATE_PLAYER);
        } catch (FlagConflictException e) {
            logger.log(Level.WARNING, "", e);
        }
        try {
            registry.register(STATE_MOBS);
        } catch (FlagConflictException e) {
            logger.log(Level.WARNING, "", e);
        }
        try {
            registry.register(STATE_NATURAL);
        } catch (FlagConflictException e) {
            logger.log(Level.WARNING, "", e);
        }
        try {
            registry.register(STATE_ENTITY);
        } catch (FlagConflictException e) {
            logger.log(Level.WARNING, "", e);
        }
    }
}
