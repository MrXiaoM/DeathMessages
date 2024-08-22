package dev.mrshawn.deathmessages.files;

import com.google.common.collect.Lists;

public enum Config implements ConfigEnum {
    DISABLE_DEFAULT_MESSAGES("Disable-Default-Messages", true),
    ADD_PREFIX_TO_ALL_MESSAGES("Add-Prefix-To-All-Messages", true),
    HOOKS_MYTHICMOBS_ENABLED("Hooks.MythicMobs.Enabled", true),
    HOOKS_LANGUTILS_ENABLED("Hooks.LangUtils.Enabled", true),
    HOOKS_WORLDGUARD_ENABLED("Hooks.WorldGuard.Enabled", true),
    HOOKS_BUNGEE_ENABLED("Hooks.Bungee.Enabled", false),
    HOOKS_BUNGEE_SERVER_NAME_GET_FROM_BUNGEE("Hooks.Bungee.Server-Name.Get-From-Bungee", false),
    HOOKS_BUNGEE_SERVER_NAME_DISPLAY_NAME("Hooks.Bungee.Server-Name.Display-Name", "lobby"),
    HOOKS_BUNGEE_SERVER_GROUPS_ENABLED("Hooks.Bungee.Server-Groups.Enabled", false),
    HOOKS_BUNGEE_SERVER_GROUPS_SERVERS("Hooks.Bungee.Server-Groups.Servers", Lists.newArrayList("lobby", "survival")),
    SAVED_USER_DATA("Saved-User-Data", true),
    DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_ENABLED("Disable-Weapon-Kill-With-No-Custom-Name.Enabled", false),
    DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_IGNORE_ENCHANTMENTS("Disable-Weapon-Kill-With-No-Custom-Name.Ignore-Enchantments", true),
    DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_PROJECTILE_DEFAULT_TO("Disable-Weapon-Kill-With-No-Custom-Name.Source.Projectile.Default-To", "Projectile-Unknown"),
    DISABLE_WEAPON_KILL_WITH_NO_CUSTOM_NAME_SOURCE_WEAPON_DEFAULT_TO("Disable-Weapon-Kill-With-No-Custom-Name.Source.Weapon.Default-To", "Melee"),
    DEFAULT_MELEE_LAST_DAMAGE_NOT_DEFINED("Default-Melee-Last-Damage-Not-Defined", true),
    DEFAULT_NATURAL_DEATH_NOT_DEFINED("Default-Natural-Death-Not-Defined", true),
    DEATH_LISTENER_PRIORITY("Death-Listener-Priority", "HIGH"),
    RENAME_MOBS_ENABLED("Rename-Mobs.Enabled", true),
    RENAME_MOBS_IF_CONTAINS("Rename-Mobs.If-Contains", "♡♥❤■"),
    DISABLE_NAMED_MOBS("Disable-Named-Mobs", false),
    EXPIRE_LAST_DAMAGE_EXPIRE_PLAYER("Expire-Last-Damage.Expire-Player", 7),
    EXPIRE_LAST_DAMAGE_EXPIRE_ENTITY("Expire-Last-Damage.Expire-Entity", 7),
    PER_WORLD_MESSAGES("Per-World-Messages", false),
    DISABLED_WORLDS("Disabled-Worlds", Lists.newArrayList("someDisabledWorld", "someOtherDisabledWorld")),
    PRIVATE_MESSAGES_PLAYER("Private-Messages.Player", false),
    PRIVATE_MESSAGES_MOBS("Private-Messages.Mobs", false),
    PRIVATE_MESSAGES_NATURAL("Private-Messages.Natural", false),
    COOLDOWN("Cooldown", 0),
    CUSTOM_ITEM_DISPLAY_NAMES_IS_WEAPON("Custom-Item-Display-Names-Is-Weapon", Lists.newArrayList("&6SUPER COOL GOLDEN APPLE", "SICKNAME")),
    CUSTOM_ITEM_MATERIAL_IS_WEAPON("Custom-Item-Material-Is-Weapon", Lists.newArrayList("ACACIA_FENCE"));

    private final String path;
    private final Object defaultValue;

    Config(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public Object getDefault() {
        return this.defaultValue;
    }
}
