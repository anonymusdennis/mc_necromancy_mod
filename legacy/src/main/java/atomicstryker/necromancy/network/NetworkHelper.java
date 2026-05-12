package atomicstryker.necromancy.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Helper class wrapping Forge's Netty channel system into a simple packet API.
 * Unchanged from 1.7.10 — FMLEmbeddedChannel still exists in 1.12.2.
 */
public class NetworkHelper
{
    private final FMLEmbeddedChannel clientChannel;
    private final FMLEmbeddedChannel serverChannel;
    private final Set<Class<? extends IPacket>> registeredClasses;
    private volatile boolean sending;

    @SafeVarargs
    public NetworkHelper(String channelName, Class<? extends IPacket>... packetClasses)
    {
        EnumMap<Side, FMLEmbeddedChannel> pair = NetworkRegistry.INSTANCE.newChannel(
                channelName, new ChannelCodec(packetClasses), new ChannelHandler());
        clientChannel = pair.get(Side.CLIENT);
        serverChannel = pair.get(Side.SERVER);
        registeredClasses = new HashSet<>();
        for (Class<? extends IPacket> c : packetClasses) registeredClasses.add(c);
    }

    public interface IPacket
    {
        void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes);
        void readBytes(ChannelHandlerContext ctx, ByteBuf bytes);
    }

    public void sendPacketToServer(IPacket packet)
    {
        checkAndSync(packet.getClass());
        clientChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        clientChannel.writeOutbound(packet);
        sending = false;
    }

    public void sendPacketToPlayer(IPacket packet, EntityPlayerMP player)
    {
        checkAndSync(packet.getClass());
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        serverChannel.writeOutbound(packet);
        sending = false;
    }

    public void sendPacketToAllPlayers(IPacket packet)
    {
        checkAndSync(packet.getClass());
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        serverChannel.writeOutbound(packet);
        sending = false;
    }

    public void sendPacketToAllAroundPoint(IPacket packet, TargetPoint tp)
    {
        checkAndSync(packet.getClass());
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(tp);
        serverChannel.writeOutbound(packet);
        sending = false;
    }

    private void checkAndSync(Class<? extends IPacket> clazz)
    {
        if (!registeredClasses.contains(clazz))
            throw new RuntimeException("Unknown packet type: " + clazz);
        while (sending) Thread.yield();
        sending = true;
    }

    private class ChannelCodec extends FMLIndexedMessageToMessageCodec<IPacket>
    {
        @SafeVarargs
        ChannelCodec(Class<? extends IPacket>... classes)
        {
            for (int i = 0; i < classes.length; i++) addDiscriminator(i, classes[i]);
        }

        @Override
        public void encodeInto(ChannelHandlerContext ctx, IPacket msg, ByteBuf buf) throws Exception
        {
            msg.writeBytes(ctx, buf);
        }

        @Override
        public void decodeInto(ChannelHandlerContext ctx, ByteBuf buf, IPacket msg)
        {
            msg.readBytes(ctx, buf);
        }
    }

    @Sharable
    public class ChannelHandler extends SimpleChannelInboundHandler<IPacket>
    {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, IPacket msg) throws Exception {}
    }
}
