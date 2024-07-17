package dev.mrshawn.deathmessages.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;

import java.util.Iterator;

public final class WorldGuard7Extension implements WorldGuardExtension {
    public WorldGuard7Extension() {
    }

    public StateFlag.State getRegionState(Player p, String type) {
        Location loc = new Location(BukkitAdapter.adapt(p.getWorld()), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet set = rc.createQuery().getApplicableRegions(loc);
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        switch (type) {
            case "player":
                return set.queryState(lp, BROADCAST_PLAYER);
            case "mob":
                return set.queryState(lp, BROADCAST_MOBS);
            case "natural":
                return set.queryState(lp, BROADCAST_NATURAL);
            case "entity":
                return set.queryState(lp, BROADCAST_ENTITY);
            default:
                return State.ALLOW;
        }
    }

    public boolean isInRegion(Player p, String regionID) {
        Location loc = new Location(BukkitAdapter.adapt(p.getWorld()), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
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
}
