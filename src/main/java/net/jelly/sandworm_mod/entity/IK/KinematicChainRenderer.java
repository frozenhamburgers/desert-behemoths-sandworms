package net.jelly.sandworm_mod.entity.IK;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class KinematicChainRenderer extends EntityRenderer {
    public KinematicChainRenderer(EntityRendererProvider.Context p_173958_) {
        super(p_173958_);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity pEntity) {
        return null;
    }

}
