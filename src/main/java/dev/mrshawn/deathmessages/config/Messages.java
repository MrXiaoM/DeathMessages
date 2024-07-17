package dev.mrshawn.deathmessages.config;

import com.meowj.langutils.lang.LanguageHelper;
import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.utils.CommentedConfiguration;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class Messages {
    CommentedConfiguration config;
    File file;
    private static final Messages instance = new Messages();

    public static Messages getInstance() {
        return instance;
    }

    public CommentedConfiguration getConfig() {
        return this.config;
    }

    private static CommentedConfiguration config() {
        return getInstance().getConfig();
    }
    
    public void save() {
        try {
            this.config.save(this.file);
        } catch (Exception e) {
            File f = new File(DeathMessages.getInstance().getDataFolder(), "Messages.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not save: Messages.yml");
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
            File f = new File(DeathMessages.getInstance().getDataFolder(), "Messages.broken." + new Date().getTime());
            DeathMessages.getInstance().getLogger().severe("Could not reload: Messages.yml");
            DeathMessages.getInstance().getLogger().severe("Regenerating file and renaming the current file to: " + f.getName());
            DeathMessages.getInstance().getLogger().severe("You can try fixing the file with a yaml parser online!");
            this.file.renameTo(f);
            initialize();
        }
    }

    public void initialize() {
        this.file = new File(DeathMessages.getInstance().getDataFolder(), "Messages.yml");
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            copy(DeathMessages.getInstance().getResource("Messages.yml"), this.file);
        }
        this.config = CommentedConfiguration.loadConfiguration(this.file);
        try {
            this.config.syncWithConfig(this.file, DeathMessages.getInstance().getResource("Messages.yml"), "none");
        } catch (Exception e) {
            warn(e);
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

    public static String getBlockName(String splitMessage, Material mat, Player player) {
        if (mat == null) return null;
        String configValue2;
        if (DeathMessages.langUtilsEnabled) {
            configValue2 = LanguageHelper.getItemName(new ItemStack(mat), player);
        } else {
            String material2 = mat.toString().toLowerCase();
            configValue2 = config().getString("Blocks." + material2);
        }
        return colorize(splitMessage.replace("%climbable%", configValue2 + (splitMessage.endsWith(".") ? "" : " ")));
    }

    @Nullable
    @SuppressWarnings({"deprecation"})
    public static String getBlockName(String splitMessage, FallingBlock block, Player player) {
        if (block == null) return null;
        Material mat;
        if (DeathMessages.majorVersion() < 13) {
            mat = block.getMaterial();
        } else {
            mat = block.getBlockData().getMaterial();
        }
        return getBlockName(splitMessage, mat, player);
    }

    public static String convertString(String string) {
        String[] spl = string.replace("_", " ").toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spl.length; i++) {
            if (i == spl.length - 1) {
                sb.append(StringUtils.capitalize(spl[i]));
            } else {
                sb.append(StringUtils.capitalize(spl[i])).append(" ");
            }
        }
        return sb.toString();
    }

    public static String formatting(BaseComponent tx) {
        String returning;
        returning = "";
        returning = tx.isBold() ? returning + "&l" : "";
        if (tx.isItalic()) {
            returning = returning + "&o";
        }
        if (tx.isObfuscated()) {
            returning = returning + "&k";
        }
        if (tx.isStrikethrough()) {
            returning = returning + "&m";
        }
        if (tx.isUnderlined()) {
            returning = returning + "&n";
        }
        return returning;
    }

    public static String getEnvironment(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return config().getString("Environment.normal");
            case NETHER:
                return config().getString("Environment.nether");
            case THE_END:
                return config().getString("Environment.the_end");
            default:
                return config().getString("Environment.unknown");
        }
    }

    public static String getSimpleProjectile(Projectile projectile) {
        if (projectile instanceof Arrow) {
            return "Projectile-Arrow";
        }
        if (projectile instanceof DragonFireball) {
            return "Projectile-Dragon-Fireball";
        }
        if (projectile instanceof Egg) {
            return "Projectile-Egg";
        }
        if (projectile instanceof EnderPearl) {
            return "Projectile-EnderPearl";
        }
        if (projectile instanceof Fireball) {
            return "Projectile-Fireball";
        }
        if (projectile instanceof FishHook) {
            return "Projectile-FishHook";
        }
        if (projectile instanceof LlamaSpit) {
            return "Projectile-LlamaSpit";
        }
        if (projectile instanceof Snowball) {
            return "Projectile-Snowball";
        }
        if (projectile instanceof Trident) {
            return "Projectile-Trident";
        }
        if (projectile instanceof ShulkerBullet) {
            return "Projectile-ShulkerBullet";
        }
        return "Projectile-Arrow";
    }

    public static String getSimpleCause(EntityDamageEvent.DamageCause damageCause) {
        switch (damageCause) {
            case CONTACT:
                return "Contact";
            case ENTITY_ATTACK:
                return "Melee";
            case PROJECTILE:
                return "Projectile";
            case SUFFOCATION:
                return "Suffocation";
            case FALL:
                return "Fall";
            case FIRE:
                return "Fire";
            case FIRE_TICK:
                return "Fire-Tick";
            case MELTING:
                return "Melting";
            case LAVA:
                return "Lava";
            case DROWNING:
                return "Drowning";
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                return "Explosion";
            case VOID:
                return "Void";
            case LIGHTNING:
                return "Lightning";
            case SUICIDE:
                return "Suicide";
            case STARVATION:
                return "Starvation";
            case POISON:
                return "Poison";
            case MAGIC:
                return "Magic";
            case WITHER:
                return "Wither";
            case FALLING_BLOCK:
                return "Falling-Block";
            case THORNS:
                return "Thorns";
            case DRAGON_BREATH:
                return "Dragon-Breath";
            case CUSTOM:
                return "Custom";
            case FLY_INTO_WALL:
                return "Fly-Into-Wall";
            case HOT_FLOOR:
                return "Hot-Floor";
            case CRAMMING:
                return "Cramming";
            case DRYOUT:
                return "Dryout";
            case FREEZE:
                return "Freeze";
            case SONIC_BOOM:
                return "Sonic-Boom";
            default:
                return "Unknown";
        }
    }

    public static FileConfiguration getPlayerDeathMessages() {
        return PlayerDeathMessages.getInstance().getConfig();
    }

    public static FileConfiguration getEntityDeathMessages() {
        return EntityDeathMessages.getInstance().getConfig();
    }

    public static String getColorOfString(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == 167 && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                    result.insert(0, color);
                    if (isChatColorAColor(color) || color.equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }
        return result.toString();
    }

    public static boolean isChatColorAColor(ChatColor chatColor) {
        return chatColor != ChatColor.MAGIC && chatColor != ChatColor.BOLD && chatColor != ChatColor.STRIKETHROUGH && chatColor != ChatColor.UNDERLINE && chatColor != ChatColor.ITALIC;
    }

    public static boolean hasNoCustomName(ItemStack item) {
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (meta == null) return true;
        if (!meta.hasDisplayName()) return true;
        return meta.getDisplayName().isEmpty();
    }

    public static String getCustomName(ItemStack item, Player player) {
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (meta == null) return getItemName(item, player);
        if (!meta.hasDisplayName()) return getItemName(item, player);
        return meta.getDisplayName();
    }

    public static String getEntityName(Entity entity, Player player) {
        if (entity instanceof Player) return getPlayerNameWithPlaceholder((Player) entity);
        if (DeathMessages.langUtilsEnabled) {
            return LanguageHelper.getEntityName(entity, player);
        }
        return config().getString("Mobs." + entity.getType().name().toLowerCase(), entity.getName());
    }

    public static String getItemName(ItemStack item, Player player) {
        if (DeathMessages.langUtilsEnabled) {
            return LanguageHelper.getItemDisplayName(item, player);
        } else {
            return convertString(item.getType().name());
        }
    }

    public static String getPlayerNameWithPlaceholder(Player player) {
        String msg = config().getString("PlayerName", "%player_name%");
        if (DeathMessages.getInstance().placeholderAPIEnabled) {
            msg = PlaceholderAPI.setPlaceholders(player,  msg);
        } else {
            msg = msg.replace("%player_name%", player.getName());
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String getCustomName(Entity entity, Player player) {
        if (entity instanceof Player) return getPlayerNameWithPlaceholder((Player) entity);
        String customName = entity.getCustomName();
        if (customName == null) return getEntityName(entity, player);
        return customName;
    }

    public static boolean isNotNumeric(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static String formatMessage(String path) {
        return colorize(getInstance().getConfig().getString(path, "").replace("%prefix%", config().getString("Prefix", "")));
    }

    public static String formatString(String string) {
        return colorize(string.replace("%prefix%", config().getString("Prefix", "")));
    }

    public static List<String> formatMessage(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(colorize(s.replace("%prefix%", config().getString("Prefix", ""))));
        }
        return newList;
    }

    public static List<String> sortList(List<String> list, Player player, Entity killer) {
        List<String> newList = list;
        List<String> arrayList = new ArrayList<>();
        for (String s : list) {
            if (s.contains("PERMISSION[")) {
                Matcher m = Pattern.compile("PERMISSION\\[([^)]+)]").matcher(s);
                while (m.find()) {
                    String perm = m.group(1);
                    if (player.getPlayer() != null && player.getPlayer().hasPermission(perm)) {
                        arrayList.add(s.replace("PERMISSION[" + perm + "]", ""));
                    }
                }
            }
            if (s.contains("PERMISSION_KILLER[")) {
                Matcher m2 = Pattern.compile("PERMISSION_KILLER\\[([^)]+)]").matcher(s);
                while (m2.find()) {
                    String perm2 = m2.group(1);
                    if (killer.hasPermission(perm2)) {
                        arrayList.add(s.replace("PERMISSION_KILLER[" + perm2 + "]", ""));
                    }
                }
            }
            if (s.contains("REGION[")) {
                Matcher m3 = Pattern.compile("REGION\\[([^)]+)]").matcher(s);
                while (m3.find()) {
                    String regionID = m3.group(1);
                    if (DeathMessages.worldGuardExtension != null && DeathMessages.worldGuardExtension.isInRegion(player.getPlayer(), regionID)) {
                        arrayList.add(s.replace("REGION[" + regionID + "]", ""));
                    }
                }
            }
        }
        if (!arrayList.isEmpty()) {
            newList = arrayList;
        } else {
            newList.removeIf(s2 -> s2.contains("PERMISSION[") || s2.contains("REGION[") || s2.contains("PERMISSION_KILLER["));
        }
        return newList;
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return message;
        if (DeathMessages.majorVersion() >= 16) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);
            while (true) {
                Matcher matcher2 = matcher;
                if (matcher2.find()) {
                    String color = message.substring(matcher2.start(), matcher2.end());
                    message = message.replace(color, ChatColor.of(color).toString());
                    matcher = pattern.matcher(message);
                } else {
                    return message.replace('&', (char) 167);
                }
            }
        } else {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }

    public static TextComponent bungee(String message) {
        return new TextComponent(TextComponent.fromLegacyText(colorize(message)));
    }

    public static String classSimple(Entity entity) {
        Class<?> clazz = entity.getType().getEntityClass();
        if (clazz != null) return clazz.getSimpleName().toLowerCase();
        return entity.getClass().getSimpleName().toLowerCase();
    }
}
