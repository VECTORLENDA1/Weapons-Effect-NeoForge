package com.exoticmoss.weaponseffect.ritual;

import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/// Starts and ends the Soul Rituals (see SoulRitualEntity).
public class RitualEvents {

    public static void register() {
        UseBlockCallback.EVENT.register(RitualEvents::onRightClickCore);
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> onDeath(entity));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onLogout(handler.player));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> onServerStopping());
    }

    /// Right-click on a Gravity Core of Simple Ores with a "ready" weapon in the main hand: starts the ritual
    /// (instead of putting the weapon in the core).
    public static InteractionResult onRightClickCore(Player clicker, Level level, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack weapon = clicker.getMainHandItem();
        if (!(weapon.getItem() instanceof AbilityWeapon) || !WeaponLevels.isReady(weapon)) return InteractionResult.PASS;
        BlockPos pos = hit.getBlockPos();
        String block = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
        int tier = SoulRitualEntity.coreTier(block);
        if (tier == 0) return InteractionResult.PASS;

        if (clicker instanceof ServerPlayer player) SoulRitualEntity.tryStart(player, pos, tier);
        return InteractionResult.SUCCESS;
    }

    /// The player who started a ritual died: the ritual fails.
    public static void onDeath(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        SoulRitualEntity ritual = SoulRitualEntity.of(player.getUUID());
        if (ritual != null) ritual.failBecauseOwnerDied(player.level(), player);
    }

    /// The player who started a ritual left the game: the ritual stops and the weapon goes back as it was.
    public static void onLogout(ServerPlayer player) {
        SoulRitualEntity ritual = SoulRitualEntity.of(player.getUUID());
        if (ritual != null && ritual.level() instanceof net.minecraft.server.level.ServerLevel level) ritual.abort(level, player);
    }

    /// The server is closing: every ritual stops and the weapons go back to their players.
    public static void onServerStopping() {
        for (SoulRitualEntity ritual : SoulRitualEntity.all()) {
            if (ritual.level() instanceof net.minecraft.server.level.ServerLevel level) {
                ritual.abort(level, ritual.getOwnerPlayer(level));
            }
        }
    }
}
