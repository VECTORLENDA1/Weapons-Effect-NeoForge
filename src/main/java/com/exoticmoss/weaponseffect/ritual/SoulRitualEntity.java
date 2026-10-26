package com.exoticmoss.weaponseffect.ritual;

import com.exoticmoss.weaponseffect.entity.ModEntities;
import com.exoticmoss.weaponseffect.entity.custom.ShockwaveEntity;
import com.exoticmoss.weaponseffect.item.ModItems;
import com.exoticmoss.weaponseffect.level.LevelUpChanges;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/// The Soul Ritual: how a weapon goes up a level.
///
/// HOW IT WORKS
///  1. When a weapon is "ready" (it has all the ability experience of its level), the player right-clicks
///     a Gravity Core of Simple Ores with it, with Soul Cores in the inventory. The core must have the
///     tier of the weapon's level or more: Sun for I -> II, Red Giant for II -> III, Pulsar for III -> IV,
///     Black Hole for IV -> V. The Gravity Core is never used up; the Soul Cores are always used up.
///  2. The weapon floats above the core and a dome opens around it. Nobody (players or monsters) can
///     leave the dome; other players can come in to help at any moment.
///  3. Waves of monsters appear (2 per level: 2 waves for I -> II, 4 for II -> III...). The game is in
///     Hard while the ritual goes on. The souls of the monsters fly into the weapon.
///  4. After the last wave the weapon goes up a level and goes back to the player.
///  5. If the player who started the ritual dies, the ritual fails: the Soul Cores break and the weapon
///     loses ALL its experience (it keeps its level). Other players dying doesn't matter.
public class SoulRitualEntity extends Entity {
    // ---------------------------------------------------------------- settings
    /// Size of the dome (radius in blocks). The ground inside must be flat, with 3 free blocks above it.
    public static final int DOME_RADIUS = 10;
    /// Soul Cores used by the ritual, for each level the weapon is at: { I -> II, II -> III, III -> IV, IV -> V }.
    public static final int[] SOUL_CORES_NEEDED = {1, 2, 3, 4};
    /// Waves for each level of the weapon (waves = level x this).
    public static final int WAVES_PER_LEVEL = 2;
    /// Monsters in the first wave for each level { I -> II, II -> III, III -> IV, IV -> V }; each next wave has one more.
    public static final int[] FIRST_WAVE_MONSTERS = {4, 5, 6, 7};
    /// The monsters of the waves for each level (a random one each time).
    public static final List<List<EntityType<? extends Mob>>> MONSTERS = List.of(
            List.of(EntityTypes.ZOMBIE, EntityTypes.SKELETON, EntityTypes.SPIDER),
            List.of(EntityTypes.ZOMBIE, EntityTypes.HUSK, EntityTypes.STRAY, EntityTypes.SPIDER, EntityTypes.WITCH),
            List.of(EntityTypes.WITHER_SKELETON, EntityTypes.BLAZE, EntityTypes.VINDICATOR, EntityTypes.PILLAGER, EntityTypes.BOGGED),
            List.of(EntityTypes.WITHER_SKELETON, EntityTypes.BLAZE, EntityTypes.VINDICATOR, EntityTypes.PIGLIN_BRUTE, EntityTypes.EVOKER));
    /// Extra monsters in the LAST wave for each level (the "bosses" of the ritual).
    public static final List<List<EntityType<? extends Mob>>> LAST_WAVE_BOSSES = List.of(
            List.of(),
            List.of(EntityTypes.WITCH),
            List.of(EntityTypes.RAVAGER),
            List.of(EntityTypes.RAVAGER, EntityTypes.EVOKER));
    /// Times (ticks, 20 = 1 second): the dome opening, the pause before each wave, the weapon absorbing the souls at the end.
    public static final int OPENING_TICKS = 40;
    public static final int WAVE_DELAY_TICKS = 60;
    public static final int FINISH_TICKS = 60;
    /// How far from the dome the entities stop (blocks), and how far out the server lets a player go
    /// before correcting them (their own game already stops them at the wall).
    public static final double WALL_GAP = 0.5;
    public static final double WALL_TOLERANCE = 1.0;
    /// Height of the floating weapon above the bottom of the core (blocks). The biggest core (Black Hole)
    /// with its glow is about 3 blocks tall, so the weapon floats above it.
    public static final double WEAPON_HEIGHT = 4.2;

    // ---------------------------------------------------------------- state
    private static final EntityDataAccessor<ItemStack> DATA_WEAPON = SynchedEntityData.defineId(SoulRitualEntity.class, EntityDataSerializers.ITEM_STACK);
    /// How open the dome is (0 to 1) and the progress of the end (0 to 1, the weapon absorbing the souls).
    private static final EntityDataAccessor<Float> DATA_DOME = SynchedEntityData.defineId(SoulRitualEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FINISH = SynchedEntityData.defineId(SoulRitualEntity.class, EntityDataSerializers.FLOAT);

    /// The rituals going on, by the player who started them (server only).
    private static final Map<UUID, SoulRitualEntity> ACTIVE = new HashMap<>();

    private enum Phase { OPENING, WAITING, FIGHTING, FINISHING }

    private Phase phase = Phase.OPENING;
    private int phaseTicks;
    private UUID ownerId;
    private int weaponLevel = 1;
    private int wave;
    private int totalWaves;
    private int waveSize;
    private boolean ended;
    private final Set<UUID> players = new HashSet<>();
    /// Only on the player's own game: true while the player is inside the dome.
    private boolean localPlayerInside;
    private final Set<UUID> monsters = new HashSet<>();
    private ServerBossEvent bossBar;

    public SoulRitualEntity(EntityType<? extends SoulRitualEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // ---------------------------------------------------------------- starting

    /// Tier of a Gravity Core of Simple Ores (1 = Sun ... 4 = Black Hole), 0 if the block isn't one.
    public static int coreTier(String blockId) {
        return switch (blockId) {
            case "simpleores:gravity_core_sun" -> 1;
            case "simpleores:gravity_core_red_giant" -> 2;
            case "simpleores:gravity_core_pulsar" -> 3;
            case "simpleores:gravity_core_black_hole" -> 4;
            default -> 0;
        };
    }

    /// Tries to start a ritual on the Gravity Core at "core" with the weapon in the player's main hand.
    /// Tells the player why when it can't start.
    public static void tryStart(ServerPlayer player, BlockPos core, int tier) {
        ServerLevel level = player.level();
        ItemStack weapon = player.getMainHandItem();
        int weaponLevel = WeaponLevels.level(weapon);

        if (ACTIVE.containsKey(player.getUUID())) {
            fail(player, "weaponseffect.ritual.already");
            return;
        }
        // Level V (or more, with the command): there is no next level
        if (weaponLevel >= WeaponLevels.MAX_LEVEL) {
            fail(player, "weaponseffect.level.max");
            return;
        }
        if (tier < weaponLevel) {
            fail(player, "weaponseffect.ritual.tier", Component.translatable(TIER_NAMES[weaponLevel - 1]));
            return;
        }
        int cores = SOUL_CORES_NEEDED[weaponLevel - 1];
        if (countSoulCores(player) < cores && !player.hasInfiniteMaterials()) {
            fail(player, "weaponseffect.ritual.cores", cores);
            return;
        }
        int badPlaces = countBadPlaces(level, core);
        if (badPlaces > 0) {
            fail(player, "weaponseffect.ritual.terrain", badPlaces);
            return;
        }

        // Start: the Soul Cores are used (not in creative), the weapon goes to float above the core
        if (!player.hasInfiniteMaterials()) useSoulCores(player, cores);
        SoulRitualEntity ritual = new SoulRitualEntity(ModEntities.SOUL_RITUAL.get(), level);
        ritual.ownerId = player.getUUID();
        ritual.weaponLevel = weaponLevel;
        ritual.totalWaves = weaponLevel * WAVES_PER_LEVEL;
        ritual.entityData.set(DATA_WEAPON, weapon.copy());
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ritual.setPos(core.getX() + 0.5, core.getY(), core.getZ() + 0.5);
        ritual.players.add(player.getUUID());
        ritual.bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("weaponseffect.ritual.bar.opening"),
                BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        ritual.bossBar.addPlayer(player);
        level.addFreshEntity(ritual);
        ACTIVE.put(player.getUUID(), ritual);

        RitualDifficulty.begin(level.getServer());
        level.playSound(null, core, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.6F);
        level.playSound(null, core, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6F, 1.5F);
    }

    private static final String[] TIER_NAMES = {"block.simpleores.gravity_core_sun", "block.simpleores.gravity_core_red_giant",
            "block.simpleores.gravity_core_pulsar", "block.simpleores.gravity_core_black_hole"};

    private static void fail(Player player, String message, Object... args) {
        player.sendOverlayMessage(Component.translatable(message, args).withStyle(ChatFormatting.RED));
    }

    private static int countSoulCores(Player player) {
        int count = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(ModItems.SOUL_CORE.get())) count += stack.getCount();
        }
        return count;
    }

    private static void useSoulCores(Player player, int amount) {
        for (ItemStack stack : player.getInventory()) {
            if (amount <= 0) return;
            if (stack.is(ModItems.SOUL_CORE.get())) {
                int used = Math.min(amount, stack.getCount());
                stack.shrink(used);
                amount -= used;
            }
        }
    }

    /// How many places inside the dome are not flat ground with 3 free blocks above (0 = ok).
    private static int countBadPlaces(ServerLevel level, BlockPos core) {
        int bad = 0;
        for (int dx = -DOME_RADIUS; dx <= DOME_RADIUS; dx++) {
            for (int dz = -DOME_RADIUS; dz <= DOME_RADIUS; dz++) {
                if (dx * dx + dz * dz > DOME_RADIUS * DOME_RADIUS || (dx == 0 && dz == 0)) continue;
                BlockPos floor = core.offset(dx, -1, dz);
                boolean ok = level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP);
                for (int dy = 1; dy <= 3 && ok; dy++) {
                    BlockPos air = floor.above(dy);
                    ok = level.getBlockState(air).getCollisionShape(level, air).isEmpty();
                }
                if (!ok) bad++;
            }
        }
        return bad;
    }

    // ---------------------------------------------------------------- the ritual

    public ItemStack getWeapon() {
        return this.entityData.get(DATA_WEAPON);
    }

    public float getDomeOpen() {
        return this.entityData.get(DATA_DOME);
    }

    public float getFinishProgress() {
        return this.entityData.get(DATA_FINISH);
    }

    /// Where the weapon floats.
    public Vec3 weaponPosition() {
        return this.position().add(0, WEAPON_HEIGHT, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            wallForLocalPlayer();
            return;
        }
        if (!(this.level() instanceof ServerLevel level) || ended) return;

        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || owner.level() != level) {
            // The player left the game (or there is no owner after a reload): stop without punishment
            abort(level, owner);
            return;
        }
        phaseTicks++;
        keepPlayersInside(level);
        keepMonstersInside(level);

        switch (phase) {
            case OPENING -> {
                this.entityData.set(DATA_DOME, Math.min(1F, phaseTicks / (float) OPENING_TICKS));
                if (phaseTicks >= OPENING_TICKS) setPhase(Phase.WAITING);
            }
            case WAITING -> {
                bossBar.setName(Component.translatable("weaponseffect.ritual.bar.next", wave + 1, totalWaves));
                bossBar.setProgress(phaseTicks / (float) WAVE_DELAY_TICKS);
                if (phaseTicks >= WAVE_DELAY_TICKS) spawnWave(level, owner);
            }
            case FIGHTING -> {
                bossBar.setName(Component.translatable("weaponseffect.ritual.bar.wave", wave, totalWaves, monsters.size()));
                bossBar.setProgress(waveSize == 0 ? 0 : monsters.size() / (float) waveSize);
                if (monsters.isEmpty()) {
                    if (wave >= totalWaves) setPhase(Phase.FINISHING);
                    else setPhase(Phase.WAITING);
                }
            }
            case FINISHING -> {
                bossBar.setName(Component.translatable("weaponseffect.ritual.bar.finishing"));
                bossBar.setProgress(phaseTicks / (float) FINISH_TICKS);
                this.entityData.set(DATA_FINISH, Math.min(1F, phaseTicks / (float) FINISH_TICKS));
                if (phaseTicks >= FINISH_TICKS) succeed(level, owner);
            }
        }
    }

    private void setPhase(Phase phase) {
        this.phase = phase;
        this.phaseTicks = 0;
    }

    /// A new wave of monsters, appearing near the wall of the dome.
    private void spawnWave(ServerLevel level, ServerPlayer owner) {
        wave++;
        List<EntityType<? extends Mob>> types = new ArrayList<>();
        List<EntityType<? extends Mob>> pool = MONSTERS.get(weaponLevel - 1);
        int count = FIRST_WAVE_MONSTERS[weaponLevel - 1] + wave - 1;
        for (int i = 0; i < count; i++) types.add(pool.get(level.getRandom().nextInt(pool.size())));
        if (wave == totalWaves) types.addAll(LAST_WAVE_BOSSES.get(weaponLevel - 1));

        for (EntityType<? extends Mob> type : types) {
            for (int attempt = 0; attempt < 10; attempt++) {
                double angle = level.getRandom().nextDouble() * Math.PI * 2;
                double distance = DOME_RADIUS * (0.5 + 0.4 * level.getRandom().nextDouble());
                BlockPos pos = BlockPos.containing(this.getX() + Math.cos(angle) * distance, this.getY(), this.getZ() + Math.sin(angle) * distance);
                if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) continue;
                Mob mob = type.spawn(level, pos, EntitySpawnReason.EVENT);
                if (mob == null) continue;
                mob.setPersistenceRequired();
                // no burning in the sun, and they go straight for the player
                mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, false, false));
                mob.setTarget(owner);
                monsters.add(mob.getUUID());
                break;
            }
        }
        waveSize = monsters.size();
        setPhase(Phase.FIGHTING);
        level.playSound(null, this.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 2.0F, 1.0F);
    }

    /// Players inside the dome join the ritual; players of the ritual can't leave the dome.
    private void keepPlayersInside(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            double distance = domeDistance(player);
            boolean joined = players.contains(player.getUUID());
            if (!joined && distance <= DOME_RADIUS && player.isAlive() && !player.isSpectator()) {
                players.add(player.getUUID());
                bossBar.addPlayer(player);
                joined = true;
            }
            // The player's own game already stops them at the wall (see wallForLocalPlayer): the server only
            // corrects players that got further out (lag, ender pearls, teleports...)
            if (joined && player.isAlive() && distance > DOME_RADIUS + WALL_TOLERANCE) stopAtWall(player, true);
        }
    }

    /// Monsters of the ritual can't leave the dome, always attack a player of the ritual,
    /// and their souls fly to the weapon when they die.
    private void keepMonstersInside(ServerLevel level) {
        var iterator = monsters.iterator();
        while (iterator.hasNext()) {
            Entity entity = level.getEntity(iterator.next());
            if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                if (entity instanceof LivingEntity dead && dead.isDeadOrDying()) SoulEntity.spawn(level, dead.position(), this);
                iterator.remove();
                continue;
            }
            if (domeDistance(mob) > DOME_RADIUS - WALL_GAP) stopAtWall(mob, false);
            if (mob.getTarget() == null || !mob.getTarget().isAlive()) mob.setTarget(closestPlayer(level, mob));
        }
    }

    private Player closestPlayer(ServerLevel level, Mob mob) {
        Player closest = null;
        double best = Double.MAX_VALUE;
        for (UUID id : players) {
            Player player = level.getPlayerByUUID(id);
            if (player == null || !player.isAlive() || player.isCreative()) continue;
            double distance = player.distanceToSqr(mob);
            if (distance < best) {
                best = distance;
                closest = player;
            }
        }
        return closest;
    }

    /// Distance from the middle of the dome to the middle of the entity's body. The dome is a ball,
    /// so this works for the sides and for the top (no flying or pearling out over it).
    private double domeDistance(Entity entity) {
        return entity.getBoundingBox().getCenter().distanceTo(this.position());
    }

    /// The dome works like a solid wall: an entity that goes past it is put back right on the inside of the
    /// wall (or the roof) and loses only the speed that was going out, so it slides along the wall
    /// instead of being thrown back.
    private void stopAtWall(Entity entity, boolean sendToPlayer) {
        Vec3 center = entity.getBoundingBox().getCenter();
        Vec3 out = center.subtract(this.position()).normalize();
        Vec3 inside = this.position().add(out.scale(DOME_RADIUS - WALL_GAP));
        Vec3 move = inside.subtract(center);
        double feetY = Math.max(this.getY(), entity.getY() + move.y);
        Vec3 velocity = entity.getDeltaMovement();
        double outward = velocity.dot(out);
        if (outward > 0) entity.setDeltaMovement(velocity.subtract(out.scale(outward)));
        if (sendToPlayer) {
            entity.teleportTo(entity.getX() + move.x, feetY, entity.getZ() + move.z);
            entity.hurtMarked = true;
        } else {
            entity.setPos(entity.getX() + move.x, feetY, entity.getZ() + move.z);
        }
    }

    /// On the player's own game: the player that was inside the dome can't walk, jump or fly out of it
    /// (it feels like a wall). Players outside can still walk in (and then they are inside too).
    private void wallForLocalPlayer() {
        if (getDomeOpen() <= 0 || getFinishProgress() > 0) return;
        for (Player player : this.level().players()) {
            if (!player.isLocalPlayer() || player.isSpectator()) continue;
            boolean inside = domeDistance(player) <= DOME_RADIUS - WALL_GAP + 0.05;
            if (!inside && localPlayerInside) {
                stopAtWall(player, false);
                inside = true;
            }
            localPlayerInside = inside;
        }
    }

    // ---------------------------------------------------------------- the end

    /// All the waves were beaten: the weapon goes up a level and back to the player.
    private void succeed(ServerLevel level, ServerPlayer owner) {
        ItemStack weapon = getWeapon().copy();
        WeaponLevels.set(weapon, weaponLevel + 1, 0);
        // The name and the news are read before giving the weapon back (giving it empties this stack)
        LevelUpChanges.tell(owner, weapon, weaponLevel, weaponLevel + 1);
        giveBack(level, owner, weapon);
        level.playSound(null, this.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.5F, 1.0F);
        ShockwaveEntity.spawn(level, this.position(), DOME_RADIUS, 0x6040E0FF);
        end(level);
    }

    /// The player who started the ritual died: the Soul Cores break and the weapon loses all its experience.
    public void failBecauseOwnerDied(ServerLevel level, Player owner) {
        if (ended) return;
        ItemStack weapon = getWeapon().copy();
        WeaponLevels.set(weapon, weaponLevel, 0);
        level.addFreshEntity(new ItemEntity(level, owner.getX(), owner.getY() + 0.5, owner.getZ(), weapon));
        owner.sendSystemMessage(Component.translatable("weaponseffect.ritual.failed").withStyle(ChatFormatting.DARK_RED));
        level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
        level.playSound(null, this.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.6F, 1.5F);
        end(level);
    }

    /// Stopped without punishment (the player left the game, the server is closing...): the weapon goes back as it was.
    public void abort(ServerLevel level, ServerPlayer owner) {
        if (ended) return;
        giveBack(level, owner, getWeapon().copy());
        end(level);
    }

    private void giveBack(ServerLevel level, ServerPlayer owner, ItemStack weapon) {
        if (owner != null && owner.isAlive() && owner.getInventory().add(weapon)) return;
        Vec3 at = owner != null ? owner.position() : this.position().add(DOME_RADIUS / 2.0, 0.5, 0);
        level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, weapon));
    }

    /// Removes the monsters left, the dome, the boss bar, and gives back the old difficulty.
    private void end(ServerLevel level) {
        ended = true;
        for (UUID id : monsters) {
            Entity monster = level.getEntity(id);
            if (monster != null) monster.discard();
        }
        monsters.clear();
        if (bossBar != null) bossBar.removeAllPlayers();
        ACTIVE.remove(ownerId);
        RitualDifficulty.end(level.getServer());
        this.discard();
    }

    /// The player who started the ritual (null if not online).
    public ServerPlayer getOwnerPlayer(ServerLevel level) {
        return ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
    }

    /// The ritual of this player (null if none).
    public static SoulRitualEntity of(UUID player) {
        return ACTIVE.get(player);
    }

    /// All the rituals going on (to stop them when the server closes).
    public static List<SoulRitualEntity> all() {
        return new ArrayList<>(ACTIVE.values());
    }

    // If the chunk is unloaded in the middle of a ritual (it shouldn't happen), the weapon is not lost
    @Override
    public void remove(RemovalReason reason) {
        if (!ended && this.level() instanceof ServerLevel level) {
            ended = true;
            level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY() + 1, this.getZ(), getWeapon().copy()));
            if (bossBar != null) bossBar.removeAllPlayers();
            ACTIVE.remove(ownerId);
            RitualDifficulty.end(level.getServer());
        }
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_WEAPON, ItemStack.EMPTY);
        builder.define(DATA_DOME, 0F);
        builder.define(DATA_FINISH, 0F);
    }

    // The ritual is not saved with the world (see remove: the weapon is dropped if it is unloaded)
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
