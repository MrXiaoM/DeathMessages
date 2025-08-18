package dev.mrshawn.deathmessages.utils;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.ExplosionManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.config.PlayerDeathMessages;
import dev.mrshawn.deathmessages.config.Settings;
import dev.mrshawn.deathmessages.enums.DeathAffiliation;
import dev.mrshawn.deathmessages.enums.MobType;
import dev.mrshawn.deathmessages.enums.PDMode;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.mrshawn.deathmessages.DeathMessages.majorVersion;
import static dev.mrshawn.deathmessages.config.Messages.parseBungee;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

public class DeathResolver {
    private static final FileSettings<Config> config = FileSettings.CONFIG;
    private static final boolean addPrefix = config.getBoolean(Config.ADD_PREFIX_TO_ALL_MESSAGES);
    public static final boolean supportDamageSource = testDamageSource();
    private static Method damageSourceGetHandle;
    private static boolean testDamageSource() {
        try {
            Class.forName("org.bukkit.damage.DamageSource");
            EntityDamageEvent.class.getDeclaredMethod("getDamageSource");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
    @Nullable
    @SuppressWarnings("UnstableApiUsage")
    public static String getDamageSourceMsgId(EntityDamageEvent e) {
        if (!supportDamageSource) return null;
        try {
            DamageSource source = e.getDamageSource();
            Class<? extends DamageSource> type = source.getClass();
            if (damageSourceGetHandle == null) {
                // org.bukkit.craftbukkit.v*_*_R*.damage.CraftDamageSource#getHandle
                damageSourceGetHandle = type.getDeclaredMethod("getHandle");
            }
            // net.minecraft.world.damagesource.DamageSource#toString
            // "DamageSource (" + this.type().msgId() + ")"
            String string = String.valueOf(damageSourceGetHandle.invoke(source));
            if (string.startsWith("DamageSource (") && string.endsWith(")")) {
                return string.substring(14, string.length() - 1);
            } else {
                return string; // 用于测试
            }
        } catch (Throwable t) {
            return null;
        }
    }

    @NotNull
    private static List<String> getStringList(ConfigurationSection section, String parent, String... keys) {
        for (@Nullable String key : keys) {
            if (key == null) continue;
            List<String> list = section.getStringList(parent + key);
            if (list.isEmpty()) continue;
            return list;
        }
        return new ArrayList<>();
    }

    public static boolean isClimbable(Block block) {
        return isClimbable(block.getType());
    }

    public static boolean isClimbable(Material material) {
        String name = material.name();
        return name.contains("LADDER") || name.contains("VINE") || name.contains("SCAFFOLDING") || name.contains("TRAPDOOR");
    }

    public static boolean isWeaponByItemName(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        List<String> list = config.getStringList(Config.CUSTOM_ITEM_DISPLAY_NAMES_IS_WEAPON);
        if (list.isEmpty()) return true;
        ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;
        if (meta == null || !meta.hasDisplayName()) return false;
        String displayName = meta.getDisplayName();
        for (String s : list) {
            Pattern pattern = Pattern.compile(Messages.colorize(s));
            Matcher matcher = pattern.matcher(displayName);
            if (matcher.find()) return true;
        }
        return false;
    }

    public static boolean isWeaponByItemMaterial(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        List<String> list = config.getStringList(Config.CUSTOM_ITEM_MATERIAL_IS_WEAPON);
        if (list.isEmpty()) return true;
        for (String s : list) {
            Material mat = Material.getMaterial(s);
            if (item.getType().equals(mat)) return true;
        }
        return false;
    }

    public static boolean isWeapon(ItemStack item) {
        return isWeaponByItemName(item) && isWeaponByItemMaterial(item);
    }

    public static boolean hasWeapon(LivingEntity mob, EntityDamageEvent.DamageCause cause) {
        EntityEquipment e = mob.getEquipment();
        return e != null && !cause.equals(THORNS) && isWeapon(getItemInHand(e));
    }

    public static TextComponent playerDeathMessage(PlayerManager pm, boolean gang) {
        LivingEntity mob = (LivingEntity) pm.getLastEntityDamager();
        EntityDamageEvent.DamageCause cause = pm.getLastDamage();
        String msgId = pm.getDamageSourceMsgId();
        boolean hasWeapon = hasWeapon(mob, cause);
        if (cause.equals(ENTITY_EXPLOSION)) {
            Entity exp = pm.getLastExplosiveEntity();
            if (exp instanceof EnderCrystal) return get(gang, pm, mob, "End-Crystal", msgId);
            if (exp instanceof TNTPrimed) return get(gang, pm, mob, "TNT", msgId);
            if (exp instanceof Firework) return get(gang, pm, mob, "Firework", msgId);
            return get(gang, pm, mob, Messages.getSimpleCause(ENTITY_EXPLOSION), msgId);
        }
        if (cause.equals(BLOCK_EXPLOSION)) {
            ExplosionManager manager = ExplosionManager.getManagerIfEffected(pm.getUUID());
            if (manager != null) {
                String m = manager.getMaterial().name().toUpperCase();
                if (m.contains("BED")) return get(gang, pm, manager.getPyro(), "Bed", msgId);
                if (m.equals("RESPAWN_ANCHOR")) return get(gang, pm, manager.getPyro(), "Respawn-Anchor", msgId);
            }
        }
        Projectile ep = pm.getLastProjectileEntity();
        if (hasWeapon) {
            if (cause.equals(ENTITY_ATTACK)) return getWeapon(gang, pm, mob);
            if (cause.equals(PROJECTILE) && (getSettings().getBoolean("Ignore-Projectile-Type") || ep instanceof Arrow))
                return getProjectile(gang, pm, mob, Messages.getSimpleProjectile(ep));
            return get(gang, pm, mob, Messages.getSimpleCause(ENTITY_ATTACK), msgId);
        }
        for (EntityDamageEvent.DamageCause dc : EntityDamageEvent.DamageCause.values()) {
            if (cause.equals(PROJECTILE)) return getProjectile(gang, pm, mob, Messages.getSimpleProjectile(ep));
            if (cause.equals(dc)) return get(gang, pm, mob, Messages.getSimpleCause(dc), msgId);
        }
        return null;
    }

    public static TextComponent entityDeathMessage(EntityManager em, MobType mobType) {
        PlayerManager pm = em.getLastPlayerDamager();
        Player p = pm.getPlayer();
        boolean hasWeapon = hasWeapon(p, pm.getLastDamage());
        EntityDamageEvent.DamageCause cause = em.getLastDamage();
        String msgId = em.getDamageSourceMsgId();
        if (cause.equals(ENTITY_EXPLOSION)) {
            if (em.getLastExplosiveEntity() instanceof EnderCrystal) return getEntityDeath(p, em.getEntity(), "End-Crystal", mobType, msgId);
            if (em.getLastExplosiveEntity() instanceof TNTPrimed) return getEntityDeath(p, em.getEntity(), "TNT", mobType, msgId);
            if (em.getLastExplosiveEntity() instanceof Firework) return getEntityDeath(p, em.getEntity(), "Firework", mobType, msgId);
            return getEntityDeath(p, em.getEntity(), Messages.getSimpleCause(ENTITY_EXPLOSION), mobType, msgId);
        }
        if (cause.equals(BLOCK_EXPLOSION)) {
            ExplosionManager manager = ExplosionManager.getManagerIfEffected(em.getEntityUUID());
            if (manager != null) {
                String m = manager.getMaterial().name().toUpperCase();
                if (m.contains("BED")) return getEntityDeath(manager.getPyro(), em.getEntity(), "Bed", mobType, msgId);
                if (m.equals("RESPAWN_ANCHOR")) return getEntityDeath(manager.getPyro(), em.getEntity(), "Respawn-Anchor", mobType, msgId);
            }
        }
        Projectile ep = em.getLastProjectileEntity();
        if (hasWeapon) {
            if (cause.equals(ENTITY_ATTACK)) return getEntityDeathWeapon(p, em.getEntity(), mobType);
            if (cause.equals(PROJECTILE) && (getSettings().getBoolean("Ignore-Projectile-Type") || ep instanceof Arrow))
                return getEntityDeathProjectile(p, em, Messages.getSimpleProjectile(ep), mobType);
            return getEntityDeathWeapon(p, em.getEntity(), mobType);
        }
        for (EntityDamageEvent.DamageCause dc : EntityDamageEvent.DamageCause.values()) {
            if (cause.equals(PROJECTILE)) return getEntityDeathProjectile(p, em, Messages.getSimpleProjectile(ep), mobType);
            if (cause.equals(dc)) return getEntityDeath(p, em.getEntity(), Messages.getSimpleCause(dc), mobType, msgId);
        }
        return null;
    }

    @Deprecated
    public static TextComponent resolveWeapon(String splitMessage, ItemStack i, String displayName) {
        return new TextComponent(parseWeapon(splitMessage, i, displayName));
    }

    public static BaseComponent parseWeapon(String splitMessage, ItemStack i, String displayName) {
        String[] spl = splitMessage.split("%weapon%");
        if (spl.length != 0 && spl[0] != null && !spl[0].isEmpty()) {
            displayName = Messages.colorize(spl[0]) + displayName;
        }
        if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].isEmpty()) {
            displayName = displayName + Messages.colorize(spl[1]);
        }
        BaseComponent weaponComp = parseBungee(displayName);
        weaponComp.setHoverEvent(HoverShowItemResolver.toHoverEvent(i));
        return weaponComp;
    }

    public static TextComponent addHoverAndClick(TextComponent tc, String[] sec, Function<String, String> placeholders) {
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(placeholders.apply(sec[1]))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + placeholders.apply(cmd)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + placeholders.apply(cmd2)));
            }
        }
        return tc;
    }

    public static TextComponent getNaturalDeath(PlayerManager pm, String damageCause) {
        return getNaturalDeath(pm, damageCause, null);
    }

    public static TextComponent getNaturalDeath(PlayerManager pm, String damageCause, @Nullable String msgId) {
        List<String> unsortedMessages = getStringList(Messages.getPlayerDeathMessages(), "Natural-Cause.", msgId, damageCause);
        List<String> msgs = Messages.sortList(unsortedMessages, pm.getPlayer(), pm.getPlayer());
        if (msgs.isEmpty()) return null;
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        EntityEquipment equipment = pm.getPlayer().getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%block%") && (pm.getLastEntityDamager() instanceof FallingBlock)) {
                FallingBlock fb = (FallingBlock) pm.getLastEntityDamager();
                String mssa = Messages.getBlockName(splitMessage, fb, pm.getPlayer());
                if (mssa == null) {
                    DeathMessages.getInstance().getLogger().severe("Could not parse %block%. Please check your config for a wrong value. Your materials could be spelt wrong or it does not exists in the config.");
                    pm.setLastEntityDamager(null);
                    return getNaturalDeath(pm, Messages.getSimpleCause(SUFFOCATION));
                }
                tc.addExtra(mssa);
                lastColor = Messages.getColorOfString(lastColor + mssa);
            } else if (splitMessage.contains("%climbable%") && pm.getLastDamage().equals(FALL)) {
                Material mat = pm.getLastClimbing();
                String mssa2 = Messages.getBlockName(splitMessage, mat, pm.getPlayer());
                if (mssa2 == null) {
                    DeathMessages.getInstance().getLogger().severe("Could not parse %climbable%. Please check your config for a wrong value. Your materials could be spelt wrong or it does not exists in the config.");
                    pm.setLastClimbing(null);
                    return getNaturalDeath(pm, Messages.getSimpleCause(FALL));
                }
                tc.addExtra(mssa2);
                lastColor = Messages.getColorOfString(lastColor + mssa2);
            } else if (pm.getLastDamage().equals(PROJECTILE) && splitMessage.contains("%weapon%") && equipment != null) {
                ItemStack i = getItemInHand(equipment);
                String m = i.getType().name().toUpperCase();
                if (!m.equals("BOW") && !m.equals("CROSSBOW")) return getNaturalDeath(pm, "Projectile-Unknown");
                String displayName;
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().isEmpty()) return getNaturalDeath(pm, "Projectile-Unknown");
                        } else return getNaturalDeath(pm, "Projectile-Unknown");
                    }
                    displayName = Messages.getItemName(i, pm.getPlayer());
                } else {
                    displayName = Messages.getCustomName(i, pm.getPlayer());
                }
                tc.addExtra(parseWeapon(splitMessage, i, displayName));
            } else {
                BaseComponent tx = parseBungee(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, null) + " ");
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) lastColor = bs.getColor().toString();
                    lastFont = Messages.formatting(bs);
                }
            }
        }
        return addHoverAndClick(tc, sec, cmd -> playerDeathPlaceholders(cmd, pm, null));
    }

    public static TextComponent getWeapon(boolean gang, PlayerManager pm, LivingEntity mob) {
        List<String> msgs;
        boolean basicMode = PlayerDeathMessages.getInstance().getConfig().getBoolean("Basic-Mode.Enabled");
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + Messages.classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + ".Weapon"), pm.getPlayer(), mob);
        } else msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList(cMode + "." + affiliation + ".Weapon"), pm.getPlayer(), mob);
        if (msgs.isEmpty()) return null;
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        EntityEquipment mobEquipment = mob.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && mobEquipment != null) {
                ItemStack i = getItemInHand(mobEquipment);
                String displayName;
                if (Messages.hasNoCustomName(i)) {
                    if (FileSettings.CONFIG.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!FileSettings.CONFIG.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().isEmpty()) return get(gang, pm, mob, FileSettings.CONFIG.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO));
                        } else return get(gang, pm, mob, FileSettings.CONFIG.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO));
                    }
                    displayName = Messages.getItemName(i, pm.getPlayer());
                } else {
                    displayName = Messages.getCustomName(i, pm.getPlayer());
                }
                tc.addExtra(parseWeapon(splitMessage, i, displayName));
            } else {
                BaseComponent tx = parseBungee(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob) + " ");
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) lastColor = bs.getColor().toString();
                    lastFont = Messages.formatting(bs);
                }
            }
        }
        return addHoverAndClick(tc, sec, cmd -> playerDeathPlaceholders(cmd, pm, mob));
    }

    public static TextComponent getEntityDeathWeapon(Player p, Entity e, MobType mobType) {
        List<String> msgs;
        String entityName = Messages.classSimple(e);
        if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getUniqueId())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(e);
            }
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + ".Weapon"), p, e);
        } else msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + entityName + ".Weapon"), p, e);
        if (msgs.isEmpty()) return null;
        boolean hasOwner = e instanceof Tameable && ((Tameable) e).getOwner() != null;
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        EntityEquipment equipment = p.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && equipment != null) {
                ItemStack i = getItemInHand(equipment);
                String displayName;
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().isEmpty())
                                return getEntityDeath(p, e, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO), mobType);
                        } else return getEntityDeath(p, e, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO), mobType);
                    }
                    displayName = Messages.getItemName(i, p);
                } else displayName = Messages.getCustomName(i, p);
                tc.addExtra(parseWeapon(splitMessage, i, displayName));
            } else {
                BaseComponent tx = parseBungee(entityDeathPlaceholders(lastColor + lastFont + splitMessage, p, e, hasOwner) + " ");
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) lastColor = bs.getColor().toString();
                    lastFont = Messages.formatting(bs);
                }
            }
        }
        return addHoverAndClick(tc, sec, cmd -> entityDeathPlaceholders(cmd, p, e, hasOwner));
    }

    public static TextComponent get(boolean gang, PlayerManager pm, UUID pyro, String damageCause) {
        return get(gang, pm, pyro, damageCause, null);
    }

    public static TextComponent get(boolean gang, PlayerManager pm, UUID pyro, String damageCause, String msgId) {
        PlayerManager p = PlayerManager.getPlayer(pyro);
        return p == null ? null : get(gang, pm, p.getPlayer(), damageCause, msgId);
    }

    public static TextComponent get(boolean gang, PlayerManager pm, LivingEntity mob, String damageCause) {
        return get(gang, pm, mob, damageCause, null);
    }

    public static TextComponent get(boolean gang, PlayerManager pm, LivingEntity mob, String damageCause, String msgId) {
        List<String> msgs;
        boolean basicMode = PlayerDeathMessages.getInstance().getConfig().getBoolean("Basic-Mode.Enabled");
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + Messages.classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = Messages.sortList(getStringList(Messages.getPlayerDeathMessages(),"Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + ".", msgId, damageCause), pm.getPlayer(), mob);
        } else msgs = Messages.sortList(getStringList(Messages.getPlayerDeathMessages(), cMode + "." + affiliation + ".", msgId, damageCause), pm.getPlayer(), mob);
        if (msgs.isEmpty()) {
            if (config.getBoolean(Config.DEFAULT_NATURAL_DEATH_NOT_DEFINED)) return getNaturalDeath(pm, damageCause, msgId);
            if (config.getBoolean(Config.DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED)) return get(gang, pm, mob, Messages.getSimpleCause(ENTITY_ATTACK), msgId);
            return null;
        }
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            BaseComponent tx = parseBungee(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob) + " ");
            tc.addExtra(tx);
            for (BaseComponent bs : tx.getExtra()) {
                if (bs.getColor() != null) {
                    lastColor = bs.getColor().toString();
                }
                lastFont = Messages.formatting(bs);
            }
        }
        return addHoverAndClick(tc, sec, cmd -> playerDeathPlaceholders(cmd, pm, mob));
    }

    public static TextComponent getProjectile(boolean gang, PlayerManager pm, LivingEntity mob, String projectileDamage) {
        List<String> msgs;
        boolean basicMode = PlayerDeathMessages.getInstance().getConfig().getBoolean("Basic-Mode.Enabled");
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + Messages.classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + "." + projectileDamage), pm.getPlayer(), mob);
        } else msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList(cMode + "." + affiliation + "." + projectileDamage), pm.getPlayer(), mob);
        if (msgs.isEmpty()) return null;
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        EntityEquipment equipment = mob.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && (getSettings().getBoolean("Ignore-Projectile-Type") || pm.getLastProjectileEntity() instanceof Arrow) && equipment != null) {
                ItemStack i = getItemInHand(equipment);
                String displayName;
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED) && !config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO).equals(projectileDamage)) {
                        return getProjectile(gang, pm, mob, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO));
                    }
                    displayName = Messages.getItemName(i, pm.getPlayer());
                } else {
                    displayName = Messages.getCustomName(i, pm.getPlayer());
                }
                tc.addExtra(parseWeapon(splitMessage, i, displayName));
            } else {
                BaseComponent tx = parseBungee(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob) + " ");
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) lastColor = bs.getColor().toString();
                    lastFont = Messages.formatting(bs);
                }
            }
        }
        return addHoverAndClick(tc, sec, cmd -> playerDeathPlaceholders(cmd, pm, mob));
    }

    public static TextComponent getEntityDeathProjectile(Player p, EntityManager em, String projectileDamage, MobType mobType) {
        List<String> msgs;
        String entityName = Messages.classSimple(em.getEntity());
        if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(em.getEntityUUID()))
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(em.getEntity());
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + "." + projectileDamage), p, em.getEntity());
        } else msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + entityName + "." + projectileDamage), p, em.getEntity());
        if (msgs.isEmpty()) {
            if (!config.getBoolean(Config.DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED)) return null;
            return getEntityDeath(p, em.getEntity(), Messages.getSimpleCause(ENTITY_ATTACK), mobType);
        }
        Entity entity = em.getEntity();
        boolean hasOwner = entity instanceof Tameable && ((Tameable) entity).getOwner() != null;
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        EntityEquipment equipment = p.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && (getSettings().getBoolean("Ignore-Projectile-Type") || em.getLastProjectileEntity() instanceof Arrow) && equipment != null) {
                ItemStack i = getItemInHand(equipment);
                String displayName;
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED) && !config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO).equals(projectileDamage)) {
                        return getEntityDeathProjectile(p, em, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO), mobType);
                    }
                    displayName = Messages.getItemName(i, p);
                } else {
                    displayName = Messages.getCustomName(i, p);
                }
                tc.addExtra(parseWeapon(splitMessage, i, displayName));
            } else {
                BaseComponent tx = parseBungee(entityDeathPlaceholders(lastColor + lastFont + splitMessage, p, em.getEntity(), hasOwner) + " ");
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = Messages.formatting(bs);
                }
            }
        }
        return addHoverAndClick(tc, sec, cmd -> entityDeathPlaceholders(cmd, p, em.getEntity(), hasOwner));
    }

    public static TextComponent getEntityDeath(UUID pyro, Entity entity, String damageCause, MobType mobType) {
        return getEntityDeath(pyro, entity, damageCause, mobType, null);
    }
    public static TextComponent getEntityDeath(UUID pyro, Entity entity, String damageCause, MobType mobType, String msgId) {
        PlayerManager pm = PlayerManager.getPlayer(pyro);
        return pm == null ? null : getEntityDeath(pm.getPlayer(), entity, damageCause, mobType, msgId);
    }
    public static TextComponent getEntityDeath(Player player, Entity entity, String damageCause, MobType mobType) {
        return getEntityDeath(player, entity, damageCause, mobType, null);
    }
    public static TextComponent getEntityDeath(Player player, Entity entity, String damageCause, MobType mobType, String msgId) {
        boolean hasOwner = entity instanceof Tameable && ((Tameable) entity).getOwner() != null;
        List<String> msgs;
        if (hasOwner) {
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + Messages.classSimple(entity) + ".Tamed"), player, entity);
        } else if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(entity.getUniqueId()))
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(entity);
            msgs = Messages.sortList(getStringList(Messages.getEntityDeathMessages(), "Mythic-Mobs-Entities." + internalMobType + ".", msgId, damageCause), player, entity);
        } else msgs = Messages.sortList(getStringList(Messages.getEntityDeathMessages(), "Entities." + Messages.classSimple(entity) + ".", msgId, damageCause), player, entity);

        if (msgs.isEmpty()) return null;
        TextComponent tc = new TextComponent("");
        if (addPrefix) tc.addExtra(parseBungee(Messages.getInstance().getConfig().getString("Prefix", "")));
        String msg = msgs.get(new Random().nextInt(msgs.size()));
        String[] sec = msg.split("::");
        String firstSection = msg.contains("::") ? (sec.length == 0 ? msg : sec[0]) : msg;
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            BaseComponent tx = parseBungee(entityDeathPlaceholders(lastColor + lastFont + splitMessage, player, entity, hasOwner) + " ");
            tc.addExtra(tx);
            for (BaseComponent bs : tx.getExtra()) {
                if (bs.getColor() != null) {
                    lastColor = bs.getColor().toString();
                }
                lastFont = Messages.formatting(bs);
            }
        }
        return addHoverAndClick(tc, sec, cmd -> entityDeathPlaceholders(cmd, player, entity, hasOwner));
    }

    public static String entityDeathPlaceholders(String msg, Player player, Entity entity, boolean owner) {
        World world = entity.getWorld();
        Block loc = entity.getLocation().getBlock();
        String msg2 = Messages.colorize(msg
                .replace("%entity%", Messages.getEntityName(entity, player))
                .replace("%entity_display%", Messages.getCustomName(entity, player))
                .replace("%killer%", Messages.getPlayerNameWithPlaceholder(player))
                .replace("%killer_display%", player.getDisplayName())
                .replace("%world%", world.getName())
                .replace("%world_environment%", Messages.getEnvironment(world.getEnvironment()))
                .replace("%x%", String.valueOf(loc.getX()))
                .replace("%y%", String.valueOf(loc.getY()))
                .replace("%z%", String.valueOf(loc.getZ())));
        if (owner && (entity instanceof Tameable)) {
            Tameable tameable = (Tameable) entity;
            if (tameable.getOwner() != null && tameable.getOwner().getName() != null) {
                String tameOwner = tameable.getOwner().getName();
                if (tameable.getOwner() instanceof Player) tameOwner = Messages.getPlayerNameWithPlaceholder((Player) tameable);
                msg2 = msg2.replace("%owner%", tameOwner);
            }
        }
        try {
            msg2 = msg2.replace("%biome%", loc.getBiome().name());
        } catch (NullPointerException e) {
            DeathMessages.getInstance().getLogger().severe("Custom Biome detected. Using 'Unknown' for a biome name.");
            DeathMessages.getInstance().getLogger().severe("Custom Biomes are not supported yet.'");
            msg2 = msg2.replace("%biome%", "Unknown");
        }
        if (DeathMessages.getInstance().placeholderAPIEnabled) {
            msg2 = PlaceholderAPI.setPlaceholders(player, msg2);
        }
        return msg2;
    }

    public static String playerDeathPlaceholders(String msg, PlayerManager pm, LivingEntity mob) {
        String msg2;
        if (mob == null) {
            Block playerLoc = pm.getLastLocation().getBlock();
            World world = playerLoc.getWorld();
            String msgId = pm.getDamageSourceMsgId();
            msg2 = Messages.colorize(msg
                    .replace("%player%", Messages.getPlayerNameWithPlaceholder(pm.getPlayer()))
                    .replace("%player_display%", pm.getPlayer().getDisplayName())
                    .replace("%world%", world.getName())
                    .replace("%world_environment%", Messages.getEnvironment(world.getEnvironment()))
                    .replace("%x%", String.valueOf(playerLoc.getX()))
                    .replace("%y%", String.valueOf(playerLoc.getY()))
                    .replace("%z%", String.valueOf(playerLoc.getZ())))
                    .replace("%source%", String.valueOf(msgId));
            try {
                msg2 = msg2.replace("%biome%", playerLoc.getBiome().name());
            } catch (NullPointerException e) {
                DeathMessages.getInstance().getLogger().severe("Custom Biome detected. Using 'Unknown' for a biome name.");
                DeathMessages.getInstance().getLogger().severe("Custom Biomes are not supported yet.'");
                msg2 = msg2.replace("%biome%", "Unknown");
            }
        } else {
            String mobName = Messages.getCustomName(mob, pm.getPlayer());
            if (config.getBoolean(Config.RENAME_MOBS_ENABLED)) {
                String[] chars = config.getString(Config.RENAME_MOBS_IF_CONTAINS).split("(?!^)");
                for (String ch : chars) {
                    if (mobName.contains(ch)) {
                        mobName = Messages.getEntityName(mob, pm.getPlayer());
                        break;
                    }
                }
            }
            if (!(mob instanceof Player) && config.getBoolean(Config.DISABLE_NAMED_MOBS)) {
                mobName = Messages.getEntityName(mob, pm.getPlayer());
            }
            Block playerLoc = pm.getLastLocation().getBlock();
            World world = playerLoc.getWorld();
            msg2 = msg.replace("%player%", Messages.getPlayerNameWithPlaceholder(pm.getPlayer()))
                    .replace("%player_display%", pm.getPlayer().getDisplayName())
                    .replace("%killer%", mobName)
                    .replace("%killer_type%", Messages.getEntityName(mob, pm.getPlayer()))
                    .replace("%world%", world.getName())
                    .replace("%world_environment%", Messages.getEnvironment(world.getEnvironment()))
                    .replace("%x%", String.valueOf(playerLoc.getX()))
                    .replace("%y%", String.valueOf(playerLoc.getY()))
                    .replace("%z%", String.valueOf(playerLoc.getZ()));
            try {
                msg2 = msg2.replace("%biome%", playerLoc.getBiome().name());
            } catch (NullPointerException e2) {
                DeathMessages.getInstance().getLogger().severe("Custom Biome detected. Using 'Unknown' for a biome name.");
                DeathMessages.getInstance().getLogger().severe("Custom Biomes are not supported yet.'");
                msg2 = msg2.replace("%biome%", "Unknown");
            }
            if (mob instanceof Player) {
                msg2 = msg2.replace("%killer_display%", ((Player) mob).getDisplayName());
            }
        }
        if (DeathMessages.getInstance().placeholderAPIEnabled) {
            msg2 = PlaceholderAPI.setPlaceholders(pm.getPlayer(), msg2);
        }
        return msg2;
    }

    public static FileConfiguration getSettings() {
        return Settings.getInstance().getConfig();
    }

    @SuppressWarnings({"deprecation"})
    public static ItemStack getItemInHand(EntityEquipment equipment) {
        return majorVersion() < 9 ? equipment.getItemInHand() : equipment.getItemInMainHand();
    }
}
