package com.sirolf2009.necromancy.network.payload;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.entity.EntityTearBlood;
import com.sirolf2009.necromancy.entity.NecromancyEntities;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-bound payload sent when the player presses the
 * {@code key.necromancy.shoot_tear} key while wearing
 * {@link com.sirolf2009.necromancy.item.ItemIsaacsHead}.
 *
 * <p>Direct port of legacy {@code TearShotPacket}.  The server resolves
 * the active player, validates the helmet equip and spawns a fresh
 * {@link EntityTearBlood} aimed away from the player's eye direction.
 */
public record TearShotPayload() implements CustomPacketPayload {

    public static final Type<TearShotPayload> TYPE = new Type<>(Reference.rl("tear_shot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TearShotPayload> STREAM_CODEC =
        StreamCodec.unit(new TearShotPayload());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /**
     * Server handler.  Validates the wearer, creates a tear and pushes it in
     * the look vector at the legacy initial speed (1.0).  The tear inherits
     * the player as its owner so kills credit the player.
     */
    public static void handle(TearShotPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.isEmpty() || helmet.getItem() != NecromancyItems.ISAACS_HEAD.get()) return;
            EntityTearBlood t = new EntityTearBlood(NecromancyEntities.TEAR_BLOOD.get(), player.level());
            t.setOwner(player);
            t.moveTo(player.getX(), player.getEyeY() - 0.10, player.getZ(),
                     player.getYRot(), player.getXRot());
            t.shootFromRotation(player, player.getXRot(), player.getYRot(), 0F, 1.0F, 1.0F);
            player.level().addFreshEntity(t);
            var rl = Reference.rl("tear");
            var snd = BuiltInRegistries.SOUND_EVENT.get(rl);
            if (snd != null) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    snd, player.getSoundSource(), 1.0F,
                    1.0F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
            }
        });
    }
}
