package dev.mrshawn.deathmessages.utils;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTReflectionUtil;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

public class HoverShowItemResolver {

    private static NBTCompound getComponent(NBTCompound nbt, String key) {
        return nbt.hasTag(key) ? nbt.getCompound(key) : null;
    }
    @ApiStatus.Experimental
    public static HoverEvent toHoverEvent(ItemStack item) {
        Object nmsItem = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, item);
        NBTContainer nbt = NBTReflectionUtil.convertNMSItemtoNBTCompound(nmsItem);
        NBTCompound components = getComponent(nbt, "components");
        NBTCompound tag = getComponent(nbt, "tag");

        ItemTag itemTag;
        if (components != null) { // 1.21.5+
            // TODO: Paper 上的 BungeeCord chat api 不支持新版本格式，应使用 adventure 接口替代
            itemTag = ItemTag.ofNbt(components.toString());
        } else if (tag != null) { // 1.7-1.21.4
            itemTag = ItemTag.ofNbt(tag.toString());
        } else { // 未知格式
            itemTag = ItemTag.ofNbt("{}");
        }

        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(
                nbt.getString("id"),
                nbt.getInteger("count"),
                itemTag
        ));
    }

}
