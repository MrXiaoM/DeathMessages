package dev.mrshawn.deathmessages.files;

import dev.mrshawn.deathmessages.DeathMessages;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class FileSettings<C extends Enum<C>> {
    public static FileSettings<Config> CONFIG = new FileSettings<Config>(
            DeathMessages.getInstance(),
            "Settings.yml"
    ).loadSettings(Config.class);

    private final JavaPlugin plugin;
    private final String fileName;
    private final File file;
    private YamlConfiguration yamlConfig;
    private final Map<Enum<C>, Object> values = new HashMap<>();

    public FileSettings(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
        loadFile();
    }

    private void loadFile() {
        if (!this.file.exists()) {
            this.plugin.saveResource(this.fileName, false);
        }
    }

    public void save() {
        try {
            this.yamlConfig.save(this.file);
        } catch (IOException e) {
            warn(e);
        }
    }

    public FileSettings<C> loadSettings(Class<C> enumClass) {
        this.yamlConfig = YamlConfiguration.loadConfiguration(this.file);
        EnumSet<C> enumSet = EnumSet.allOf(enumClass);
        for (C c : enumSet) {
            if (!(c instanceof ConfigEnum)) {
                throw new IllegalArgumentException("Enum " + enumClass.getName() + " must implement ConfigEnum");
            }
            ConfigEnum configEnum = (ConfigEnum) c;
            String configPath = configEnum.getPath();
            if (this.yamlConfig.contains(configPath)) {
                this.values.put(c, this.yamlConfig.get(configPath));
            } else {
                Object defaultValue = configEnum.getDefault();
                if (defaultValue != null) {
                    this.yamlConfig.set(configPath, defaultValue);
                    this.values.put(c, defaultValue);
                }
            }
        }
        return this;
    }

    public boolean getBoolean(Enum<?> value) {
        return get(value, Boolean.class);
    }

    public String getString(Enum<?> value) {
        return get(value, String.class);
    }

    public int getInt(Enum<?> value) {
        return get(value, Integer.class);
    }

    public long getLong(Enum<?> value) {
        return get(value, Long.class);
    }

    public List<String> getStringList(Enum<?> value) {
        List<String> tempList = new ArrayList<>();
        for (Object val : get(value, List.class)) {
            tempList.add((String) val);
        }
        return tempList;
    }

    public <T> T get(Enum<?> value, Class<T> clazz) {
        return clazz.cast(this.values.get(value));
    }

    public void set(Enum<C> enumValue, ConfigEnum configEnum, Object setValue) {
        this.values.put(enumValue, setValue);
        this.yamlConfig.set(configEnum.getPath(), setValue);
    }
}
