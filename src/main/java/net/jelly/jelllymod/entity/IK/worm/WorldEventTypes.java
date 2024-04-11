package net.jelly.jelllymod.entity.IK.worm;

import team.lodestar.lodestone.systems.worldevent.WorldEventType;
import static team.lodestar.lodestone.registry.common.LodestoneWorldEventTypeRegistry.registerEventType;


public class WorldEventTypes {
    public static WorldEventType WORM_BREACH = registerEventType(new WorldEventType("worm_breach", WormBreachWorldEvent::new));
}
