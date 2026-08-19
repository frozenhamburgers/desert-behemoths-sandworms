package net.jelly.sandworm_mod.registry.common;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.worldevents.WormBreachWorldEvent;
import net.jelly.sandworm_mod.worldevents.WormRippleWorldEvent;
import net.jelly.sandworm_mod.worldevents.SonicBoomWorldEvent;
import net.minecraft.resources.ResourceLocation;
import team.lodestar.lodestone.systems.worldevent.WorldEventType;

import static team.lodestar.lodestone.registry.common.LodestoneWorldEventTypeRegistry.registerEventType;

public class WorldEventRegistry {
    public static WorldEventType WORM_BREACH = registerEventType(new WorldEventType(ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "worm_breach"), WormBreachWorldEvent::new));
    public static WorldEventType WORM_RIPPLE = registerEventType(new WorldEventType(ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "worm_ripple"), WormRippleWorldEvent::new));
    public static WorldEventType SONIC_BOOM = registerEventType(new WorldEventType(ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "sonic_boom"), SonicBoomWorldEvent::new, true));
}
