package com.sirolf2009.necromancy.api;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Declarative description of the ambient / hurt / death sounds an adapter
 * contributes to the minion's "voice".  Returned by
 * {@link NecroEntityBase#voice()}.
 *
 * <p>The runtime picks the active voice from the TORSO adapter first
 * (because the torso "is" the body of the minion), and falls back to the
 * HEAD adapter, and finally to {@link #ZOMBIE} so a brand new adapter that
 * forgets to override anything still plays nicely.
 */
public record VoiceProfile(
        SoundEvent ambient,
        SoundEvent hurt,
        SoundEvent death,
        float      volume,
        float      pitch) {

    public static final VoiceProfile ZOMBIE = new VoiceProfile(
        SoundEvents.ZOMBIE_AMBIENT, SoundEvents.ZOMBIE_HURT, SoundEvents.ZOMBIE_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile SKELETON = new VoiceProfile(
        SoundEvents.SKELETON_AMBIENT, SoundEvents.SKELETON_HURT, SoundEvents.SKELETON_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile COW = new VoiceProfile(
        SoundEvents.COW_AMBIENT, SoundEvents.COW_HURT, SoundEvents.COW_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile PIG = new VoiceProfile(
        SoundEvents.PIG_AMBIENT, SoundEvents.PIG_HURT, SoundEvents.PIG_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile SHEEP = new VoiceProfile(
        SoundEvents.SHEEP_AMBIENT, SoundEvents.SHEEP_HURT, SoundEvents.SHEEP_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile CHICKEN = new VoiceProfile(
        SoundEvents.CHICKEN_AMBIENT, SoundEvents.CHICKEN_HURT, SoundEvents.CHICKEN_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile WOLF = new VoiceProfile(
        SoundEvents.WOLF_AMBIENT, SoundEvents.WOLF_HURT, SoundEvents.WOLF_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile CREEPER = new VoiceProfile(
        // Vanilla creepers are silent by default but hiss on attack; use the
        // standard ambient slot for the death sound to avoid total silence.
        SoundEvents.CREEPER_PRIMED, SoundEvents.CREEPER_HURT, SoundEvents.CREEPER_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile SPIDER = new VoiceProfile(
        SoundEvents.SPIDER_AMBIENT, SoundEvents.SPIDER_HURT, SoundEvents.SPIDER_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile SQUID = new VoiceProfile(
        SoundEvents.SQUID_AMBIENT, SoundEvents.SQUID_HURT, SoundEvents.SQUID_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile ENDERMAN = new VoiceProfile(
        SoundEvents.ENDERMAN_AMBIENT, SoundEvents.ENDERMAN_HURT, SoundEvents.ENDERMAN_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile VILLAGER = new VoiceProfile(
        SoundEvents.VILLAGER_AMBIENT, SoundEvents.VILLAGER_HURT, SoundEvents.VILLAGER_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile WITCH = new VoiceProfile(
        SoundEvents.WITCH_AMBIENT, SoundEvents.WITCH_HURT, SoundEvents.WITCH_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile IRON_GOLEM = new VoiceProfile(
        SoundEvents.IRON_GOLEM_STEP, SoundEvents.IRON_GOLEM_HURT, SoundEvents.IRON_GOLEM_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile PIGLIN_ZOMBIE = new VoiceProfile(
        SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT, SoundEvents.ZOMBIFIED_PIGLIN_HURT, SoundEvents.ZOMBIFIED_PIGLIN_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile ALLAY = new VoiceProfile(
        SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundEvents.ALLAY_HURT, SoundEvents.ALLAY_DEATH, 1.0F, 1.2F);

    public static final VoiceProfile AXOLOTL = new VoiceProfile(
        SoundEvents.AXOLOTL_IDLE_AIR, SoundEvents.AXOLOTL_HURT, SoundEvents.AXOLOTL_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile CAMEL = new VoiceProfile(
        SoundEvents.CAMEL_AMBIENT, SoundEvents.CAMEL_HURT, SoundEvents.CAMEL_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile FROG = new VoiceProfile(
        SoundEvents.FROG_AMBIENT, SoundEvents.FROG_HURT, SoundEvents.FROG_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile RABBIT = new VoiceProfile(
        SoundEvents.RABBIT_AMBIENT, SoundEvents.RABBIT_HURT, SoundEvents.RABBIT_DEATH, 1.0F, 1.25F);

    public static final VoiceProfile SNIFFER = new VoiceProfile(
        SoundEvents.SNIFFER_IDLE, SoundEvents.SNIFFER_HURT, SoundEvents.SNIFFER_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile WARDEN = new VoiceProfile(
        SoundEvents.WARDEN_AMBIENT, SoundEvents.WARDEN_HURT, SoundEvents.WARDEN_DEATH, 1.0F, 0.85F);

    public static final VoiceProfile BLAZE = new VoiceProfile(
        SoundEvents.BLAZE_AMBIENT, SoundEvents.BLAZE_HURT, SoundEvents.BLAZE_DEATH, 1.0F, 1.0F);

    public static final VoiceProfile GOAT = new VoiceProfile(
        SoundEvents.GOAT_AMBIENT, SoundEvents.GOAT_HURT, SoundEvents.GOAT_DEATH, 1.0F, 1.0F);
}
