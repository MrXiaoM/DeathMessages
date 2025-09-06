package dev.mrshawn.deathmessages.utils;

import de.tr7zw.nbtapi.utils.MinecraftVersion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.HoverEventSource;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ComponentUtils {
    private static boolean is1_20_5;
    private static boolean supportAdventure;
    private static boolean useAdventure;
    private static boolean isItemSupportHoverSource;
    public static void init() {
        try {
            supportAdventure = Bukkit.getConsoleSender() instanceof Audience;
        } catch (Throwable ignored) {
            supportAdventure = false;
        }
        try {
            is1_20_5 = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4);
        } catch (Throwable ignored) {
            is1_20_5 = false;
        }
        try {
            isItemSupportHoverSource = new ItemStack(Material.STONE) instanceof HoverEventSource;
        } catch (Throwable ignored) {
            isItemSupportHoverSource = false;
        }
    }

    public static boolean is1_20_5() {
        return is1_20_5;
    }

    public static boolean isUseAdventure() {
        return useAdventure;
    }

    public static boolean isItemSupportHoverSource() {
        return isItemSupportHoverSource;
    }

    public static void setUseAdventure(boolean useAdventure) {
        ComponentUtils.useAdventure = useAdventure;
    }

    public static <T extends BaseComponent> T scanAndInheritStyle(T message) {
        scanAndInheritStyle(null, message);
        return message;
    }

    public static void scanAndInheritStyle(BaseComponent last, BaseComponent message) {
        inherit(message, last);
        List<BaseComponent> extra = message.getExtra();
        if (extra != null) for (BaseComponent baseComponent : extra) {
            scanAndInheritStyle(last, baseComponent);
            last = baseComponent;
        }
    }

    public static void applyStyle(BaseComponent builder, BaseComponent component) {
        if (component.getColorRaw() != null) {
            builder.setColor(component.getColorRaw());
        }
        try {
            if (component.getShadowColorRaw() != null) {
                builder.setShadowColor(component.getShadowColorRaw());
            }
        } catch (LinkageError ignored) {
        }
        if (component.isBold()) builder.setBold(component.isBold());
        if (component.isItalic()) builder.setItalic(component.isItalic());
        if (component.isUnderlined()) builder.setUnderlined(component.isUnderlined());
        if (component.isStrikethrough()) builder.setStrikethrough(component.isStrikethrough());
        if (component.isObfuscated()) builder.setObfuscated(component.isObfuscated());
        if (component.getInsertion() != null) builder.setInsertion(component.getInsertion());
        if (component.getFontRaw() != null) builder.setFont(component.getFontRaw());
        if (component.getHoverEvent() != null) builder.setHoverEvent(component.getHoverEvent());
        if (component.getClickEvent() != null) builder.setClickEvent(component.getClickEvent());
        List<BaseComponent> extra = component.getExtra();
        if (extra != null) {
            for (BaseComponent baseComponent : extra) {
                applyStyle(builder, baseComponent);
            }
        }
    }

    public static void inherit(BaseComponent component, BaseComponent last) {
        if (component instanceof TranslatableComponent) {
            if (last != null) {
                applyStyle(component, last);
            }
        }
    }

    public static void send(CommandSender sender, BaseComponent... message) {
        if (supportAdventure && useAdventure) {
            // Paper 从 1.16 开始内置 adventure api
            if (AdventureUtils.send(sender, message)) return;
        }

        // 兼容 1.8
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(message);
            return;
        }
        try {
            sender.spigot().sendMessage(message);
        } catch (Throwable t) {
            TextComponent text = new TextComponent(message);
            sender.sendMessage(text.toLegacyText());
        }
    }
}
