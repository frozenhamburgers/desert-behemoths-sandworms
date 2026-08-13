package net.jelly.sandworm_mod.entity.IK.worm;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WormSegmentModel extends GeoModel<WormSegment> {
	@Override
	public ResourceLocation getModelResource(WormSegment wormSegment) {
		return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "geo/worm_segment.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(WormSegment wormSegment) {
		return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "textures/entity/worm_segment_texture.png");
	}

	@Override
	public ResourceLocation getAnimationResource(WormSegment wormSegment) {
		return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "animations/no_animation.json");
	}

}