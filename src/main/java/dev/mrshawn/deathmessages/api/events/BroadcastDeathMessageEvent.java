package dev.mrshawn.deathmessages.api.events;

import dev.mrshawn.deathmessages.enums.MessageType;
import dev.mrshawn.deathmessages.utils.ComponentUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class BroadcastDeathMessageEvent extends Event implements Cancellable {
    private final Player player;
    private final LivingEntity livingEntity;
    private final MessageType messageType;
    private final TextComponent textComponent;
    private final boolean isGangDeath;
    private final List<World> broadcastedWorlds;
    private boolean isCancelled = false;
    public final StackTraceElement[] stackTrace;
    private static final HandlerList HANDLERS = new HandlerList();

    public BroadcastDeathMessageEvent(Player player, LivingEntity livingEntity, MessageType messageType, TextComponent textComponent, List<World> broadcastedWorlds, boolean isGangDeath, StackTraceElement[] stackTrace) {
        this.player = player;
        this.livingEntity = livingEntity;
        this.messageType = messageType;
        this.textComponent = ComponentUtils.scanAndInheritStyle(textComponent);
        this.isGangDeath = isGangDeath;
        this.broadcastedWorlds = broadcastedWorlds;
        this.stackTrace = stackTrace;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return this.player;
    }

    public LivingEntity getLivingEntity() {
        return this.livingEntity;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public TextComponent getTextComponent() {
        return this.textComponent;
    }

    public boolean isGangDeath() {
        return this.isGangDeath;
    }

    public List<World> getBroadcastedWorlds() {
        return this.broadcastedWorlds;
    }
}
