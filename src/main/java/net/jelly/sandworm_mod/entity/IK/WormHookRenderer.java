package net.jelly.sandworm_mod.entity.IK;

import net.jelly.sandworm_mod.entity.WormHook;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;

public class WormHookRenderer extends FishingHookRenderer {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/zombie.png");;

    public WormHookRenderer(EntityRendererProvider.Context p_173958_) {
        super(p_173958_);
    }

    @Override
    public ResourceLocation getTextureLocation(FishingHook pEntity) {
        return TEXTURE_LOCATION;
    }

}
