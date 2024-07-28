package net.jelly.sandworm_mod.block.thumper;

import net.jelly.sandworm_mod.block.ThumperBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ThumperRenderer extends GeoBlockRenderer<ThumperBlockEntity> {


    public ThumperRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(new ThumperModel());
    }

    @Override
    public RenderType getRenderType(ThumperBlockEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
