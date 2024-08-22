package dev.mrshawn.deathmessages;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.command.CommandManager;
import dev.mrshawn.deathmessages.command.impl.CommandToggle;
import dev.mrshawn.deathmessages.config.ConfigManager;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.hooks.IMythicMobsAPI;
import dev.mrshawn.deathmessages.hooks.MythicMobs4API;
import dev.mrshawn.deathmessages.hooks.MythicMobs5API;
import dev.mrshawn.deathmessages.hooks.PlaceholderAPIExtension;
import dev.mrshawn.deathmessages.listeners.*;
import dev.mrshawn.deathmessages.listeners.api.BlockExplosionListener;
import dev.mrshawn.deathmessages.listeners.api.BroadcastEntityDeathListener;
import dev.mrshawn.deathmessages.listeners.api.BroadcastPlayerDeathListener;
import dev.mrshawn.deathmessages.listeners.mythicmobs.MobDeath4;
import dev.mrshawn.deathmessages.listeners.mythicmobs.MobDeath5;
import dev.mrshawn.deathmessages.hooks.WorldGuard7Extension;
import dev.mrshawn.deathmessages.hooks.WorldGuardExtension;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;


public class DeathMessages extends JavaPlugin {
    private static DeathMessages instance;
    public boolean placeholderAPIEnabled = false;
    public IMythicMobsAPI mythicMobs = null;
    public boolean mythicmobsEnabled = false;
    public static boolean langUtilsEnabled = false;
    public static boolean useTranslateComponent = true;
    public static String bungeeServerName;
    public static WorldGuardExtension worldGuardExtension;
    public static boolean worldGuardEnabled;
    private static FileSettings<Config> config;
    public static boolean bungeeServerNameRequest = true;
    public static boolean bungeeInit = false;
    private static EventPriority eventPriority = EventPriority.HIGH;
    private static int savedVersion = 0;

    public static void warn(Throwable t) {
        instance.getLogger().log(Level.WARNING, "", t);
    }

    public void onEnable() {
        instance = this;
        initializeConfigs();
        initializeHooksOnLoad();

        initializeListeners();
        initializeCommands();
        initializeHooks();
        initializeOnlinePlayers();
        checkGameRules();
        getLogger().info("Plugin Enabled!");
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
        instance = null;
    }

    public static int majorVersion() {
        if (savedVersion <= 0) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String v = name.substring(name.lastIndexOf('.') + 1)
                    .replace("1_", "")
                    .replaceAll("_R\\d", "")
                    .replaceAll("v", "");
            try {
                savedVersion = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                savedVersion = 20;
            }
        }
        return savedVersion;
    }

    private void initializeConfigs() {
        ConfigManager.getInstance().initialize();

        config = FileSettings.CONFIG;
        eventPriority = EventPriority.valueOf(config.getString(Config.DEATH_LISTENER_PRIORITY).toUpperCase());
    }

    private void initializeListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        for (Listener listener : new Listener[] {
                new BroadcastPlayerDeathListener(),
                new BroadcastEntityDeathListener(),
                new BlockExplosionListener(),
                new EntityDamage(),
                new EntityDamageByBlock(),
                new EntityDamageByEntity(),
                new EntityDeath(),
                new InteractEvent(),
                new OnJoin(),
                new OnMove(),
                new OnQuit(),
                new PlayerDeath()
        }) pluginManager.registerEvents(listener, this);
    }

    private void initializeCommands() {
        CommandManager manager = new CommandManager();
        setupCommand("deathmessages", manager);
        setupCommand("deathmessagestoggle", new CommandToggle.Alias());
    }

    private void setupCommand(String label, Object command) {
        PluginCommand cmd = getCommand(label);
        if (cmd != null) {
            if (command instanceof CommandExecutor) cmd.setExecutor((CommandExecutor) command);
            if (command instanceof TabCompleter) cmd.setTabCompleter((TabCompleter) command);
        }
    }

    private void initializeHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIExtension(this).register();
            this.placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI Hook Enabled!");
        }
        if (worldGuardEnabled) {
            getLogger().info("WorldGuard Hook Enabled!");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlugMan") && worldGuardExtension != null) {
            Plugin plugMan = Bukkit.getPluginManager().getPlugin("PlugMan");
            if (plugMan != null) {
                getLogger().info("PlugMan found. Adding this plugin to its ignored plugins list due to WorldGuard hook being enabled!");
                try {
                    @SuppressWarnings({"unchecked"})
                    List<String> ignoredPlugins = (List<String>) plugMan.getClass().getMethod("getIgnoredPlugins").invoke(plugMan, new Object[0]);
                    if (!ignoredPlugins.contains("DeathMessages")) {
                        ignoredPlugins.add("DeathMessages");
                    }
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
                    getLogger().severe("Error adding plugin to ignored plugins list: " + exception.getMessage());
                }
            }
        }
        Plugin mythicPlugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (mythicPlugin != null && config.getBoolean(Config.HOOKS_MYTHICMOBS_ENABLED)) {
            String ver = mythicPlugin.getDescription().getVersion();
            if (ver.startsWith("4.")) {
                this.mythicMobs = new MythicMobs4API();
                this.mythicmobsEnabled = true;
                getLogger().info("MythicMobs 4.x Hook Enabled!");
                Bukkit.getPluginManager().registerEvents(new MobDeath4(), this);
            }
            if (ver.startsWith("5.")) {
                this.mythicMobs = new MythicMobs5API();
                this.mythicmobsEnabled = true;
                getLogger().info("MythicMobs 5.x Hook Enabled!");
                Bukkit.getPluginManager().registerEvents(new MobDeath5(), this);
            }
            if (!mythicmobsEnabled) {
                getLogger().warning("Unknown MythicMobs version " + ver);
            }
        }
        useTranslateComponent = isPresent("org.bukkit.Translatable");
        if (Bukkit.getPluginManager().getPlugin("LangUtils") != null && config.getBoolean(Config.HOOKS_LANGUTILS_ENABLED)) {
            langUtilsEnabled = true;
            getLogger().info("LangUtils Hook Enabled!");
        }
        if (config.getBoolean(Config.HOOKS_BUNGEE_ENABLED)) {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new PluginMessaging());
            getLogger().info("Bungee Hook enabled!");
            if (config.getBoolean(Config.HOOKS_BUNGEE_SERVER_NAME_GET_FROM_BUNGEE)) {
                bungeeInit = true;
                return;
            }
            bungeeInit = false;
            bungeeServerName = config.getString(Config.HOOKS_BUNGEE_SERVER_NAME_DISPLAY_NAME);
        }
    }

    private void initializeHooksOnLoad() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && config.getBoolean(Config.HOOKS_WORLDGUARD_ENABLED)) {
            try {
                WorldGuardPlugin worldGuardPlugin = WorldGuardPlugin.inst();
                if (worldGuardPlugin == null) {
                    throw new Exception();
                }
                String version = worldGuardPlugin.getDescription().getVersion();
                if (version.startsWith("7")) {
                    worldGuardExtension = new WorldGuard7Extension();
                    worldGuardExtension.registerFlags();
                } else if (version.startsWith("6")) {
                    worldGuardExtension.registerFlags();
                } else {
                    throw new Exception();
                }
                worldGuardEnabled = true;
            } catch (Throwable e) {
                getLogger().severe("Error loading WorldGuardHook. Error: " + e.getMessage());
                worldGuardEnabled = false;
            }
        }
    }

    private void initializeOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(PlayerManager::new);
    }

    private void checkGameRules() {
        if (config.getBoolean(Config.DISABLE_DEFAULT_MESSAGES)) {
            GameRule<Boolean> ruleToDisable;
            try {
                ruleToDisable = GameRule.SHOW_DEATH_MESSAGES;
            } catch (Throwable ignored) {
                return;
            }
            for (World world : Bukkit.getWorlds()) {
                Boolean rule = world.getGameRuleValue(ruleToDisable);
                if (rule == null || rule.equals(true)) {
                    world.setGameRule(ruleToDisable, false);
                }
            }
        }
    }

    public static EventPriority getEventPriority() {
        return eventPriority;
    }

    public static DeathMessages getInstance() {
        return instance;
    }

    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
