package net.jelly.sandworm_mod.item;

import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ThumperItemModel extends GeoModel<ThumperItem> {

    @Override
    public ResourceLocation getModelResource(ThumperItem thumperItem) {
        return new ResourceLocation(SandwormMod.MODID, "geo/thumper.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ThumperItem thumperItem) {
        return new ResourceLocation("minecraft", "textures/block/piston_side.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ThumperItem thumperItem) {
        return new ResourceLocation(SandwormMod.MODID, "animations/thumper.animation.json");
    }
}
