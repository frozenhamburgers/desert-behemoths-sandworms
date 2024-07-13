package net.jelly.jelllymod.registry.common;

import net.jelly.jelllymod.JellyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class DamageTypesRegistry {
    public static final ResourceKey<DamageType> WORM = register("worm");

    private static ResourceKey<DamageType> register(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(JellyMod.MODID, name));
    }

    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(WORM, new DamageType("worm", 0.1F));
    }
}
