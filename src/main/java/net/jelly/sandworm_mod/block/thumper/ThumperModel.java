package net.jelly.sandworm_mod.block.thumper;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.block.ThumperBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class ThumperModel extends GeoModel<ThumperBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ThumperBlockEntity thumperBlockEntity) {
        return new ResourceLocation(SandwormMod.MODID, "geo/thumper.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ThumperBlockEntity thumperBlockEntity) {
        return new ResourceLocation(SandwormMod.MODID, "textures/block/thumper.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ThumperBlockEntity thumperBlockEntity) {
        return new ResourceLocation(SandwormMod.MODID, "animations/thumper.animation.json");
    }
}
