package atomicstryker.necromancy.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import atomicstryker.necromancy.network.NetworkHelper.IPacket;
import com.sirolf2009.necromancy.entity.EntityTear;
import com.sirolf2009.necromancy.entity.EntityTearBlood;

public class TearShotPacket implements IPacket
{
    private String user;
    private boolean blood;

    public TearShotPacket() {}

    public TearShotPacket(String username, boolean blood)
    {
        this.user = username;
        this.blood = blood;
    }

    @Override
    public void writeBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        ByteBufUtils.writeUTF8String(bytes, user);
        bytes.writeBoolean(blood);
    }

    @Override
    public void readBytes(ChannelHandlerContext ctx, ByteBuf bytes)
    {
        user = ByteBufUtils.readUTF8String(bytes);
        blood = bytes.readBoolean();

        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance()
                .getPlayerList().getPlayerByUsername(user);
        if (player == null) return;

        EntityTear tear = blood
                ? new EntityTearBlood(player.world, player)
                : new EntityTear(player.world, player);

        if (blood)
        {
            if (player.getHealth() > 0.5F) player.attackEntityFrom(DamageSource.STARVE, 0.5F);
            else return;
        }
        else
        {
            if (player.getFoodStats().getFoodLevel() > 3) player.getFoodStats().addExhaustion(3F);
            else return;
        }

        player.world.spawnEntity(tear);
        SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation("necromancy:tear"));
        if (sound != null) player.playSound(sound, 1.0F, 1.0F / (player.getRNG().nextFloat() * 0.4F + 0.8F));
    }
}
