package net.jelly.sandworm_mod.entity.hook;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.entity.IK.worm.WormSegment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WormHookModel extends GeoModel<WormHook> {
	@Override
	public ResourceLocation getModelResource(WormHook wormHook) {
		return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "geo/worm_hook.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(WormHook wormHook) {
		return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "textures/item/worm_rod.png");
	}

	@Override
	public ResourceLocation getAnimationResource(WormHook wormHook) {
		return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "animations/no_animation.json");
	}
}