package dev.mrshawn.deathmessages.config;

import dev.mrshawn.deathmessages.DeathMessages;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class UserData {
    public final String fileName = "UserData";
    FileConfiguration config;
    File file;
    private static final UserData instance = new UserData();

    public static UserData getInstance() {
        return instance;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void save() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "UserData.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not save: UserData.yml");
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
        }
    }

    public void reload() {
        try {
            this.config.load(this.file);
        } catch (Exception e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "UserData.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not reload: UserData.yml");
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
        }
    }

    public void initialize() {
        this.file = new File(DeathMessages.getInstance().getDataFolder(), "UserData.yml");
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                warn(e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        save();
        reload();
    }
}
