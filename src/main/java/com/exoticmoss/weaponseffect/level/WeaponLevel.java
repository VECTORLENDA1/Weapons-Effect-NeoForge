package com.exoticmoss.weaponseffect.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// The level of a weapon (1 to WeaponLevels.MAX_LEVEL) and the ability experience it has.
/// Saved on the weapon itself, so it goes with the weapon if it is given to another player.
public record WeaponLevel(int level, int xp) {
    public static final WeaponLevel DEFAULT = new WeaponLevel(1, 0);

    public static final Codec<WeaponLevel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("level").forGetter(WeaponLevel::level),
            Codec.INT.fieldOf("xp").forGetter(WeaponLevel::xp)
    ).apply(inst, WeaponLevel::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponLevel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WeaponLevel::level,
            ByteBufCodecs.VAR_INT, WeaponLevel::xp,
            WeaponLevel::new);
}
