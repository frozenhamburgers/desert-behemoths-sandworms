package net.jelly.sandworm_mod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;

public class WormHeadSegment extends WormSegment implements GeoEntity {
    private static final ParticleEmitterInfo SPLASH = new ParticleEmitterInfo(new ResourceLocation(SandwormMod.MODID, "herald"));
    public WormHeadSegment(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        // AAALevel.addParticle(this.level(), false, SPLASH.clone().position(this.position()));

    }

    @Override
    protected double getDamage() { return super.getDamage()* CommonConfigs.HEAD_MULTIPLIER.get(); }
    @Override
    protected Vec3 getKB() {return new Vec3(5,2,5); }
}
