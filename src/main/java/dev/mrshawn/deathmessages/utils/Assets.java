package dev.mrshawn.deathmessages.utils;

import com.cryptomorin.xseries.XMaterial;
import com.meowj.langutils.lang.LanguageHelper;
import de.tr7zw.nbtapi.NBTItem;
import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.EntityManager;
import dev.mrshawn.deathmessages.api.ExplosionManager;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.EntityDeathMessages;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.config.PlayerDeathMessages;
import dev.mrshawn.deathmessages.config.Settings;
import dev.mrshawn.deathmessages.enums.DeathAffiliation;
import dev.mrshawn.deathmessages.enums.MobType;
import dev.mrshawn.deathmessages.enums.PDMode;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assets {
    private static final FileSettings<Config> config = FileSettings.CONFIG;
    private static final boolean addPrefix = config.getBoolean(Config.ADD_PREFIX_TO_ALL_MESSAGES);
    public static final HashMap<String, String> addingMessage = new HashMap<>();

    public static boolean isNotNumeric(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static String formatMessage(String path) {
        return colorize(Messages.getInstance().getConfig().getString(path, "").replace("%prefix%", Messages.getInstance().getConfig().getString("Prefix", "")));
    }

    public static String formatString(String string) {
        return colorize(string.replace("%prefix%", Messages.getInstance().getConfig().getString("Prefix", "")));
    }

    public static List<String> formatMessage(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String s : list) {
            newList.add(colorize(s.replace("%prefix%", Messages.getInstance().getConfig().getString("Prefix", ""))));
        }
        return newList;
    }

    public static boolean isClimbable(Block block) {
        return isClimbable(block.getType());
    }

    public static boolean isClimbable(Material material) {
        String name = material.name();
        return name.contains("LADDER") || name.contains("VINE") || name.contains("SCAFFOLDING") || name.contains("TRAPDOOR");
    }

    public static boolean itemNameIsWeapon(ItemStack itemStack) {
        if (itemStack == null) return false;
        List<String> list = config.getStringList(Config.CUSTOM_ITEM_DISPLAY_NAMES_IS_WEAPON);
        if (list.isEmpty()) return true;
        ItemMeta meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;
        if (meta == null || !meta.hasDisplayName()) return false;

        String displayName = meta.getDisplayName();
        for (String s : list) {
            Pattern pattern = Pattern.compile(colorize(s));
            Matcher matcher = pattern.matcher(displayName);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public static boolean itemMaterialIsWeapon(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        List<String> list = config.getStringList(Config.CUSTOM_ITEM_MATERIAL_IS_WEAPON);
        if (list.isEmpty()) return true;
        for (String s : list) {
            Material mat = Material.getMaterial(s);
            if (mat == null) {
                return false;
            }
            if (itemStack.getType().equals(mat)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWeapon(ItemStack itemStack) {
        return itemStack != null && !itemStack.getType().isAir() && itemNameIsWeapon(itemStack) && itemMaterialIsWeapon(itemStack);
    }

    @SuppressWarnings({"deprecation"})
    public static boolean hasWeapon(LivingEntity mob, EntityDamageEvent.DamageCause damageCause) {
        EntityEquipment equipment = mob.getEquipment();
        return !damageCause.equals(EntityDamageEvent.DamageCause.THORNS) && equipment != null &&
                (DeathMessages.majorVersion() < 9 ?
                        isWeapon(equipment.getItemInHand()) :
                        isWeapon(equipment.getItemInMainHand())
                );
    }

    public static TextComponent playerDeathMessage(PlayerManager pm, boolean gang) {
        //EntityDamageEvent.DamageCause[] values;
        LivingEntity mob = (LivingEntity) pm.getLastEntityDamager();
        boolean hasWeapon = hasWeapon(mob, pm.getLastDamage());
        if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            if (pm.getLastExplosiveEntity() instanceof EnderCrystal) {
                return get(gang, pm, mob, "End-Crystal");
            }
            if (pm.getLastExplosiveEntity() instanceof TNTPrimed) {
                return get(gang, pm, mob, "TNT");
            }
            if (pm.getLastExplosiveEntity() instanceof Firework) {
                return get(gang, pm, mob, "Firework");
            }
            return get(gang, pm, mob, getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION));
        }
        if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            ExplosionManager explosionManager = ExplosionManager.getManagerIfEffected(pm.getUUID());
            if (explosionManager != null) {
                if (explosionManager.getMaterial().name().contains("BED")) {
                    PlayerManager pyro = PlayerManager.getPlayer(explosionManager.getPyro());
                    if (pyro != null) return get(gang, pm, pyro.getPlayer(), "Bed");
                } else if (DeathMessages.majorVersion() >= 16 && explosionManager.getMaterial().equals(Material.RESPAWN_ANCHOR)) {
                    PlayerManager pyro = PlayerManager.getPlayer(explosionManager.getPyro());
                    if (pyro != null) return get(gang, pm, pyro.getPlayer(), "Respawn-Anchor");
                }
            }
        }
        if (hasWeapon) {
            if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                return getWeapon(gang, pm, mob);
            }
            if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE) && (getSettings().getBoolean("Ignore-Projectile-Type") || pm.getLastProjectileEntity() instanceof Arrow)) {
                return getProjectile(gang, pm, mob, getSimpleProjectile(pm.getLastProjectileEntity()));
            }
            return get(gang, pm, mob, getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        }
        for (EntityDamageEvent.DamageCause dc : EntityDamageEvent.DamageCause.values()) {
            if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                return getProjectile(gang, pm, mob, getSimpleProjectile(pm.getLastProjectileEntity()));
            }
            if (pm.getLastDamage().equals(dc)) {
                return get(gang, pm, mob, getSimpleCause(dc));
            }
        }
        return null;
    }

    public static TextComponent entityDeathMessage(EntityManager em, MobType mobType) {
        PlayerManager pm = em.getLastPlayerDamager();
        Player p = pm.getPlayer();
        boolean hasWeapon = hasWeapon(p, pm.getLastDamage());
        if (em.getLastDamage().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            if (em.getLastExplosiveEntity() instanceof EnderCrystal) {
                return getEntityDeath(p, em.getEntity(), "End-Crystal", mobType);
            }
            if (em.getLastExplosiveEntity() instanceof TNTPrimed) {
                return getEntityDeath(p, em.getEntity(), "TNT", mobType);
            }
            if (em.getLastExplosiveEntity() instanceof Firework) {
                return getEntityDeath(p, em.getEntity(), "Firework", mobType);
            }
            return getEntityDeath(p, em.getEntity(), getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION), mobType);
        }
        if (em.getLastDamage().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            ExplosionManager explosionManager = ExplosionManager.getManagerIfEffected(em.getEntityUUID());
            if (explosionManager != null) {
                if (explosionManager.getMaterial().name().contains("BED")) {
                    PlayerManager pyro = PlayerManager.getPlayer(explosionManager.getPyro());
                    if (pyro != null) return getEntityDeath(pyro.getPlayer(), em.getEntity(), "Bed", mobType);
                } else if (DeathMessages.majorVersion() >= 16 && explosionManager.getMaterial().equals(Material.RESPAWN_ANCHOR)) {
                    PlayerManager pyro = PlayerManager.getPlayer(explosionManager.getPyro());
                    if (pyro != null)
                        return getEntityDeath(pyro.getPlayer(), em.getEntity(), "Respawn-Anchor", mobType);
                }
            }
        }
        if (hasWeapon) {
            if (em.getLastDamage().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                return getEntityDeathWeapon(p, em.getEntity(), mobType);
            }
            if (em.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE) && (getSettings().getBoolean("Ignore-Projectile-Type") || em.getLastProjectileEntity() instanceof Arrow)) {
                return getEntityDeathProjectile(p, em, getSimpleProjectile(em.getLastProjectileEntity()), mobType);
            }
            return getEntityDeathWeapon(p, em.getEntity(), mobType);
        }
        for (EntityDamageEvent.DamageCause dc : EntityDamageEvent.DamageCause.values()) {
            if (em.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                return getEntityDeathProjectile(p, em, getSimpleProjectile(em.getLastProjectileEntity()), mobType);
            }
            if (em.getLastDamage().equals(dc)) {
                return getEntityDeath(p, em.getEntity(), getSimpleCause(dc), mobType);
            }
        }
        return null;
    }

    @SuppressWarnings({"deprecation"})
    public static TextComponent getNaturalDeath(PlayerManager pm, String damageCause) {
        String firstSection;
        //String[] split;
        ItemStack i;
        String displayName;
        String material;
        Random random = new Random();
        List<String> msgs = sortList(getPlayerDeathMessages().getStringList("Natural-Cause." + damageCause), pm.getPlayer(), pm.getPlayer());
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        EntityEquipment equipment = pm.getPlayer().getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%block%") && (pm.getLastEntityDamager() instanceof FallingBlock)) {
                FallingBlock fb = (FallingBlock) pm.getLastEntityDamager();
                String mssa = getBlockName(splitMessage, fb, pm.getPlayer());
                if (mssa == null) {
                    DeathMessages.getInstance().getLogger().severe("Could not parse %block%. Please check your config for a wrong value. Your materials could be spelt wrong or it does not exists in the config.");
                    pm.setLastEntityDamager(null);
                    return getNaturalDeath(pm, getSimpleCause(EntityDamageEvent.DamageCause.SUFFOCATION));
                }
                tc.addExtra(mssa);
                lastColor = getColorOfString(lastColor + mssa);
            } else if (splitMessage.contains("%climbable%") && pm.getLastDamage().equals(EntityDamageEvent.DamageCause.FALL)) {
                Material mat = XMaterial.matchXMaterial(pm.getLastClimbing()).parseMaterial();
                String mssa2 = getBlockName(splitMessage, mat, pm.getPlayer());
                if (mssa2 == null) {
                    DeathMessages.getInstance().getLogger().severe("Could not parse %climbable%. Please check your config for a wrong value. Your materials could be spelt wrong or it does not exists in the config.");
                    pm.setLastClimbing(null);
                    return getNaturalDeath(pm, getSimpleCause(EntityDamageEvent.DamageCause.FALL));
                }
                tc.addExtra(mssa2);
                lastColor = getColorOfString(lastColor + mssa2);
            } else if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE) && splitMessage.contains("%weapon%") && equipment != null) {

                if (DeathMessages.majorVersion() <= 9) {
                    i = equipment.getItemInHand();
                } else {
                    i = equipment.getItemInMainHand();
                }
                if (!i.getType().equals(XMaterial.BOW.parseMaterial())) {
                    return getNaturalDeath(pm, "Projectile-Unknown");
                }
                if (DeathMessages.majorVersion() >= 14 && !i.getType().equals(XMaterial.CROSSBOW.parseMaterial())) {
                    return getNaturalDeath(pm, "Projectile-Unknown");
                }
                if (hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().size() == 0) {
                                return getNaturalDeath(pm, "Projectile-Unknown");
                            }
                        } else {
                            return getNaturalDeath(pm, "Projectile-Unknown");
                        }
                    }
                    displayName = getName(i, pm.getPlayer());
                } else {
                    displayName = getCustomName(i, pm.getPlayer());
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = colorize(spl[0]) + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);

            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, null)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = formatting(bs);
                }
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(playerDeathPlaceholders(sec[1], pm, null))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].substring("COMMAND:".length());
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + playerDeathPlaceholders(cmd, pm, null)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].substring("SUGGEST_COMMAND:".length());
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + playerDeathPlaceholders(cmd2, pm, null)));
            }
        }
        return tc;
    }

    @SuppressWarnings({"deprecation"})
    public static TextComponent getWeapon(boolean gang, PlayerManager pm, LivingEntity mob) {
        List<String> msgs;
        String firstSection;
        //String[] split;
        ItemStack i;
        String displayName;
        Random random = new Random();
        boolean basicMode = PlayerDeathMessages.getInstance().getConfig().getBoolean("Basic-Mode.Enabled");
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = sortList(getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + ".Weapon"), pm.getPlayer(), mob);
        } else {
            msgs = sortList(getPlayerDeathMessages().getStringList(cMode + "." + affiliation + ".Weapon"), pm.getPlayer(), mob);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        EntityEquipment mobEquipment = mob.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && mobEquipment != null) {
                if (DeathMessages.majorVersion() <= 9) {
                    i = mobEquipment.getItemInHand();
                } else {
                    i = mobEquipment.getItemInMainHand();
                }
                if (hasNoCustomName(i)) {
                    if (FileSettings.CONFIG.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!FileSettings.CONFIG.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().size() == 0) {
                                return get(gang, pm, mob, FileSettings.CONFIG.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO));
                            }
                        } else {
                            return get(gang, pm, mob, FileSettings.CONFIG.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO));
                        }
                    }
                    displayName = getName(i, pm.getPlayer());
                } else {
                    displayName = getCustomName(i, pm.getPlayer());
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = colorize(spl[0]) + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = formatting(bs);
                }
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(playerDeathPlaceholders(sec[1], pm, mob))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + playerDeathPlaceholders(cmd, pm, mob)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + playerDeathPlaceholders(cmd2, pm, mob)));
            }
        }
        return tc;
    }

    @SuppressWarnings({"deprecation"})
    public static TextComponent getEntityDeathWeapon(Player p, Entity e, MobType mobType) {
        List<String> msgs;
        String firstSection;
        //String[] split;
        ItemStack i;
        String displayName;
        Random random = new Random();
        String entityName = classSimple(e);
        if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getUniqueId())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(e);
            }
            msgs = sortList(getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + ".Weapon"), p, e);
        } else {
            msgs = sortList(getEntityDeathMessages().getStringList("Entities." + entityName + ".Weapon"), p, e);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        boolean hasOwner = false;
        if (e instanceof Tameable) {
            Tameable tameable = (Tameable) e;
            if (tameable.getOwner() != null) {
                hasOwner = true;
            }
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        EntityEquipment equipment = p.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && equipment != null) {
                if (DeathMessages.majorVersion() <= 9) {
                    i = equipment.getItemInHand();
                } else {
                    i = equipment.getItemInMainHand();
                }
                if (hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().size() == 0) {
                                return getEntityDeath(p, e, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO), mobType);
                            }
                        } else {
                            return getEntityDeath(p, e, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO), mobType);
                        }
                    }
                    displayName = getName(i, p);
                } else {
                    displayName = getCustomName(i, p);
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = colorize(spl[0]) + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(entityDeathPlaceholders(lastColor + lastFont + splitMessage, p, e, hasOwner)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = formatting(bs);
                }
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(entityDeathPlaceholders(sec[1], p, e, hasOwner))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + entityDeathPlaceholders(cmd, p, e, hasOwner)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + entityDeathPlaceholders(cmd2, p, e, hasOwner)));
            }
        }
        return tc;
    }

    public static TextComponent get(boolean gang, PlayerManager pm, LivingEntity mob, String damageCause) {
        List<String> msgs;
        String firstSection;
        //String[] split;
        Random random = new Random();
        boolean basicMode = PlayerDeathMessages.getInstance().getConfig().getBoolean("Basic-Mode.Enabled");
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = sortList(getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + "." + damageCause), pm.getPlayer(), mob);
        } else {
            msgs = sortList(getPlayerDeathMessages().getStringList(cMode + "." + affiliation + "." + damageCause), pm.getPlayer(), mob);
        }
        if (msgs.isEmpty()) {
            if (config.getBoolean(Config.DEFAULT_NATURAL_DEATH_NOT_DEFINED)) {
                return getNaturalDeath(pm, damageCause);
            }
            if (config.getBoolean(Config.DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED)) {
                return get(gang, pm, mob, getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK));
            }
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob)) + " "));
            tc.addExtra(tx);
            for (BaseComponent bs : tx.getExtra()) {
                if (bs.getColor() != null) {
                    lastColor = bs.getColor().toString();
                }
                lastFont = formatting(bs);
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(playerDeathPlaceholders(sec[1], pm, mob))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + playerDeathPlaceholders(cmd, pm, mob)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + playerDeathPlaceholders(cmd2, pm, mob)));
            }
        }
        return tc;
    }

    @SuppressWarnings({"deprecation"})
    public static TextComponent getProjectile(boolean gang, PlayerManager pm, LivingEntity mob, String projectileDamage) {
        List<String> msgs;
        String firstSection;
        //String[] split;
        ItemStack i;
        String displayName;
        Random random = new Random();
        boolean basicMode = PlayerDeathMessages.getInstance().getConfig().getBoolean("Basic-Mode.Enabled");
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = sortList(getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + "." + projectileDamage), pm.getPlayer(), mob);
        } else {
            msgs = sortList(getPlayerDeathMessages().getStringList(cMode + "." + affiliation + "." + projectileDamage), pm.getPlayer(), mob);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        EntityEquipment equipment = mob.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && (getSettings().getBoolean("Ignore-Projectile-Type") || pm.getLastProjectileEntity() instanceof Arrow) && equipment != null) {
                if (DeathMessages.majorVersion() < 9) {
                    i = equipment.getItemInHand();
                } else {
                    i = equipment.getItemInMainHand();
                }
                if (hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED) && !config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO).equals(projectileDamage)) {
                        return getProjectile(gang, pm, mob, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO));
                    }
                    displayName = getName(i, pm.getPlayer());
                } else {
                    displayName = getCustomName(i, pm.getPlayer());
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = colorize(spl[0]) + ChatColor.RESET + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + ChatColor.RESET + colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = formatting(bs);
                }
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(playerDeathPlaceholders(sec[1], pm, mob))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + playerDeathPlaceholders(cmd, pm, mob)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + playerDeathPlaceholders(cmd2, pm, mob)));
            }
        }
        return tc;
    }

    @SuppressWarnings({"deprecation"})
    public static TextComponent getEntityDeathProjectile(Player p, EntityManager em, String projectileDamage, MobType mobType) {
        List<String> msgs;
        String firstSection;
        //String[] split;
        ItemStack i;
        String displayName;
        Random random = new Random();
        String entityName = classSimple(em.getEntity());
        if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(em.getEntityUUID())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(em.getEntity());
            }
            msgs = sortList(getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + "." + projectileDamage), p, em.getEntity());
        } else {
            msgs = sortList(getEntityDeathMessages().getStringList("Entities." + entityName + "." + projectileDamage), p, em.getEntity());
        }
        if (msgs.isEmpty()) {
            if (config.getBoolean(Config.DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED)) {
                return getEntityDeath(p, em.getEntity(), getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK), mobType);
            }
            return null;
        }
        boolean hasOwner = false;
        Entity entity = em.getEntity();
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.getOwner() != null) {
                hasOwner = true;
            }
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        EntityEquipment equipment = p.getEquipment();
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            if (splitMessage.contains("%weapon%") && (getSettings().getBoolean("Ignore-Projectile-Type") || em.getLastProjectileEntity() instanceof Arrow) && equipment != null) {
                if (DeathMessages.majorVersion() < 9) {
                    i = equipment.getItemInHand();
                } else {
                    i = equipment.getItemInMainHand();
                }
                if (hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED) && !config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO).equals(projectileDamage)) {
                        return getEntityDeathProjectile(p, em, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO), mobType);
                    }
                    displayName = getName(i, p);
                } else {
                    displayName = getCustomName(i, p);
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = colorize(spl[0]) + ChatColor.RESET + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + ChatColor.RESET + colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(entityDeathPlaceholders(lastColor + lastFont + splitMessage, p, em.getEntity(), hasOwner)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = formatting(bs);
                }
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(entityDeathPlaceholders(sec[1], p, em.getEntity(), hasOwner))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + entityDeathPlaceholders(cmd, p, em.getEntity(), hasOwner)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + entityDeathPlaceholders(cmd2, p, em.getEntity(), hasOwner)));
            }
        }
        return tc;
    }

    public static TextComponent getEntityDeath(Player player, Entity entity, String damageCause, MobType mobType) {
        List<String> msgs;
        String firstSection;
        //String[] split;
        Random random = new Random();
        boolean hasOwner = false;
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.getOwner() != null) {
                hasOwner = true;
            }
        }
        if (hasOwner) {
            msgs = sortList(getEntityDeathMessages().getStringList("Entities." + classSimple(entity) + ".Tamed"), player, entity);
        } else if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(entity.getUniqueId())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(entity);
            }
            msgs = sortList(getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + "." + damageCause), player, entity);
        } else {
            msgs = sortList(getEntityDeathMessages().getStringList("Entities." + classSimple(entity) + "." + damageCause), player, entity);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
        }
        String[] sec = msg.split("::");
        if (msg.contains("::")) {
            if (sec.length == 0) {
                firstSection = msg;
            } else {
                firstSection = sec[0];
            }
        } else {
            firstSection = msg;
        }
        String lastColor = "";
        String lastFont = "";
        for (String splitMessage : firstSection.split(" ")) {
            TextComponent tx = new TextComponent(TextComponent.fromLegacyText(colorize(entityDeathPlaceholders(lastColor + lastFont + splitMessage, player, entity, hasOwner)) + " "));
            tc.addExtra(tx);
            for (BaseComponent bs : tx.getExtra()) {
                if (bs.getColor() != null) {
                    lastColor = bs.getColor().toString();
                }
                lastFont = formatting(bs);
            }
        }
        if (sec.length >= 2) {
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(entityDeathPlaceholders(sec[1], player, entity, hasOwner))));
        }
        if (sec.length == 3) {
            if (sec[2].startsWith("COMMAND:")) {
                String cmd = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + entityDeathPlaceholders(cmd, player, entity, hasOwner)));
            } else if (sec[2].startsWith("SUGGEST_COMMAND:")) {
                String cmd2 = sec[2].split(":")[1];
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + entityDeathPlaceholders(cmd2, player, entity, hasOwner)));
            }
        }
        return tc;
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
        if (message == null || message.length() == 0) return message;
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

    public static String entityDeathPlaceholders(String msg, Player player, Entity entity, boolean owner) {
        World world = entity.getWorld();
        Block loc = entity.getLocation().getBlock();
        String msg2 = colorize(msg
                .replace("%entity%", getName(entity, player))
                .replace("%entity_display%", getCustomName(entity, player))
                .replace("%killer%", getName(player))
                .replace("%killer_display%", player.getDisplayName())
                .replace("%world%", world.getName())
                .replace("%world_environment%", getEnvironment(world.getEnvironment()))
                .replace("%x%", String.valueOf(loc.getX()))
                .replace("%y%", String.valueOf(loc.getY()))
                .replace("%z%", String.valueOf(loc.getZ())));
        if (owner && (entity instanceof Tameable)) {
            Tameable tameable = (Tameable) entity;
            if (tameable.getOwner() != null && tameable.getOwner().getName() != null) {
                String tameOwner = tameable.getOwner().getName();
                if (tameable.getOwner() instanceof Player) tameOwner = getName((Player) tameable);
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
            msg2 = colorize(msg
                    .replace("%player%", getName(pm.getPlayer()))
                    .replace("%player_display%", pm.getPlayer().getDisplayName())
                    .replace("%world%", world.getName())
                    .replace("%world_environment%", getEnvironment(world.getEnvironment()))
                    .replace("%x%", String.valueOf(playerLoc.getX()))
                    .replace("%y%", String.valueOf(playerLoc.getY()))
                    .replace("%z%", String.valueOf(playerLoc.getZ())));
            try {
                msg2 = msg2.replace("%biome%", playerLoc.getBiome().name());
            } catch (NullPointerException e) {
                DeathMessages.getInstance().getLogger().severe("Custom Biome detected. Using 'Unknown' for a biome name.");
                DeathMessages.getInstance().getLogger().severe("Custom Biomes are not supported yet.'");
                msg2 = msg2.replace("%biome%", "Unknown");
            }
        } else {
            String mobName = getCustomName(mob, pm.getPlayer());
            if (config.getBoolean(Config.RENAME_MOBS_ENABLED)) {
                String[] chars = config.getString(Config.RENAME_MOBS_IF_CONTAINS).split("(?!^)");
                int length = chars.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    String ch = chars[i];
                    if (!mobName.contains(ch)) {
                        i++;
                    } else {
                        mobName = getName(mob, pm.getPlayer());
                        break;
                    }
                }
            }
            if (!(mob instanceof Player) && config.getBoolean(Config.DISABLE_NAMED_MOBS)) {
                mobName = getName(mob, pm.getPlayer());
            }
            Block playerLoc = pm.getLastLocation().getBlock();
            World world = playerLoc.getWorld();
            msg2 = msg.replace("%player%", getName(pm.getPlayer()))
                    .replace("%player_display%", pm.getPlayer().getDisplayName())
                    .replace("%killer%", mobName)
                    .replace("%killer_type%", getName(mob, pm.getPlayer()))
                    .replace("%world%", world.getName())
                    .replace("%world_environment%", getEnvironment(world.getEnvironment()))
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
                Player p = (Player) mob;
                msg2 = msg2.replace("%killer_display%", p.getDisplayName());
            }
        }
        if (DeathMessages.getInstance().placeholderAPIEnabled) {
            msg2 = PlaceholderAPI.setPlaceholders(pm.getPlayer(), msg2);
        }
        return msg2;
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
                return Messages.getInstance().getConfig().getString("Environment.normal");
            case NETHER:
                return Messages.getInstance().getConfig().getString("Environment.nether");
            case THE_END:
                return Messages.getInstance().getConfig().getString("Environment.the_end");
            default:
                return Messages.getInstance().getConfig().getString("Environment.unknown");
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

    public static FileConfiguration getSettings() {
        return Settings.getInstance().getConfig();
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
        if (meta == null) return getName(item, player);
        if (!meta.hasDisplayName()) return getName(item, player);
        return meta.getDisplayName();
    }

    public static String getName(ItemStack item, Player player) {
        if (DeathMessages.langUtilsEnabled) {
            return LanguageHelper.getItemDisplayName(item, player);
        } else {
            return convertString(item.getType().name());
        }
    }

    public static String getName(Player player) {
        String msg = Messages.getInstance().getConfig().getString("PlayerName", "%player_name%");
        if (DeathMessages.getInstance().placeholderAPIEnabled) {
            msg = PlaceholderAPI.setPlaceholders(player,  msg);
        } else {
            msg = msg.replace("%player_name%", player.getName());
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String getCustomName(Entity entity, Player player) {
        if (entity instanceof Player) return getName((Player) entity);
        String customName = entity.getCustomName();
        if (customName == null) return getName(entity, player);
        return customName;
    }

    public static String getName(Entity entity, Player player) {
        if (entity instanceof Player) return getName((Player) entity);
        if (DeathMessages.langUtilsEnabled) {
            return LanguageHelper.getEntityName(entity, player);
        }
        return Messages.getInstance().getConfig().getString("Mobs." + entity.getType().name().toLowerCase(), entity.getName());
    }

    @Nullable
    @SuppressWarnings({"deprecation"})
    public static String getBlockName(String splitMessage, FallingBlock block, Player player) {
        if (block == null) return null;
        Material mat;
        if (DeathMessages.majorVersion() < 13) {
            mat = XMaterial.matchXMaterial(block.getMaterial()).parseMaterial();
            if (mat == null) return null;
        } else {
            mat = XMaterial.matchXMaterial(block.getBlockData().getMaterial()).parseMaterial();
            if (mat == null) return null;
        }
        return getBlockName(splitMessage, mat, player);
    }

    public static String getBlockName(String splitMessage, Material mat, Player player) {
        if (mat == null) return null;
        String configValue2;
        if (DeathMessages.langUtilsEnabled) {
            configValue2 = LanguageHelper.getItemName(new ItemStack(mat), player);
        } else {
            String material2 = mat.toString().toLowerCase();
            configValue2 = Messages.getInstance().getConfig().getString("Blocks." + material2);
        }
        return colorize(splitMessage.replace("%climbable%", configValue2 + (splitMessage.endsWith(".") ? "" : " ")));
    }

    public static String classSimple(Entity entity) {
        Class<?> clazz = entity.getType().getEntityClass();
        if (clazz != null) return clazz.getSimpleName().toLowerCase();
        return entity.getClass().getSimpleName().toLowerCase();
    }
}
