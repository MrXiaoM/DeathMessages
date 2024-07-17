package dev.mrshawn.deathmessages.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public final class CommentedConfiguration extends YamlConfiguration {
    private final Map<String, String> configComments = new HashMap<>();
    private boolean creationFailure = false;

    public void syncWithConfig(File file, InputStream resource, String... ignoredSections) throws IOException {
        if (this.creationFailure) {
            return;
        }
        CommentedConfiguration cfg = loadConfiguration(resource);
        ConfigurationSection section = cfg.getConfigurationSection("");
        if (section != null && syncConfigurationSection(section, Arrays.asList(ignoredSections)) && file != null) {
            save(file);
        }
    }

    public void setComment(String path, String comment) {
        if (comment == null) {
            this.configComments.remove(path);
        } else {
            Set<String> set = Sets.newHashSet(comment.split("\n"));
            this.configComments.put(path, String.join("\n", set));
        }
    }

    public String getComment(String path) {
        return getComment(path, null);
    }

    public String getComment(String path, String def) {
        return this.configComments.getOrDefault(path, def);
    }

    public boolean containsComment(String path) {
        return getComment(path) != null;
    }

    public boolean hasFailed() {
        return this.creationFailure;
    }

    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        super.loadFromString(contents);
        String[] lines = contents.split("\n");
        StringBuilder comments = new StringBuilder();
        String currentSection = "";
        for (String line : lines) {
            if (isComment(line)) {
                comments.append(line).append("\n");
            } else if (isNewSection(line)) {
                currentSection = getSectionPath(this, line, currentSection);
                if (comments.length() > 1) {
                    setComment(currentSection, comments.substring(0, comments.length() - 1));
                }
                comments = new StringBuilder();
            }
        }
    }

    @NotNull
    public String saveToString() {
        List<String> lines = Lists.newArrayList(super.saveToString().split("\n"));
        int currentIndex = 0;
        String currentSection = "";
        while (currentIndex < lines.size()) {
            String line = lines.get(currentIndex);
            if (isNewSection(line)) {
                currentSection = getSectionPath(this, line, currentSection);
                if (containsComment(currentSection)) {
                    lines.add(currentIndex, getComment(currentSection));
                    currentIndex++;
                }
            }
            currentIndex++;
        }
        StringBuilder contents = new StringBuilder();
        for (String line2 : lines) {
            contents.append("\n").append(line2);
        }
        return contents.length() == 0 ? "" : contents.substring(1);
    }

    private boolean syncConfigurationSection(ConfigurationSection section, List<String> ignoredSections) {
        boolean changed = false;
        for (String key : section.getKeys(false)) {
            String current = section.getCurrentPath();
            if (current == null) current = "";
            String path = current.isEmpty() ? key : (current + "." + key);
            if (section.isConfigurationSection(key)) {
                Objects.requireNonNull(path);
                boolean isIgnored = ignoredSections.stream().anyMatch(path::contains);
                boolean containsSection = contains(path);
                ConfigurationSection section1 = section.getConfigurationSection(key);
                if (section1 != null && (!containsSection || !isIgnored)) {
                    changed = syncConfigurationSection(section1, ignoredSections) || changed;
                }
            } else if (!contains(path)) {
                set(path, section.get(key));
                changed = true;
            }
        }
        String current = section.getCurrentPath();
        if (current != null && changed) {
            correctIndexes(section, getConfigurationSection(current));
        }
        return changed;
    }

    private CommentedConfiguration flagAsFailed() {
        this.creationFailure = true;
        return this;
    }

    @NotNull
    public static CommentedConfiguration loadConfiguration(@NotNull File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            return loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().warning("File " + file.getName() + " doesn't exist.");
            return new CommentedConfiguration().flagAsFailed();
        }
    }

    public static CommentedConfiguration loadConfiguration(InputStream inputStream) {
        return loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @NotNull
    public static CommentedConfiguration loadConfiguration(@NotNull Reader reader) {
        CommentedConfiguration config = new CommentedConfiguration();
        try (BufferedReader bufferedReader = reader instanceof BufferedReader
                ? (BufferedReader) reader
                : new BufferedReader(reader)) {
            StringBuilder contents = new StringBuilder();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) break;
                contents.append(line).append('\n');
            }
            config.loadFromString(contents.toString());
        } catch (IOException | InvalidConfigurationException ex) {
            config.flagAsFailed();
            warn(ex);
        }
        return config;
    }

    private static boolean isNewSection(String line) {
        String trimLine = line.trim();
        return trimLine.contains(": ") || trimLine.endsWith(":");
    }

    private static String getSectionPath(CommentedConfiguration commentedConfig, String line, String currentSection) {
        String newSection;
        String newSection2 = line.trim().split(": ")[0];
        if (newSection2.endsWith(":")) {
            newSection2 = newSection2.substring(0, newSection2.length() - 1);
        }
        if (newSection2.startsWith(".")) {
            newSection2 = newSection2.substring(1);
        }
        if (newSection2.startsWith("'") && newSection2.endsWith("'")) {
            newSection2 = newSection2.substring(1, newSection2.length() - 1);
        }
        if (!currentSection.isEmpty() && commentedConfig.contains(currentSection + "." + newSection2)) {
            newSection = currentSection + "." + newSection2;
        } else {
            String parentSection = currentSection;
            while (!parentSection.isEmpty()) {
                String parentPath = getParentPath(parentSection);
                parentSection = parentPath;
                if (commentedConfig.contains(parentPath + "." + newSection2)) {
                    break;
                }
            }
            newSection = parentSection.trim().isEmpty() ? newSection2 : parentSection + "." + newSection2;
        }
        return newSection;
    }

    private static boolean isComment(String line) {
        String trimLine = line.trim();
        return trimLine.startsWith("#") || trimLine.isEmpty();
    }

    private static String getParentPath(String path) {
        return path.contains(".") ? path.substring(0, path.lastIndexOf(46)) : "";
    }

    private static void correctIndexes(ConfigurationSection section, ConfigurationSection target) {
        List<Pair<String, Object>> sectionMap = getSectionMap(section);
        List<Pair<String, Object>> correctOrder = new ArrayList<>();
        for (Pair<String, Object> entry : sectionMap) {
            correctOrder.add(new Pair<>(entry.key, target.get(entry.key)));
        }
        clearConfiguration(target);
        for (Pair<String, Object> entry2 : correctOrder) {
            target.set((entry2).key, (entry2).value);
        }
    }

    private static List<Pair<String, Object>> getSectionMap(ConfigurationSection section) {
        List<Pair<String, Object>> list = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            list.add(new Pair<>(key, section.get(key)));
        }
        return list;
    }

    private static void clearConfiguration(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            section.set(key, null);
        }
    }


    public static class Pair<K, V> {
        private final K key;
        private final V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @SuppressWarnings({"rawtypes"})
        public boolean equals(Object obj) {
            return (obj instanceof Pair) && this.key.equals(((Pair) obj).key) && this.value.equals(((Pair) obj).value);
        }

        public int hashCode() {
            return this.key.hashCode();
        }
    }
}
