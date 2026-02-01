package net.jelly.sandworm_mod.worldevents;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.jelly.sandworm_mod.vfx.SonicBoomFx;
import net.jelly.sandworm_mod.vfx.SonicBoomPostProcessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

public class SonicBoomWorldEvent extends WorldEventInstance {

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

    public SonicBoomWorldEvent setFollowEntity(Entity followEntity) {
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
        if(this.level == null) {
            return;
        }

        if (FMLEnvironment.dist.isClient()) {
            if (followEntity == null && followEntityId != Integer.MIN_VALUE) {
                SandwormMod.LOGGER.info("Follow entity is null, trying to get it from id: {}", followEntityId);
                followEntity = this.level.getEntity(followEntityId);
                followEntityId = Integer.MIN_VALUE;
            }

            if (fx == null) {
                SandwormMod.LOGGER.info("Creating Sonic Boom FX instance");
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
            if (FMLEnvironment.dist.isClient() && fx != null) {
                SandwormMod.LOGGER.info("Removing Sonic Boom FX instance");
                fx.remove();
                fx = null;
            }
            SandwormMod.LOGGER.info("Sonic Boom World Event ended");
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
        SandwormMod.LOGGER.info("Serializing Sonic Boom World Event, followEntity: {}", followEntity);
        if (followEntity != null) {
            tag.putInt("followEntityId", followEntity.getId());
        }
        tag.putInt("lifetime", lifetime);
        return super.serializeNBT(tag);
    }

    @Override
    public WorldEventInstance deserializeNBT(CompoundTag tag) {
        SandwormMod.LOGGER.info("Deserializing Sonic Boom World Event on {}, followEntityId: {}",
            FMLEnvironment.dist.isClient() ? "CLIENT" : "SERVER",
            tag.contains("followEntityId") ? tag.getInt("followEntityId") : "none");
        if (tag.contains("followEntityId")) {
            followEntityId = tag.getInt("followEntityId");
        }
        lifetime = tag.getInt("lifetime");
        return super.deserializeNBT(tag);
    }
}

