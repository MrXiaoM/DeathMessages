package dev.mrshawn.deathmessages.hooks;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class MythicMobs5API implements IMythicMobsAPI {
    MythicBukkit mythicMobs;
    public MythicMobs5API() {
        mythicMobs = MythicBukkit.inst();
    }
    @Override
    public boolean isMythicMob(UUID uuid) {
        return mythicMobs.getAPIHelper().isMythicMob(uuid);
    }

    @Override
    public String getMobType(Entity mob) {
        return mythicMobs.getAPIHelper().getMythicMobInstance(mob).getMobType();
    }
}
