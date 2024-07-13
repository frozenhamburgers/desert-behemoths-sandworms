package net.jelly.jelllymod.worldevents;

import net.jelly.jelllymod.registry.client.ParticleRegistry;
import net.jelly.jelllymod.registry.common.WorldEventRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

public class WormBreachWorldEvent extends WorldEventInstance {
    public Vec3 position;
    public boolean spawnedParticles;
    public int lifetime = 0;

    public WormBreachWorldEvent() {
        super(WorldEventRegistry.WORM_BREACH);
    }

    public WormBreachWorldEvent setPosition(Vec3 pos) {
        position = pos;
        return this;
    }

    @Override
    public void tick(Level level) {
        lifetime++;
        if(lifetime >= 480) {
            this.discarded = true;
            this.end(level);
        }
        super.tick(level);
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag) {
        tag.putDouble("x", position.x);
        tag.putDouble("y", position.y);
        tag.putDouble("z", position.z);
        tag.putBoolean("spawnedparticles", spawnedParticles);
        tag.putDouble("age", lifetime);
        return super.serializeNBT(tag);
    }

    @Override
    public WorldEventInstance deserializeNBT(CompoundTag tag) {
        this.position = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        spawnedParticles = tag.getBoolean("spawnedparticles");
        lifetime = tag.getInt("age");
        return super.deserializeNBT(tag);
    }

    @Override
    public boolean isClientSynced() {
        return true;
    }
}
