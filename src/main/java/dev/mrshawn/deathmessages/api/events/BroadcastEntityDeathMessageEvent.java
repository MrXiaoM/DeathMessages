package dev.mrshawn.deathmessages.api.events;

import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.enums.MessageType;
import dev.mrshawn.deathmessages.utils.ComponentUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class BroadcastEntityDeathMessageEvent extends Event implements Cancellable {
    private final PlayerManager player;
    private final Entity entity;
    private final MessageType messageType;
    private final TextComponent textComponent;
    private final List<World> broadcastedWorlds;
    private boolean isCancelled = false;
    private static final HandlerList HANDLERS = new HandlerList();

    public BroadcastEntityDeathMessageEvent(PlayerManager pm, Entity entity, MessageType messageType, TextComponent textComponent, List<World> broadcastedWorlds) {
        this.player = pm;
        this.entity = entity;
        this.messageType = messageType;
        this.textComponent = ComponentUtils.scanAndInheritStyle(textComponent);
        this.broadcastedWorlds = broadcastedWorlds;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PlayerManager getPlayer() {
        return this.player;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public TextComponent getTextComponent() {
        return this.textComponent;
    }

    public List<World> getBroadcastedWorlds() {
        return this.broadcastedWorlds;
    }
}
