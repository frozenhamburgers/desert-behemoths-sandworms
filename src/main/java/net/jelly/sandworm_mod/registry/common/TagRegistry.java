package net.jelly.sandworm_mod.registry.common;

import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class TagRegistry {
    public static final TagKey<Biome> SANDWORM_SPAWNABLE = TagKey.create(Registries.BIOME, new ResourceLocation(SandwormMod.MODID, "can_spawn_sandworms"));
}
