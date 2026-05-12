package com.sirolf2009.necromancy.client.renderer;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.sirolf2009.necromancy.client.model.ModelMinion;
import com.sirolf2009.necromancy.entity.EntityMinion;

@SideOnly(Side.CLIENT)
public class RenderMinion extends RenderLiving<EntityMinion>
{
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("necromancy:textures/entity/minion.png");

    public RenderMinion(RenderManager manager)
    {
        super(manager, new ModelMinion(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMinion entity)
    {
        String[] parts = entity.getBodyPartsNames();
        if (parts != null && parts[0] != null && !parts[0].equals("UNDEFINED"))
        {
            com.sirolf2009.necroapi.NecroEntityBase mob = com.sirolf2009.necroapi.NecroEntityRegistry.registeredEntities.get(parts[0]);
            if (mob != null && mob.texture != null) return mob.texture;
        }
        return DEFAULT_TEXTURE;
    }
}
