package net.jelly.jelllymod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.jelllymod.JellyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;
import team.lodestar.lodestone.systems.worldevent.WorldEventType;

import java.awt.*;

public class WormBreachWorldEvent extends WorldEventInstance {

    private static ParticleEmitterInfo SPLASH = new ParticleEmitterInfo(new ResourceLocation(JellyMod.MODID, "sandsplash"));
    public Vec3 sourcePos;
    public WormBreachWorldEvent() {
        super(WorldEventTypes.WORM_BREACH);
    }
    public WormBreachWorldEvent setPosition(Vec3 sourcePos) {
        System.out.println("SETPOS working");
        this.sourcePos = sourcePos;
        return this;
    }

    public void createBreach(ServerLevel level) {
        System.out.println("create breach wokring");
        level.playSound(null, sourcePos.x, sourcePos.y, sourcePos.z, SoundEvents.WITHER_DEATH, SoundSource.BLOCKS, 1f, 1.8f);
    }

    @Override
    public void tick(Level level) {
        super.tick(level);
        if(level.isClientSide()) AAALevel.addParticle(level, true, SPLASH.clone().position(sourcePos));
    }
}
