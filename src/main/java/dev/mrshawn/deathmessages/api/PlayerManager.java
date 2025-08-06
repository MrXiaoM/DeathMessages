package dev.mrshawn.deathmessages.api;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.config.UserData;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.utils.DeathResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class PlayerManager {
    private final UUID uuid;
    private final String name;
    private boolean messagesEnabled;
    private boolean isBlacklisted;
    private EntityDamageEvent.DamageCause damageCause;
    private @Nullable String damageSourceMsgId;
    private Entity lastEntityDamager;
    private Entity lastExplosiveEntity;
    private Projectile lastProjectileEntity;
    private Material climbing;
    private Location explosionCauser;
    private Location location;
    private WrappedTask cooldownTask;
    private Inventory cachedInventory;
    private WrappedTask lastEntityTask;
    private static final FileSettings<Config> config = FileSettings.CONFIG;
    private static final List<PlayerManager> players = new ArrayList<>();
    private int cooldown = 0;
    public boolean saveUserData = config.getBoolean(Config.SAVED_USER_DATA);

    public PlayerManager(Player p) {
        this.uuid = p.getUniqueId();
        this.name = p.getName();
        if (this.saveUserData && !UserData.getInstance().getConfig().contains(p.getUniqueId().toString())) {
            UserData.getInstance().getConfig().set(p.getUniqueId() + ".username", p.getName());
            UserData.getInstance().getConfig().set(p.getUniqueId() + ".messages-enabled", true);
            UserData.getInstance().getConfig().set(p.getUniqueId() + ".is-blacklisted", false);
            UserData.getInstance().save();
        }
        if (this.saveUserData) {
            this.messagesEnabled = UserData.getInstance().getConfig().getBoolean(p.getUniqueId() + ".messages-enabled");
            this.isBlacklisted = UserData.getInstance().getConfig().getBoolean(p.getUniqueId() + ".is-blacklisted");
        } else {
            this.messagesEnabled = true;
            this.isBlacklisted = false;
        }
        this.damageCause = EntityDamageEvent.DamageCause.CUSTOM;
        players.add(this);
    }

    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(this.uuid);
    }

    public UUID getUUID() {
        return Objects.requireNonNull(this.uuid);
    }

    public String getName() {
        return Objects.requireNonNull(this.name);
    }

    public boolean getMessagesEnabled() {
        return this.messagesEnabled;
    }

    public void setMessagesEnabled(boolean b) {
        this.messagesEnabled = b;
        if (this.saveUserData) {
            UserData.getInstance().getConfig().set(this.uuid.toString() + ".messages-enabled", b);
            UserData.getInstance().save();
        }
    }

    public boolean isBlacklisted() {
        return this.isBlacklisted;
    }

    public void setBlacklisted(boolean b) {
        this.isBlacklisted = b;
        if (this.saveUserData) {
            UserData.getInstance().getConfig().set(this.uuid.toString() + ".is-blacklisted", b);
            UserData.getInstance().save();
        }
    }

    public void setLastDamageCause(EntityDamageEvent.DamageCause dc) {
        this.damageCause = dc;
    }

    public EntityDamageEvent.DamageCause getLastDamage() {
        return this.damageCause;
    }

    public void setDamageSourceMsgId(EntityDamageEvent event) {
        this.damageSourceMsgId = DeathResolver.getDamageSourceMsgId(event);
    }

    public void setDamageSourceMsgId(String damageSourceMsgId) {
        this.damageSourceMsgId = damageSourceMsgId;
    }

    @Nullable
    public String getDamageSourceMsgId() {
        return damageSourceMsgId;
    }

    public void setLastEntityDamager(Entity e) {
        setLastExplosiveEntity(null);
        setLastProjectileEntity(null);
        this.lastEntityDamager = e;
        if (e == null) {
            return;
        }
        if (this.lastEntityTask != null) {
            this.lastEntityTask.cancel();
        }
        DeathMessages.getInstance().getScheduler().runLater(
                () -> setLastEntityDamager(null),
                config.getInt(Config.EXPIRE_LAST_DAMAGE_EXPIRE_PLAYER) * 20L);
    }

    public Entity getLastEntityDamager() {
        return this.lastEntityDamager;
    }

    public void setLastExplosiveEntity(Entity e) {
        this.lastExplosiveEntity = e;
    }

    public Entity getLastExplosiveEntity() {
        return this.lastExplosiveEntity;
    }

    public Projectile getLastProjectileEntity() {
        return this.lastProjectileEntity;
    }

    public void setLastProjectileEntity(Projectile lastProjectileEntity) {
        this.lastProjectileEntity = lastProjectileEntity;
    }

    public Material getLastClimbing() {
        return this.climbing;
    }

    public void setLastClimbing(Material climbing) {
        this.climbing = climbing;
    }

    public void setExplosionCauser(Location location) {
        this.explosionCauser = location;
    }

    public Location getExplosionCauser() {
        return this.explosionCauser;
    }

    public Location getLastLocation() {
        return getPlayer().getLocation();
    }

    public boolean isInCooldown() {
        return this.cooldown > 0;
    }


    public void setCooldown() {
        this.cooldown = config.getInt(Config.COOLDOWN);
        this.cooldownTask = DeathMessages.getInstance().getScheduler().runTimer(() -> {
            if (PlayerManager.this.cooldown <= 0) {
                this.cooldownTask.cancel();
            }
            PlayerManager.this.cooldown--;
        }, 1L, 20L);
    }

    public void setCachedInventory(Inventory inventory) {
        this.cachedInventory = inventory;
    }

    public Inventory getCachedInventory() {
        return this.cachedInventory;
    }

    public static PlayerManager getPlayer(Player p) {
        Optional<PlayerManager> opm = players.stream()
                .filter(it -> it.getUUID().equals(p.getUniqueId()))
                .findFirst();
        if (opm.isPresent()) return opm.get();
        PlayerManager pm = new PlayerManager(p);
        players.add(pm);
        return pm;
    }

    public static PlayerManager getPlayer(UUID uuid) {
        for (PlayerManager pm : players) {
            if (pm.getUUID().equals(uuid)) {
                return pm;
            }
        }
        return null;
    }

    public void removePlayer() {
        players.remove(this);
    }
}
