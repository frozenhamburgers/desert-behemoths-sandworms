package net.jelly.sandworm_mod.entity.IK.worm;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class WormControllerRenderer extends EntityRenderer {
    public WormControllerRenderer(EntityRendererProvider.Context p_173958_) {
        super(p_173958_);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity pEntity) {
        return null;
    }

}
