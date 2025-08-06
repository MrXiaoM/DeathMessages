package dev.mrshawn.deathmessages.api;

import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.enums.MobType;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.utils.DeathResolver;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class EntityManager {
    private final Entity entity;
    private final UUID entityUUID;
    private final MobType mobType;
    private EntityDamageEvent.DamageCause damageCause;
    private @Nullable String damageSourceMsgId;
    private PlayerManager lastPlayerDamager;
    private Entity lastExplosiveEntity;
    private Projectile lastPlayerProjectile;
    private Location lastLocation;
    private BukkitTask lastPlayerTask;
    private static final FileSettings<Config> config = FileSettings.CONFIG;
    private static final List<EntityManager> entities = new ArrayList<>();

    public EntityManager(Entity entity, UUID entityUUID, MobType mobType) {
        this.entity = entity;
        this.entityUUID = entityUUID;
        this.mobType = mobType;
        entities.add(this);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public UUID getEntityUUID() {
        return this.entityUUID;
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

    public void setLastPlayerDamager(PlayerManager pm) {
        setLastExplosiveEntity(null);
        setLastProjectileEntity(null);
        this.lastPlayerDamager = pm;
        if (pm == null) {
            return;
        }
        if (this.lastPlayerTask != null) {
            this.lastPlayerTask.cancel();
        }
        DeathMessages.getInstance().getScheduler().runLater(
                EntityManager.this::destroy,
                config.getInt(Config.EXPIRE_LAST_DAMAGE_EXPIRE_ENTITY) * 20L);
        this.damageCause = EntityDamageEvent.DamageCause.CUSTOM;
    }

    public PlayerManager getLastPlayerDamager() {
        return this.lastPlayerDamager;
    }

    public void setLastExplosiveEntity(Entity e) {
        this.lastExplosiveEntity = e;
    }

    public Entity getLastExplosiveEntity() {
        return this.lastExplosiveEntity;
    }

    public void setLastProjectileEntity(Projectile projectile) {
        this.lastPlayerProjectile = projectile;
    }

    public Projectile getLastProjectileEntity() {
        return this.lastPlayerProjectile;
    }

    public void setLastLocation(Location location) {
        this.lastLocation = location;
    }

    public Location getLastLocation() {
        return this.lastLocation;
    }

    public static EntityManager getEntity(UUID uuid) {
        for (EntityManager em : entities) {
            if (em.getEntityUUID().equals(uuid)) {
                return em;
            }
        }
        return null;
    }

    public void destroy() {
        entities.remove(this);
    }
}
