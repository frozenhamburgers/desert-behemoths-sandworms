package net.jelly.sandworm_mod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.sandworm_mod.SandwormMod;
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
    protected float getDamage() { return 20.0f; }
    @Override
    protected Vec3 getKB() {return new Vec3(5,2,5); }
}
