package net.jelly.sandworm_mod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.advancements.AdvancementTriggerRegistry;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
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

    public void hitSandwormHead(Entity sourcePlayer, int dmg) {
        WormChainEntity wormChain = this.getOwner();
        if (wormChain != null) wormChain.blastHit(dmg);
        if(sourcePlayer == null) {
            Vec3 explosionPos = this.position();
            sourcePlayer = this.level().getNearestPlayer(explosionPos.x, explosionPos.y, explosionPos.z, 100.0, true);
        }
        if(sourcePlayer instanceof ServerPlayer) {
            AdvancementTriggerRegistry.FIRST_BLAST.trigger((ServerPlayer) sourcePlayer);
            if (wormChain.dmgTaken >= CommonConfigs.HEALTH.get()) AdvancementTriggerRegistry.SANDWORM_FLEE.trigger((ServerPlayer) sourcePlayer);
        }
    }
}
