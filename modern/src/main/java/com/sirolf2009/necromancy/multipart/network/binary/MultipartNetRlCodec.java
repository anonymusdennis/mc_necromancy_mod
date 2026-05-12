package com.sirolf2009.necromancy.multipart.network.binary;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

final class MultipartNetRlCodec {

    private static final int MAX_RL_UTF = 32767;

    private MultipartNetRlCodec() {}

    static void writeRl(FriendlyByteBuf buf, ResourceLocation rl) {
        buf.writeUtf(rl.toString(), MAX_RL_UTF);
    }

    static ResourceLocation readRl(FriendlyByteBuf buf) {
        return ResourceLocation.parse(buf.readUtf(MAX_RL_UTF));
    }
}
