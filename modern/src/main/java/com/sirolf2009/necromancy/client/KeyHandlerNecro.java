package com.sirolf2009.necromancy.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.item.NecromancyItems;
import com.sirolf2009.necromancy.network.payload.TearShotPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Modern equivalent of legacy {@code KeyHandlerNecro}.
 *
 * <p>Registers the "Shoot Tear" key mapping and -- while the player wears
 * {@link NecromancyItems#ISAACS_HEAD} -- forwards a {@link TearShotPayload}
 * to the server every {@code 333 ms} the binding is held.
 *
 * <p>The legacy mod had two separate keys (normal vs. blood); we ship one
 * binding because the blood-tear was server-decided based on whether the
 * helmet was in trophy form.  Adding a second binding is straightforward.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public final class KeyHandlerNecro {

    private static final String CATEGORY = "key.categories." + Reference.MOD_ID;

    public static final KeyMapping SHOOT_TEAR =
        new KeyMapping("key.necromancy.shoot_tear",
            InputConstants.Type.KEYSYM, InputConstants.KEY_F, CATEGORY);

    private static long nextShotMs = 0;

    private KeyHandlerNecro() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SHOOT_TEAR);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isAlive() || mc.screen != null) return;
        var helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || helmet.getItem() != NecromancyItems.ISAACS_HEAD.get()) return;
        long now = System.currentTimeMillis();
        if (SHOOT_TEAR.isDown() && now > nextShotMs) {
            PacketDistributor.sendToServer(new TearShotPayload());
            nextShotMs = now + 333L;
        }
    }
}
