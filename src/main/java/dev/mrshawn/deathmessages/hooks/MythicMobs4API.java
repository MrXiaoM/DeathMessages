package dev.mrshawn.deathmessages.hooks;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class MythicMobs4API implements IMythicMobsAPI {
    MythicMobs mythicMobs;
    public MythicMobs4API() {
        mythicMobs = MythicMobs.inst();
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
