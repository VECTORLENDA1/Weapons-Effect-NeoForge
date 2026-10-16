package com.exoticmoss.weaponseffect.ritual;

import com.exoticmoss.weaponseffect.WeaponsEffect;
import com.exoticmoss.weaponseffect.level.AbilityWeapon;
import com.exoticmoss.weaponseffect.level.WeaponLevels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/// Starts and ends the Soul Rituals (see SoulRitualEntity).
@EventBusSubscriber(modid = WeaponsEffect.MODID)
public class RitualEvents {

    /// Right-click on a Gravity Core of Simple Ores with a "ready" weapon in the main hand: starts the ritual
    /// (instead of putting the weapon in the core).
    @SubscribeEvent
    public static void onRightClickCore(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack weapon = event.getEntity().getMainHandItem();
        if (!(weapon.getItem() instanceof AbilityWeapon) || !WeaponLevels.isReady(weapon)) return;
        String block = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        int tier = SoulRitualEntity.coreTier(block);
        if (tier == 0) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player) SoulRitualEntity.tryStart(player, event.getPos(), tier);
    }

    /// The player who started a ritual died: the ritual fails.
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SoulRitualEntity ritual = SoulRitualEntity.of(player.getUUID());
        if (ritual != null) ritual.failBecauseOwnerDied(player.level(), player);
    }

    /// The player who started a ritual left the game: the ritual stops and the weapon goes back as it was.
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SoulRitualEntity ritual = SoulRitualEntity.of(player.getUUID());
        if (ritual != null && ritual.level() instanceof net.minecraft.server.level.ServerLevel level) ritual.abort(level, player);
    }

    /// The server is closing: every ritual stops and the weapons go back to their players.
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (SoulRitualEntity ritual : SoulRitualEntity.all()) {
            if (ritual.level() instanceof net.minecraft.server.level.ServerLevel level) {
                ritual.abort(level, ritual.getOwnerPlayer(level));
            }
        }
    }
}
