package dev.mrshawn.deathmessages.utils.items;

import dev.mrshawn.deathmessages.utils.messages.Chat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ItemEditor {
    @NotNull
    public static final ItemEditor INSTANCE = new ItemEditor();

    private ItemEditor() {
    }

    private ItemMeta getItemMeta(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return Bukkit.getItemFactory().getItemMeta(item.getType());
        }
        return itemMeta;
    }

    @NotNull
    public ItemStack rename(@NotNull ItemStack item, @NotNull String name) {
        ItemMeta meta = getItemMeta(item);
        meta.setDisplayName(Chat.INSTANCE.colorize(name));
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public ItemStack relore(@NotNull ItemStack item, @NotNull String[] lore) {
        ItemMeta meta = getItemMeta(item);
        List<String> destination$iv$iv = new ArrayList<>();
        for (String str : lore) {
            destination$iv$iv.add(Chat.INSTANCE.colorize(str));
        }
        meta.setLore(destination$iv$iv);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public ItemStack amount(@NotNull ItemStack item, int amount) {
        item.setAmount(amount);
        return item;
    }

    @NotNull
    public ItemStack glow(@NotNull ItemStack item) {
        ItemMeta meta = getItemMeta(item);
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public ItemStack unglow(@NotNull ItemStack item) {
        ItemMeta meta = getItemMeta(item);
        meta.removeEnchant(Enchantment.DURABILITY);
        meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    @Nullable
    public String getData(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        return getItemMeta(item).getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    @Nullable
    public Set<NamespacedKey> getAllKeys(@NotNull ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            return persistentDataContainer.getKeys();
        }
        return null;
    }
}
