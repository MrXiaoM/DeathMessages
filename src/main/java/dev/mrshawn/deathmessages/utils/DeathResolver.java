package dev.mrshawn.deathmessages.utils;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.nbtapi.NBTItem;
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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathResolver {
    private static final FileSettings<Config> config = FileSettings.CONFIG;
    private static final boolean addPrefix = config.getBoolean(Config.ADD_PREFIX_TO_ALL_MESSAGES);

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
            Pattern pattern = Pattern.compile(Messages.colorize(s));
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
            return get(gang, pm, mob, Messages.getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION));
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
                return getProjectile(gang, pm, mob, Messages.getSimpleProjectile(pm.getLastProjectileEntity()));
            }
            return get(gang, pm, mob, Messages.getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        }
        for (EntityDamageEvent.DamageCause dc : EntityDamageEvent.DamageCause.values()) {
            if (pm.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                return getProjectile(gang, pm, mob, Messages.getSimpleProjectile(pm.getLastProjectileEntity()));
            }
            if (pm.getLastDamage().equals(dc)) {
                return get(gang, pm, mob, Messages.getSimpleCause(dc));
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
            return getEntityDeath(p, em.getEntity(), Messages.getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION), mobType);
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
                return getEntityDeathProjectile(p, em, Messages.getSimpleProjectile(em.getLastProjectileEntity()), mobType);
            }
            return getEntityDeathWeapon(p, em.getEntity(), mobType);
        }
        for (EntityDamageEvent.DamageCause dc : EntityDamageEvent.DamageCause.values()) {
            if (em.getLastDamage().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                return getEntityDeathProjectile(p, em, Messages.getSimpleProjectile(em.getLastProjectileEntity()), mobType);
            }
            if (em.getLastDamage().equals(dc)) {
                return getEntityDeath(p, em.getEntity(), Messages.getSimpleCause(dc), mobType);
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
        List<String> msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList("Natural-Cause." + damageCause), pm.getPlayer(), pm.getPlayer());
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
                String mssa = Messages.getBlockName(splitMessage, fb, pm.getPlayer());
                if (mssa == null) {
                    DeathMessages.getInstance().getLogger().severe("Could not parse %block%. Please check your config for a wrong value. Your materials could be spelt wrong or it does not exists in the config.");
                    pm.setLastEntityDamager(null);
                    return getNaturalDeath(pm, Messages.getSimpleCause(EntityDamageEvent.DamageCause.SUFFOCATION));
                }
                tc.addExtra(mssa);
                lastColor = Messages.getColorOfString(lastColor + mssa);
            } else if (splitMessage.contains("%climbable%") && pm.getLastDamage().equals(EntityDamageEvent.DamageCause.FALL)) {
                Material mat = XMaterial.matchXMaterial(pm.getLastClimbing()).parseMaterial();
                String mssa2 = Messages.getBlockName(splitMessage, mat, pm.getPlayer());
                if (mssa2 == null) {
                    DeathMessages.getInstance().getLogger().severe("Could not parse %climbable%. Please check your config for a wrong value. Your materials could be spelt wrong or it does not exists in the config.");
                    pm.setLastClimbing(null);
                    return getNaturalDeath(pm, Messages.getSimpleCause(EntityDamageEvent.DamageCause.FALL));
                }
                tc.addExtra(mssa2);
                lastColor = Messages.getColorOfString(lastColor + mssa2);
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
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().size() == 0) {
                                return getNaturalDeath(pm, "Projectile-Unknown");
                            }
                        } else {
                            return getNaturalDeath(pm, "Projectile-Unknown");
                        }
                    }
                    displayName = Messages.getItemName(i, pm.getPlayer());
                } else {
                    displayName = Messages.getCustomName(i, pm.getPlayer());
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = Messages.colorize(spl[0]) + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + Messages.colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);

            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, null)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = Messages.formatting(bs);
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
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + Messages.classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + ".Weapon"), pm.getPlayer(), mob);
        } else {
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList(cMode + "." + affiliation + ".Weapon"), pm.getPlayer(), mob);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
                if (Messages.hasNoCustomName(i)) {
                    if (FileSettings.CONFIG.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!FileSettings.CONFIG.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().size() == 0) {
                                return get(gang, pm, mob, FileSettings.CONFIG.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO));
                            }
                        } else {
                            return get(gang, pm, mob, FileSettings.CONFIG.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO));
                        }
                    }
                    displayName = Messages.getItemName(i, pm.getPlayer());
                } else {
                    displayName = Messages.getCustomName(i, pm.getPlayer());
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = Messages.colorize(spl[0]) + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + Messages.colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = Messages.formatting(bs);
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
        String entityName = Messages.classSimple(e);
        if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(e.getUniqueId())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(e);
            }
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + ".Weapon"), p, e);
        } else {
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + entityName + ".Weapon"), p, e);
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
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED)) {
                        if (!config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS)) {
                            if (i.getEnchantments().size() == 0) {
                                return getEntityDeath(p, e, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO), mobType);
                            }
                        } else {
                            return getEntityDeath(p, e, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO), mobType);
                        }
                    }
                    displayName = Messages.getItemName(i, p);
                } else {
                    displayName = Messages.getCustomName(i, p);
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = Messages.colorize(spl[0]) + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + Messages.colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(entityDeathPlaceholders(lastColor + lastFont + splitMessage, p, e, hasOwner)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = Messages.formatting(bs);
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
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + Messages.classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + "." + damageCause), pm.getPlayer(), mob);
        } else {
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList(cMode + "." + affiliation + "." + damageCause), pm.getPlayer(), mob);
        }
        if (msgs.isEmpty()) {
            if (config.getBoolean(Config.DEFAULT_NATURAL_DEATH_NOT_DEFINED)) {
                return getNaturalDeath(pm, damageCause);
            }
            if (config.getBoolean(Config.DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED)) {
                return get(gang, pm, mob, Messages.getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK));
            }
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
            TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob)) + " "));
            tc.addExtra(tx);
            for (BaseComponent bs : tx.getExtra()) {
                if (bs.getColor() != null) {
                    lastColor = bs.getColor().toString();
                }
                lastFont = Messages.formatting(bs);
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
        String cMode = basicMode ? PDMode.BASIC_MODE.getValue() : PDMode.MOBS.getValue() + "." + Messages.classSimple(mob);
        String affiliation = gang ? DeathAffiliation.GANG.getValue() : DeathAffiliation.SOLO.getValue();
        if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(mob.getUniqueId())) {
            String internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(mob);
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList("Custom-Mobs.Mythic-Mobs." + internalMobType + "." + affiliation + "." + projectileDamage), pm.getPlayer(), mob);
        } else {
            msgs = Messages.sortList(Messages.getPlayerDeathMessages().getStringList(cMode + "." + affiliation + "." + projectileDamage), pm.getPlayer(), mob);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED) && !config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO).equals(projectileDamage)) {
                        return getProjectile(gang, pm, mob, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO));
                    }
                    displayName = Messages.getItemName(i, pm.getPlayer());
                } else {
                    displayName = Messages.getCustomName(i, pm.getPlayer());
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = Messages.colorize(spl[0]) + ChatColor.RESET + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + ChatColor.RESET + Messages.colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(playerDeathPlaceholders(lastColor + lastFont + splitMessage, pm, mob)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = Messages.formatting(bs);
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
        String entityName = Messages.classSimple(em.getEntity());
        if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(em.getEntityUUID())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(em.getEntity());
            }
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + "." + projectileDamage), p, em.getEntity());
        } else {
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + entityName + "." + projectileDamage), p, em.getEntity());
        }
        if (msgs.isEmpty()) {
            if (config.getBoolean(Config.DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED)) {
                return getEntityDeath(p, em.getEntity(), Messages.getSimpleCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK), mobType);
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
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
                if (Messages.hasNoCustomName(i)) {
                    if (config.getBoolean(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED) && !config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO).equals(projectileDamage)) {
                        return getEntityDeathProjectile(p, em, config.getString(Config.DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO), mobType);
                    }
                    displayName = Messages.getItemName(i, p);
                } else {
                    displayName = Messages.getCustomName(i, p);
                }
                String[] spl = splitMessage.split("%weapon%");
                if (spl.length != 0 && spl[0] != null && !spl[0].equals("")) {
                    displayName = Messages.colorize(spl[0]) + ChatColor.RESET + displayName;
                }
                if (spl.length != 0 && spl.length != 1 && spl[1] != null && !spl[1].equals("")) {
                    displayName = displayName + ChatColor.RESET + Messages.colorize(spl[1]);
                }
                TextComponent weaponComp = new TextComponent(TextComponent.fromLegacyText(displayName));
                BaseComponent[] hoverEventComponents = {new TextComponent(NBTItem.convertItemtoNBT(i).getCompound().toString())};
                weaponComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
                tc.addExtra(weaponComp);
            } else {
                TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(entityDeathPlaceholders(lastColor + lastFont + splitMessage, p, em.getEntity(), hasOwner)) + " "));
                tc.addExtra(tx);
                for (BaseComponent bs : tx.getExtra()) {
                    if (bs.getColor() != null) {
                        lastColor = bs.getColor().toString();
                    }
                    lastFont = Messages.formatting(bs);
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
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + Messages.classSimple(entity) + ".Tamed"), player, entity);
        } else if (mobType.equals(MobType.MYTHIC_MOB)) {
            String internalMobType = null;
            if (DeathMessages.getInstance().mythicmobsEnabled && DeathMessages.getInstance().mythicMobs.isMythicMob(entity.getUniqueId())) {
                internalMobType = DeathMessages.getInstance().mythicMobs.getMobType(entity);
            }
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Mythic-Mobs-Entities." + internalMobType + "." + damageCause), player, entity);
        } else {
            msgs = Messages.sortList(Messages.getEntityDeathMessages().getStringList("Entities." + Messages.classSimple(entity) + "." + damageCause), player, entity);
        }
        if (msgs.isEmpty()) {
            return null;
        }
        String msg = msgs.get(random.nextInt(msgs.size()));
        TextComponent tc = new TextComponent("");
        if (addPrefix) {
            tc.addExtra(new TextComponent(TextComponent.fromLegacyText(Messages.colorize(Messages.getInstance().getConfig().getString("Prefix", "")))));
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
            TextComponent tx = new TextComponent(TextComponent.fromLegacyText(Messages.colorize(entityDeathPlaceholders(lastColor + lastFont + splitMessage, player, entity, hasOwner)) + " "));
            tc.addExtra(tx);
            for (BaseComponent bs : tx.getExtra()) {
                if (bs.getColor() != null) {
                    lastColor = bs.getColor().toString();
                }
                lastFont = Messages.formatting(bs);
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
            msg2 = Messages.colorize(msg
                    .replace("%player%", Messages.getPlayerNameWithPlaceholder(pm.getPlayer()))
                    .replace("%player_display%", pm.getPlayer().getDisplayName())
                    .replace("%world%", world.getName())
                    .replace("%world_environment%", Messages.getEnvironment(world.getEnvironment()))
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
            String mobName = Messages.getCustomName(mob, pm.getPlayer());
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
                Player p = (Player) mob;
                msg2 = msg2.replace("%killer_display%", p.getDisplayName());
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

}
