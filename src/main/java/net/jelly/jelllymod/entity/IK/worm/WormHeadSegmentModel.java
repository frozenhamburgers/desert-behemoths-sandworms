package net.jelly.jelllymod.entity.IK.worm;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.jelly.jelllymod.JellyMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WormHeadSegmentModel extends GeoModel<WormHeadSegment> {
	@Override
	public ResourceLocation getModelResource(WormHeadSegment wormHeadSegment) {
		return new ResourceLocation(JellyMod.MODID, "geo/worm_segment_head.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(WormHeadSegment wormHeadSegment) {
		return new ResourceLocation(JellyMod.MODID, "textures/entity/worm_head_segment_texture.png");
	}

	@Override
	public ResourceLocation getAnimationResource(WormHeadSegment wormHeadSegment) {
		return new ResourceLocation(JellyMod.MODID, "animations/sapphire_armor.animation.json");
	}
}