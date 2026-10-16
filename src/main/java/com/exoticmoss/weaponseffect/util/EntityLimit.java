package com.exoticmoss.weaponseffect.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/// Keeps the number of ability entities low, so using the abilities again and again doesn't make the game lag
/// (like many TNT exploding at once). The limits are for each player and each kind of entity.
///  - Projectiles (skulls, shards, lances, axes): when the player already has the most allowed, the new one is
///    not created and the closest one gets its power instead (it does double damage, triple...).
///    So every shot still counts, with far fewer entities.
///  - Areas (clouds, smoke, zones, black holes...): when the player already has the most allowed, the oldest
///    one disappears to make space for the new one (their effects don't add up anyway).
public final class EntityLimit {
    /// How far (blocks) the entities of the same player are counted.
    public static final double SEARCH_RANGE = 64;

    /// An ability entity: who used the ability (null = nobody, e.g. only the look of an explosion).
    public interface Owned {
        @Nullable UUID abilityOwnerId();
    }

    /// A projectile that can take the power of others of its kind.
    public interface Mergeable extends Owned {
        /// "count" more projectiles of this kind were not created: this one does their damage too.
        void absorb(int count);
    }

    private EntityLimit() {
    }

    /// Creates the projectile, or (when the player already has "max") gives its power to the closest one.
    /// Returns true if the projectile was created.
    public static <T extends Entity & Mergeable> boolean spawnOrMerge(ServerLevel level, T entity, int max) {
        List<T> same = sameKind(level, entity);
        if (same.size() < max) {
            level.addFreshEntity(entity);
            return true;
        }
        T closest = same.getFirst();
        for (T other : same) {
            if (other.distanceToSqr(entity) < closest.distanceToSqr(entity)) closest = other;
        }
        closest.absorb(1);
        return false;
    }

    /// Creates the area entity, removing the oldest of the player's when they already have "max".
    public static <T extends Entity & Owned> void spawnReplacingOldest(ServerLevel level, T entity, int max) {
        List<T> same = sameKind(level, entity);
        while (same.size() >= max) {
            T oldest = same.getFirst();
            for (T other : same) {
                if (other.tickCount > oldest.tickCount) oldest = other;
            }
            oldest.discard();
            same.remove(oldest);
        }
        level.addFreshEntity(entity);
    }

    /// True if the player already has "max" (or more) entities like this one.
    public static <T extends Entity & Owned> boolean isFull(ServerLevel level, T entity, int max) {
        return sameKind(level, entity).size() >= max;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity & Owned> List<T> sameKind(ServerLevel level, T entity) {
        UUID owner = entity.abilityOwnerId();
        return new java.util.ArrayList<>(level.getEntitiesOfClass((Class<T>) entity.getClass(), entity.getBoundingBox().inflate(SEARCH_RANGE),
                other -> other != entity && other.isAlive() && Objects.equals(owner, other.abilityOwnerId())));
    }
}
