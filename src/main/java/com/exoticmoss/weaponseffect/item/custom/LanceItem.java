package com.exoticmoss.weaponseffect.item.custom;


import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.exoticmoss.weaponseffect.entity.custom.LanceProjectileEntity;

public class LanceItem extends Item {
    public LanceItem(Properties pProperties) {
        super(pProperties);
    }

    // Right-click: throws the lance (like a trident, but without charging)
    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!pLevel.isClientSide()) {
            LanceProjectileEntity lanceProjectile = new LanceProjectileEntity(pLevel, pPlayer, itemstack);
            lanceProjectile.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 3.0F, 0F);
            if (pPlayer.getAbilities().instabuild) {
                lanceProjectile.pickup = LanceProjectileEntity.Pickup.CREATIVE_ONLY;
            }
            pLevel.addFreshEntity(lanceProjectile);
        }

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

}
