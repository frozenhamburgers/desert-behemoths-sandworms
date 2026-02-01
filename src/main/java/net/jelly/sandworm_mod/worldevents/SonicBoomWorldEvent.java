package net.jelly.sandworm_mod.worldevents;

import com.mojang.logging.LogUtils;
import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.jelly.sandworm_mod.vfx.SonicBoomFx;
import net.jelly.sandworm_mod.vfx.SonicBoomPostProcessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

public class SonicBoomWorldEvent extends WorldEventInstance {

    private static final Logger LOGGER = LogUtils.getLogger();

    private Entity followEntity;
    private int followEntityId = Integer.MIN_VALUE;
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
        super(WorldEventRegistry.SONIC_BOOM);
    }

    public SonicBoomWorldEvent spawnRipple(Entity followEntity) {
        this.followEntity = followEntity;
        if(followEntity == null) {
            this.discarded = true;
            return this;
        }
        this.followEntityId = followEntity.getId();
        return this;
    }

    @Override
    public void tick(Level level) {
        LOGGER.info("Ticking Sonic Boom World Event, followEntityId: {}, level: {}", followEntityId, level.isClientSide);
        if(this.level == null) {
            LOGGER.info("Level is null, discarding Sonic Boom World Event");
            return;
        }

        if (level.isClientSide()) {
            LOGGER.info("followEntity: {}, followEntityId: {}", followEntity, followEntityId);
            if (followEntity == null && followEntityId != Integer.MIN_VALUE) {
                LOGGER.info("Follow entity is null, trying to get it from id: {}", followEntityId);
                followEntity = this.level.getEntity(followEntityId);
                followEntityId = Integer.MIN_VALUE;
            }

            if (fx == null) {
                LOGGER.info("Creating Sonic Boom FX instance");
                fx = new SonicBoomFx(followEntity.position().toVector3f(), 0, 0, 0,0);
                SonicBoomPostProcessor.INSTANCE.addFxInstance(fx);
                SonicBoomPostProcessor.INSTANCE.setActive(true);
            }

            fx.center = followEntity.position().toVector3f();
            if (lifetime <= in) {
                fx.radius = lerp(0, maxRadius, (float) lifetime / in);
                fx.speed = lerp(0, maxSpeed, (float) lifetime / in);
                fx.magnitude = lerp(0, maxMagnitude, (float) lifetime / in);
                fx.frequency = lerp(0, maxFrequency, (float) lifetime / in);
            } else if (lifetime >= in + sustain) {
                // fx.radius = lerp(maxRadius, 0, (float) lifetime / (in+sustain+out));
                // fx.speed = lerp(maxSpeed, 0, (float) lifetime / (in+sustain+out));
                fx.magnitude = lerp(maxMagnitude, 0, (float) lifetime / (in + sustain + out));
                // fx.frequency = lerp(4, 0, (float) (lifetime-in) / out);
            }
        }

        if (lifetime >= in + sustain + out) {
            LOGGER.info("Sonic Boom World Event ended");
            if (level.isClientSide() && fx != null) {
                fx.remove();
                fx = null;
            }
            this.end(level);
            return;
        }

        lifetime++;
    }

    float lerp(float a, float b, float f)
    {
        return (float)(a * (1.0 - f)) + (b * f);
    }

    @Override
    public boolean isClientSynced() {
        return true;
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag) {
        LOGGER.info("Serializing Sonic Boom World Event, followEntity: {}", followEntity);
        if (followEntity != null) {
            tag.putInt("followEntityId", followEntity.getId());
        }
        tag.putInt("lifetime", lifetime);
        return super.serializeNBT(tag);
    }

    @Override
    public WorldEventInstance deserializeNBT(CompoundTag tag) {
        LOGGER.info("Deserializing Sonic Boom World Event, followEntityId: {}", followEntityId);
        if (tag.contains("followEntityId")) {
            followEntityId = tag.getInt("followEntityId");
        }
        lifetime = tag.getInt("lifetime");
        return super.deserializeNBT(tag);
    }
}

