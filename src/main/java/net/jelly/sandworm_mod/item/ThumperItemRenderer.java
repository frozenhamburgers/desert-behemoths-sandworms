package net.jelly.sandworm_mod.item;

import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ThumperItemRenderer extends GeoItemRenderer<ThumperItem> {
    public ThumperItemRenderer() {
        super(new ThumperItemModel());
    }
}
