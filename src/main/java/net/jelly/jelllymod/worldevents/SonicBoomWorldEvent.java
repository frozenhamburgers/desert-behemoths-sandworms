package net.jelly.jelllymod.worldevents;

import net.jelly.jelllymod.registry.common.WorldEventRegistry;
import net.jelly.jelllymod.vfx.SinkholeFx;
import net.jelly.jelllymod.vfx.SinkholePostProcessor;
import net.jelly.jelllymod.vfx.SonicBoomFx;
import net.jelly.jelllymod.vfx.SonicBoomPostProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

public class SonicBoomWorldEvent extends WorldEventInstance {
    private Entity followEntity;
    public int lifetime = 0;
    public SonicBoomFx fx;

    private int in = 10;
    private int sustain = 30;
    private int out = 30;

    private static float maxRadius = 1;
    private static float maxSpeed = 50;
    private static float maxMagnitude = 0.03f;
    private static float maxFrequency = 160;

    public SonicBoomWorldEvent() {
        super(WorldEventRegistry.WORM_BREACH);
    }

    public SonicBoomWorldEvent spawnRipple(Entity followEntity) {
        this.followEntity = followEntity;
        fx = new SonicBoomFx(followEntity.position().toVector3f(), 0, 0, 0,0);
        SonicBoomPostProcessor.INSTANCE.addFxInstance(fx);
        SonicBoomPostProcessor.INSTANCE.setActive(true);
        return this;
    }

    @Override
    public void tick(Level level) {
        if(followEntity != null) fx.center = followEntity.position().toVector3f();
        if(lifetime <= in) {
            fx.radius = lerp(0, maxRadius, (float) lifetime / in);
            fx.speed = lerp(0, maxSpeed, (float) lifetime / in);
            fx.magnitude = lerp(0, maxMagnitude, (float) lifetime / in);
            fx.frequency = lerp(0, maxFrequency, (float) lifetime / in);
        }
        else if (lifetime >= in+sustain){
            // fx.radius = lerp(maxRadius, 0, (float) lifetime / (in+sustain+out));
            // fx.speed = lerp(maxSpeed, 0, (float) lifetime / (in+sustain+out));
            fx.magnitude = lerp(maxMagnitude, 0, (float) lifetime / (in+sustain+out));
            // fx.frequency = lerp(4, 0, (float) (lifetime-in) / out);
        }
        lifetime++;
        if(lifetime >= in+sustain+out) {
            fx.remove();
            this.end(level);
        }
    }

    float lerp(float a, float b, float f)
    {
        return (float)(a * (1.0 - f)) + (b * f);
    }
}

