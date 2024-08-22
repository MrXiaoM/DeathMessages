package dev.mrshawn.deathmessages.config;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.utils.FileUtils;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class ConfigManager {
    private static final ConfigManager instance = new ConfigManager();
    public final File backupDirectory = new File(DeathMessages.getInstance().getDataFolder(), "Backups");

    public static ConfigManager getInstance() {
        return instance;
    }

    public void initialize() {
        if (!DeathMessages.getInstance().getDataFolder().exists()) {
            DeathMessages.getInstance().getDataFolder().mkdir();
        }
        EntityDeathMessages.getInstance().initialize();
        Gangs.getInstance().initialize();
        Messages.getInstance().initialize();
        PlayerDeathMessages.getInstance().initialize();
        Settings.getInstance().initialize();
        UserData.getInstance().initialize();
    }

    public void reload() {
        EntityDeathMessages.getInstance().reload();
        Gangs.getInstance().reload();
        Messages.getInstance().reload();
        PlayerDeathMessages.getInstance().reload();
        Settings.getInstance().reload();
    }

    public String backup(boolean excludeUserData) {
        if (!this.backupDirectory.exists()) {
            this.backupDirectory.mkdir();
        }
        String randomCode = RandomStringUtils.randomNumeric(4);
        File backupDir = new File(this.backupDirectory, randomCode);
        backupDir.mkdir();
        try {
            FileUtils.copyFileToDirectory(EntityDeathMessages.getInstance().file, backupDir);
        } catch (IOException e) {
            warn(e);
        }
        try {
            FileUtils.copyFileToDirectory(Gangs.getInstance().file, backupDir);
        } catch (IOException e2) {
            warn(e2);
        }
        try {
            FileUtils.copyFileToDirectory(Messages.getInstance().file, backupDir);
        } catch (IOException e3) {
            warn(e3);
        }
        try {
            FileUtils.copyFileToDirectory(PlayerDeathMessages.getInstance().file, backupDir);
        } catch (IOException e4) {
            warn(e4);
        }
        try {
            FileUtils.copyFileToDirectory(Settings.getInstance().getFile(), backupDir);
        } catch (IOException e5) {
            warn(e5);
        }
        if (!excludeUserData) {
            try {
                FileUtils.copyFileToDirectory(UserData.getInstance().file, backupDir);
            } catch (IOException e6) {
                warn(e6);
            }
        }
        return randomCode;
    }

    public boolean restore(String code, boolean excludeUserData) {
        File backupDir = new File(this.backupDirectory, code);
        if (!backupDir.exists()) {
            return false;
        }
        try {
            Objects.requireNonNull(EntityDeathMessages.getInstance());
            File f = new File(backupDir, "EntityDeathMessages" + ".yml");
            if (EntityDeathMessages.getInstance().file.delete()) {
                FileUtils.copyFileToDirectory(f, DeathMessages.getInstance().getDataFolder());
            } else {
                DeathMessages.getInstance().getLogger().severe("COULD NOT RESTORE " + "EntityDeathMessages" + ".");
            }
        } catch (IOException e) {
            warn(e);
        }
        try {
            Objects.requireNonNull(Gangs.getInstance());
            File f2 = new File(backupDir, "Gangs" + ".yml");
            if (Gangs.getInstance().file.delete()) {
                FileUtils.copyFileToDirectory(f2, DeathMessages.getInstance().getDataFolder());
            } else {
                DeathMessages.getInstance().getLogger().severe("COULD NOT RESTORE " + "Gangs" + ".");
            }
        } catch (IOException e2) {
            warn(e2);
        }
        try {
            Objects.requireNonNull(Messages.getInstance());
            File f3 = new File(backupDir, "Messages" + ".yml");
            if (Messages.getInstance().file.delete()) {
                FileUtils.copyFileToDirectory(f3, DeathMessages.getInstance().getDataFolder());
            } else {
                DeathMessages.getInstance().getLogger().severe("COULD NOT RESTORE " + "Messages" + ".");
            }
        } catch (IOException e3) {
            warn(e3);
        }
        try {
            Objects.requireNonNull(PlayerDeathMessages.getInstance());
            File f4 = new File(backupDir, "PlayerDeathMessages" + ".yml");
            if (PlayerDeathMessages.getInstance().file.delete()) {
                FileUtils.copyFileToDirectory(f4, DeathMessages.getInstance().getDataFolder());
            } else {
                DeathMessages.getInstance().getLogger().severe("COULD NOT RESTORE " + "PlayerDeathMessages" + ".");
            }
        } catch (IOException e4) {
            warn(e4);
        }
        try {
            Objects.requireNonNull(Settings.getInstance());
            File f5 = new File(backupDir, "Settings" + ".yml");
            if (Settings.getInstance().getFile().delete()) {
                FileUtils.copyFileToDirectory(f5, DeathMessages.getInstance().getDataFolder());
            } else {
                DeathMessages.getInstance().getLogger().severe("COULD NOT RESTORE " + "Settings" + ".");
            }
        } catch (IOException e5) {
            warn(e5);
        }
        if (!excludeUserData) {
            try {
                Objects.requireNonNull(UserData.getInstance());
                File f6 = new File(backupDir, "UserData" + ".yml");
                if (UserData.getInstance().file.delete()) {
                    FileUtils.copyFileToDirectory(f6, DeathMessages.getInstance().getDataFolder());
                } else {
                    DeathMessages.getInstance().getLogger().severe("COULD NOT RESTORE " + "UserData" + ".");
                }
            } catch (IOException e6) {
                warn(e6);
            }
        }
        getInstance().reload();
        return true;
    }
}
