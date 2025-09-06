package dev.mrshawn.deathmessages.utils;

import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class AdventureUtils {
    private static class A {
        public static Component shadowColor(Component base, java.awt.Color color) {
            return base.shadowColor(net.kyori.adventure.text.format.ShadowColor.shadowColor(color.getAlpha(), color.getRed(), color.getRed(), color.getBlue()));
        }
    }
    public static class Builder {
        private Component base;
        public Builder(Component component) {
            this.base = component;
        }

        public void append(Component component) {
            base = base.append(component);
        }

        public void color(TextColor color) {
            base = base.color(color);
        }

        public void shadowColor(java.awt.Color color) {
            base = A.shadowColor(base, color);
        }

        public void decorate(TextDecoration decoration) {
            base = base.decorate(decoration);
        }

        public void font(Key key) {
            base = base.font(key);
        }

        public void insertion(String insertion) {
            if (insertion != null) {
                base = base.insertion(insertion);
            }
        }

        public void clickEvent(net.md_5.bungee.api.chat.ClickEvent clickEvent) {
            if (clickEvent != null) {
                String value = clickEvent.getValue();
                switch (clickEvent.getAction()) {
                    case OPEN_URL: base = base.clickEvent(ClickEvent.openUrl(value));break;
                    case OPEN_FILE: base = base.clickEvent(ClickEvent.openFile(value));break;
                    case CHANGE_PAGE:
                        try {
                            base = base.clickEvent(ClickEvent.changePage(Integer.parseInt(value)));
                        } catch (NumberFormatException ignored) {
                        }
                        break;
                    case RUN_COMMAND: base = base.clickEvent(ClickEvent.runCommand(value));break;
                    case SUGGEST_COMMAND: base = base.clickEvent(ClickEvent.suggestCommand(value));break;
                    case COPY_TO_CLIPBOARD: base = base.clickEvent(ClickEvent.copyToClipboard(value));break;
                }
            }
        }

        public void hoverEvent(net.md_5.bungee.api.chat.HoverEvent hoverEvent) {
            if (hoverEvent != null) {
                if (hoverEvent.isLegacy()) {
                    Component component = convert((BaseComponent[]) ((Text) hoverEvent.getContents().get(0)).getValue());
                    base = base.hoverEvent(component.asHoverEvent());
                } else if (!hoverEvent.getContents().isEmpty()) {
                    switch (hoverEvent.getAction()) {
                        case SHOW_ITEM: {
                            Item impl = (Item) hoverEvent.getContents().get(0);
                            // 如果服务端是 Paper 衍生端，支持将物品视为 HoverEventSource
                            if (ComponentUtils.isItemSupportHoverSource()) {
                                // 新建一个物品，通过 NBT-API 还原 NMS 物品
                                Object nmsItem = ReflectionMethod.ITEMSTACK_NMSCOPY.run(null, new ItemStack(Material.STONE));
                                NBTContainer nbt = NBTReflectionUtil.convertNMSItemtoNBTCompound(nmsItem);
                                nbt.setString("id", impl.getId());
                                nbt.setInteger("count", Math.max(1, impl.getCount()));
                                if (ComponentUtils.is1_20_5()) {
                                    // 1.20.5+ 使用物品堆叠组件
                                    NBTCompound components = nbt.getOrCreateCompound("components");
                                    components.mergeCompound(NBT.parseNBT(impl.getTag().getNbt()));
                                } else {
                                    // 低版本使用 NBT
                                    NBTCompound tag = nbt.getOrCreateCompound("tag");
                                    tag.mergeCompound(NBT.parseNBT(impl.getTag().getNbt()));
                                }
                                Object newItem = NBTReflectionUtil.convertNBTCompoundtoNMSItem(nbt);
                                // 转换为 BukkitAPI 的 ItemStack，并应用到文本组件
                                Object hover = ReflectionMethod.ITEMSTACK_BUKKITMIRROR.run(null, newItem);
                                if (hover instanceof HoverEventSource) {
                                    base = base.hoverEvent((HoverEventSource<?>) hover);
                                    break;
                                }
                            }
                            // 旧版通用方法
                            Key key = Key.key(impl.getId(), ':');
                            int count = impl.getCount();
                            BinaryTagHolder nbt = BinaryTagHolder.binaryTagHolder(impl.getTag().getNbt());
                            base = base.hoverEvent(HoverEvent.showItem(key, count, nbt));
                            break;
                        }
                        case SHOW_TEXT: {
                            for (Content content : hoverEvent.getContents()) {
                                Text impl = (Text) content;
                                Object value = impl.getValue();
                                if (value instanceof BaseComponent[]) {
                                    base = base.hoverEvent(HoverEvent.showText(convert((BaseComponent[]) value)));
                                } else if (value instanceof BaseComponent) {
                                    Component convert = convert((BaseComponent) value);
                                    if (convert != null) {
                                        base = base.hoverEvent(HoverEvent.showText(convert));
                                    }
                                }
                            }
                            break;
                        }
                        case SHOW_ENTITY: {
                            Entity impl = (Entity) hoverEvent.getContents().get(0);
                            Key type = Key.key(impl.getType(), ':');
                            UUID id = UUID.fromString(impl.getId());
                            Component name = convert(impl.getName());
                            if (name != null) {
                                base = base.hoverEvent(HoverEvent.showEntity(type, id, name));
                            } else {
                                base = base.hoverEvent(HoverEvent.showEntity(type, id));
                            }
                            break;
                        }
                    }
                }
            }
        }

        public Component build() {
            return base;
        }
    }

    public static boolean send(CommandSender sender, BaseComponent... message) {
        if (!(sender instanceof Audience)) return false;
        Audience audience = (Audience) sender;
        audience.sendMessage(convert(message));
        return true;
    }

    public static Component convert(BaseComponent[] message) {
        TextComponent builder = Component.empty();
        for (BaseComponent component : message) {
            Component convert = convert(component);
            if (convert != null) {
                builder = builder.append(convert);
            }
        }
        return builder;
    }

    @Nullable
    public static Component convert(BaseComponent bungee) {
        Builder builder = convert0(bungee);
        if (builder == null) return null;
        if (bungee.getColorRaw() != null) {
            TextColor color = bungee.getColorRaw().getName().startsWith("#")
                    ? TextColor.fromHexString(bungee.getColorRaw().getName())
                    : NamedTextColor.NAMES.value(bungee.getColorRaw().getName());
            if (color != null) {
                builder.color(color);
            }
        }
        try {
            Class.forName("net.kyori.adventure.util.ARGBLike");
            if (bungee.getShadowColorRaw() != null) {
                builder.shadowColor(bungee.getShadowColorRaw());
            }
        } catch (LinkageError | ReflectiveOperationException ignored) {
        }
        if (bungee.isBold()) builder.decorate(TextDecoration.BOLD);
        if (bungee.isItalic()) builder.decorate(TextDecoration.ITALIC);
        if (bungee.isStrikethrough()) builder.decorate(TextDecoration.STRIKETHROUGH);
        if (bungee.isUnderlined()) builder.decorate(TextDecoration.UNDERLINED);
        if (bungee.isObfuscated()) builder.decorate(TextDecoration.OBFUSCATED);
        if (bungee.getFontRaw() != null) builder.font(Key.key(bungee.getFontRaw(), ':'));

        builder.insertion(bungee.getInsertion());
        builder.clickEvent(bungee.getClickEvent());
        builder.hoverEvent(bungee.getHoverEvent());

        List<BaseComponent> extra = bungee.getExtra();
        if (extra != null) for (BaseComponent baseComponent : extra) {
            Component convert = convert(baseComponent);
            if (convert != null) {
                builder.append(convert);
            }
        }
        return builder.build();
    }

    private static Builder convert0(BaseComponent bungee) {
        if (bungee instanceof net.md_5.bungee.api.chat.KeybindComponent) {
            net.md_5.bungee.api.chat.KeybindComponent impl = (net.md_5.bungee.api.chat.KeybindComponent) bungee;
            return new Builder(Component.keybind(impl.getKeybind()));
        }
        if (bungee instanceof net.md_5.bungee.api.chat.ScoreComponent) {
            net.md_5.bungee.api.chat.ScoreComponent impl = (net.md_5.bungee.api.chat.ScoreComponent) bungee;
            return new Builder(Component.score(impl.getName(), impl.getObjective()));
        }
        if (bungee instanceof net.md_5.bungee.api.chat.SelectorComponent) {
            net.md_5.bungee.api.chat.SelectorComponent impl = (net.md_5.bungee.api.chat.SelectorComponent) bungee;
            return new Builder(Component.selector(impl.getSelector()));
        }
        if (bungee instanceof net.md_5.bungee.api.chat.TranslatableComponent) {
            net.md_5.bungee.api.chat.TranslatableComponent impl = (net.md_5.bungee.api.chat.TranslatableComponent) bungee;
            try {
                return new Builder(Component.translatable(impl.getTranslate(), impl.getFallback()));
            } catch (Throwable t) {
                return new Builder(Component.translatable(impl.getTranslate()));
            }
        }
        if (bungee instanceof net.md_5.bungee.api.chat.TextComponent) {
            net.md_5.bungee.api.chat.TextComponent impl = (net.md_5.bungee.api.chat.TextComponent) bungee;
            return new Builder(Component.text(impl.getText()));
        }
        return null;
    }
}
