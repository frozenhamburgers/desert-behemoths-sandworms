package net.jelly.jelllymod.worldevents;

import net.jelly.jelllymod.registry.common.WorldEventRegistry;
import net.jelly.jelllymod.vfx.SinkholeFx;
import net.jelly.jelllymod.vfx.SinkholePostProcessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

public class WormRippleWorldEvent extends WorldEventInstance {
    public Vec3 position;
    public int lifetime = 0;
    public SinkholeFx fx;

    public int in = 100;
    public int out = 30;

    public WormRippleWorldEvent() {
        super(WorldEventRegistry.WORM_BREACH);
    }

    public WormRippleWorldEvent spawnRipple(Vec3 pos) {
        position = pos;
        fx = new SinkholeFx(pos.toVector3f(), 0, 0, 0f,0);
        SinkholePostProcessor.INSTANCE.addFxInstance(fx);
        SinkholePostProcessor.INSTANCE.setActive(true);
        return this;
    }

    @Override
    public void tick(Level level) {
        if(lifetime <= in) {
            fx.radius = lerp(0, 20, (float) lifetime / in);
            fx.speed = lerp(0, 4, (float) lifetime / in);
            fx.magnitude = lerp(0, 0.2f, (float) lifetime / in);
            fx.frequency = lerp(0, 4, (float) lifetime / in);
        }
        else {
            fx.radius = lerp(20, 0, (float) lifetime / (in+out));
            fx.speed = lerp(4, 0, (float) (lifetime-in) / out);
            fx.magnitude = lerp(0.2f, 0, (float) (lifetime-in) / out);
            // fx.frequency = lerp(4, 0, (float) (lifetime-in) / out);
        }

        lifetime++;
        if(lifetime >= in+out) {
            fx.remove();
            this.end(level);
        }
        super.tick(level);
    }

    float lerp(float a, float b, float f)
    {
        return (float)(a * (1.0 - f)) + (b * f);
    }
}
