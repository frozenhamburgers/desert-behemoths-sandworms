package net.jelly.sandworm_mod.helper;

import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.registry.common.TagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;

public class BiomeHelper {

    public static boolean isDesertBiome(ServerLevel level, BlockPos blockPos) {
        Holder<Biome> thisBiome = level.getBiomeManager().getBiome(blockPos);
        return (CommonConfigs.DEFAULT_SPAWNING.get() && thisBiome.is(BiomeTags.SPAWNS_GOLD_RABBITS)) || thisBiome.is(TagRegistry.SANDWORM_SPAWNABLE);
    }

    public static boolean isDesertBiome(Entity entity) {
        return isDesertBiome(entity.getServer().getLevel(entity.level().dimension()), entity.blockPosition());
    }

}
