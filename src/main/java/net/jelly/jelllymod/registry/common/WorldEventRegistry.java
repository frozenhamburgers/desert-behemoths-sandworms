package net.jelly.jelllymod.registry.common;

import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.worldevents.WormBreachWorldEvent;
import net.jelly.jelllymod.worldevents.WormRippleWorldEvent;
import net.minecraft.resources.ResourceLocation;
import team.lodestar.lodestone.systems.worldevent.WorldEventType;

import static team.lodestar.lodestone.registry.common.LodestoneWorldEventTypeRegistry.registerEventType;

public class WorldEventRegistry {
    public static WorldEventType WORM_BREACH = registerEventType(new WorldEventType(new ResourceLocation(JellyMod.MODID, "worm_breach"), WormBreachWorldEvent::new));
    public static WorldEventType WORM_RIPPLE = registerEventType(new WorldEventType(new ResourceLocation(JellyMod.MODID, "worm_ripple"), WormRippleWorldEvent::new));
}
