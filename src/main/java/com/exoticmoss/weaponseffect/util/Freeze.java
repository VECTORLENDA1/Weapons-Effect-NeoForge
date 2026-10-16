package com.exoticmoss.weaponseffect.util;

import net.minecraft.world.entity.LivingEntity;

/// The freezing of powder snow (vanilla): frost on the screen, the mob shakes, gets slow and takes
/// frost damage every 2 seconds while it is fully frozen.
public final class Freeze {
    private Freeze() {
    }

    /// Freezes the entity for about this many seconds (mobs that can't freeze, like strays, are not affected).
    public static void apply(LivingEntity entity, int seconds) {
        if (!entity.canFreeze() || seconds <= 0) return;
        // Out of powder snow, the freezing goes down 2 per tick: this keeps it fully frozen for "seconds"
        int frozen = entity.getTicksRequiredToFreeze() + seconds * 20 * 2;
        entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), frozen));
    }
}
