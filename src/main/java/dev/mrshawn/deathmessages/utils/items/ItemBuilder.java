package dev.mrshawn.deathmessages.utils.items;

import dev.mrshawn.deathmessages.utils.messages.Chat;
import org.bukkit.Material;
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
import java.util.function.Predicate;

public final class ItemBuilder {
    @NotNull
    private final ItemStack item;
    @Nullable
    private final ItemMeta meta;
    @NotNull
    private final List<String> lore;

    public ItemBuilder(@NotNull Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = this.item.getItemMeta();
        this.lore = new ArrayList<>();
    }


    @NotNull
    public static ItemStack glow(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        if (meta != null) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public ItemBuilder name(@NotNull String name) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta != null) {
            itemMeta.setDisplayName(Chat.INSTANCE.colorize(name));
        }
        return this;
    }

    @NotNull
    public ItemBuilder setNoName() {
        name(" ");
        return this;
    }

    @NotNull
    public ItemBuilder addLoreLine(@NotNull String line) {
        this.lore.add(Chat.INSTANCE.colorize(line));
        return this;
    }

    @NotNull
    public ItemBuilder addLoreLines(@NotNull String[] lines) {
        List<String> colored = new ArrayList<>();
        for (String str : lines) {
            colored.add(Chat.INSTANCE.colorize(str));
        }
        this.lore.addAll(colored);
        return this;
    }

    @NotNull
    public ItemBuilder addLoreLines(@NotNull List<String> list) {
        for (String line : list) {
            lore.add(Chat.INSTANCE.colorize(line));
        }
        return this;
    }

    @NotNull
    public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    @NotNull
    public ItemBuilder glow() {
        ItemMeta itemMeta = this.meta;
        if (itemMeta != null && !itemMeta.hasEnchants()) {
            hideAttributes();
            Enchantment enchantment = Enchantment.DURABILITY;
            addEnchantment(enchantment, 1);
        }
        return this;
    }

    @NotNull
    public ItemBuilder glowIf(@NotNull Predicate<ItemBuilder> function0) {
        if (function0.test(this)) {
            glow();
        }
        return this;
    }

    @NotNull
    public ItemBuilder addData(@NotNull NamespacedKey key, @NotNull String value) {
        ItemMeta itemMeta = this.meta;
        PersistentDataContainer container = itemMeta != null ? itemMeta.getPersistentDataContainer() : null;
        if (container != null) {
            container.set(key, PersistentDataType.STRING, value);
        }
        return this;
    }

    @NotNull
    public ItemBuilder addData(@NotNull NamespacedKey key, int value) {
        ItemMeta itemMeta = this.meta;
        PersistentDataContainer container = itemMeta != null ? itemMeta.getPersistentDataContainer() : null;
        if (container != null) {
            container.set(key, PersistentDataType.INTEGER, value);
        }
        return this;
    }

    @NotNull
    public ItemBuilder setCustomModelData(int data) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta != null) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    @NotNull
    public ItemBuilder addItemFlag(@NotNull ItemFlag flag) {
        ItemMeta itemMeta = this.meta;
        if (itemMeta != null) {
            itemMeta.addItemFlags(flag);
        }
        return this;
    }

    @NotNull
    public ItemBuilder hideAttributes() {
        return addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
    }

    @NotNull
    public ItemStack build() {
        ItemMeta itemMeta = this.meta;
        if (itemMeta != null) {
            itemMeta.setLore(this.lore);
        }
        this.item.setItemMeta(this.meta);
        return this.item;
    }
}
