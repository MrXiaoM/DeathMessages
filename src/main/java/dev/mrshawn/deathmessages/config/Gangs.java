package dev.mrshawn.deathmessages.config;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.utils.CommentedConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class Gangs {
    public final String fileName = "Gangs";
    CommentedConfiguration config;
    File file;
    private static final Gangs instance = new Gangs();

    public static Gangs getInstance() {
        return instance;
    }

    public CommentedConfiguration getConfig() {
        return this.config;
    }

    public void save() {
        try {
            this.config.save(this.file);
        } catch (Exception e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "Gangs.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not save: Gangs.yml");
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
        }
    }

    public void reload() {
        try {
            this.config = CommentedConfiguration.loadConfiguration(this.file);
        } catch (Exception e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "Gangs.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not reload: Gangs.yml");
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
        }
    }

    public void initialize() {
        this.file = new File(DeathMessages.getInstance().getDataFolder(), "Gangs.yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            copy(DeathMessages.getInstance().getResource("Gangs.yml"), this.file);
        }
        this.config = CommentedConfiguration.loadConfiguration(this.file);
        try {
            this.config.syncWithConfig(this.file, DeathMessages.getInstance().getResource("Gangs.yml"), "none");
        } catch (Exception ignored) {
        }
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = Files.newOutputStream(file.toPath());
            byte[] buf = new byte[1024];
            while (true) {
                int len = in.read(buf);
                if (len > 0) {
                    out.write(buf, 0, len);
                } else {
                    out.close();
                    in.close();
                    return;
                }
            }
        } catch (Exception e) {
            warn(e);
        }
    }
}
