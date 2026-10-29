package dev.mrshawn.deathmessages.config;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.utils.CommentedConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class PlayerDeathMessages {
    public final String fileName = "PlayerDeathMessages.yml";
    private final Map<String, CommentedConfiguration> byWorlds = new HashMap<>();
    CommentedConfiguration config;
    File file;
    private static final PlayerDeathMessages instance = new PlayerDeathMessages();

    public static PlayerDeathMessages getInstance() {
        return instance;
    }

    @Deprecated
    public CommentedConfiguration getConfig() {
        return config;
    }

    public CommentedConfiguration getConfig(String worldName) {
        if (worldName == null) {
            return config;
        }
        return byWorlds.getOrDefault(worldName, config);
    }

    public void save() {
        try {
            this.config.save(this.file);
        } catch (Exception e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "PlayerDeathMessages.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not save: " + fileName);
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
        }
    }

    public void reload() {
        try {
            this.load();
        } catch (Exception e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "PlayerDeathMessages.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not save: " + fileName);
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
            warn(e);
        }
    }

    public void initialize() {
        this.file = new File(DeathMessages.getInstance().getDataFolder(), fileName);
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            copy(DeathMessages.getInstance().getResource(fileName), this.file);
        }
        this.load();
        try {
            DeathMessages plugin = DeathMessages.getInstance();
            File worldsFolder = new File(plugin.getDataFolder(), "worlds");
            CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(plugin.getResource(fileName));
            this.config.syncWithConfig(this.file, cfg, "Entities", "Mythic-Mobs-Entities");
            for (Map.Entry<String, CommentedConfiguration> entry : byWorlds.entrySet()) {
                String world = entry.getKey();
                CommentedConfiguration config = entry.getValue();
                File file = new File(worldsFolder, world + "/" + fileName);
                config.syncWithConfig(file, cfg, "Entities", "Mythic-Mobs-Entities");
            }
        } catch (Exception ignored) {
        }
    }

    private void load() {
        DeathMessages plugin = DeathMessages.getInstance();
        File worldsFolder = new File(plugin.getDataFolder(), "worlds");
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs();
        }
        this.config = CommentedConfiguration.loadConfiguration(this.file);
        this.byWorlds.clear();
        File[] files = worldsFolder.listFiles();
        if (files != null) for (File worldFolder : files) {
            File file = new File(worldFolder, fileName);
            if (file.exists()) {
                this.byWorlds.put(worldFolder.getName(), CommentedConfiguration.loadConfiguration(file));
            }
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
