package dev.mrshawn.deathmessages.api;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class LibsDisguisesNameAdapter implements INameAdapter {
    @Nullable
    @Override
    public String getName(Player player) {
        Disguise d = DisguiseAPI.getDisguise(player);
        if (d != null) {
            Object customName = d.getCustomData("CustomName");
            if (customName != null) {
                return String.valueOf(customName);
            }
            if (d instanceof PlayerDisguise) {
                PlayerDisguise disguise = (PlayerDisguise) d;
                return disguise.getName();
            }
            if (d instanceof MobDisguise) {
                MobDisguise disguise = (MobDisguise) d;
                return disguise.getEntity().getCustomName();
            }
        }
        return null;
    }
}
