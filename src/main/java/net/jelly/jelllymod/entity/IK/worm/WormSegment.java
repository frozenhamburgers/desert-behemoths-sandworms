package net.jelly.jelllymod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.entity.IK.ChainSegment;
import net.jelly.jelllymod.entity.IK.KinematicChainEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;

public class WormSegment extends ChainSegment implements GeoEntity {
    private static int STAGE_TRACK = 0;
    private static int STAGE_BURROW = 1;
    private int stage = 0;
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public WormSegment(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        this.lookAt(EntityAnchorArgument.Anchor.FEET, this.position().add(this.getDirectionVector()));

        if(!this.level().isClientSide()) {
            List<Entity> collidingEntities = level().getEntities(this, this.getBoundingBox());
            for (int i = 0; i < collidingEntities.size(); i++) {
                if (collidingEntities.get(i) instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) (collidingEntities.get(i));
                    System.out.println(target);
                    if(target.hurtTime == 0) {
                        Vec3 vec3 = (target.position().subtract(this.position())).normalize();
                        System.out.println(vec3);
                        target.hurt(this.damageSources().explosion(this, target), getDamage());
                        Vec3 knockback = getKB();
                        target.addDeltaMovement(new Vec3(vec3.x*knockback.x, vec3.y*knockback.y, vec3.z*knockback.z));
                    }
                }
            }
        }

    }

    protected float getDamage() { return 10.0f; }
    protected Vec3 getKB() {return new Vec3(3,2,3); }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
