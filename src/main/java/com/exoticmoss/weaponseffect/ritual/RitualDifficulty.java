package com.exoticmoss.weaponseffect.ritual;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

/// While a Soul Ritual is going on the game is forced to Hard; when the last ritual ends
/// the difficulty goes back to what it was before.
public final class RitualDifficulty {
    private static int activeRituals = 0;
    private static Difficulty before = Difficulty.NORMAL;

    private RitualDifficulty() {
    }

    public static void begin(MinecraftServer server) {
        if (activeRituals++ == 0) before = server.getWorldData().getDifficulty();
        server.setDifficulty(Difficulty.HARD, true);
    }

    public static void end(MinecraftServer server) {
        if (activeRituals <= 0) return;
        if (--activeRituals == 0) server.setDifficulty(before, true);
    }
}
