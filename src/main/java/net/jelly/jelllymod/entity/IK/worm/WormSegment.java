package net.jelly.jelllymod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.entity.IK.ChainSegment;
import net.jelly.jelllymod.entity.IK.KinematicChainEntity;
import net.jelly.jelllymod.registry.common.DamageTypesRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
import java.util.UUID;

public class WormSegment extends ChainSegment implements GeoEntity {
    private static int STAGE_TRACK = 0;
    private static int STAGE_BURROW = 1;
    private int stage = 0;
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private UUID ownerEntityUUID;
    private int discardTimer = 0;
    private DamageSource dmgSource =
            new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypesRegistry.WORM));

    public WormSegment(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
        this.lookAt(EntityAnchorArgument.Anchor.FEET, this.position().add(this.getDirectionVector()));

        if(!this.level().isClientSide()) {
            // check for owner
            if(getOwner() == null) {
                if(this.discardTimer < 120) discardTimer++;
                else this.discard();
            }
            else discardTimer = 0;

            // collisions & deal damage & kb
            List<Entity> collidingEntities = level().getEntities(this, this.getBoundingBox());
            for (int i = 0; i < collidingEntities.size(); i++) {
                if (collidingEntities.get(i) instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) (collidingEntities.get(i));
                    if(target.hurtTime == 0) {
                        Vec3 vec3 = (target.position().subtract(this.position())).normalize();
                        target.hurt(dmgSource, getDamage());
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

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        ownerEntityUUID = pCompound.getUUID("chain_entity_UUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putUUID("chain_entity_UUID", ownerEntityUUID);
    }

    public void setOwnerEntityUUID(UUID uuid) {
        this.ownerEntityUUID = uuid;
    }
    public WormChainEntity getOwner() {
        List<WormChainEntity> nearbyChainEntities = this.level().getEntitiesOfClass(
                WormChainEntity.class,
                new AABB(this.position().add(200, 200, 200), this.position().add(-200, -200, -200))
        );
        return
                nearbyChainEntities.stream()
                        .filter(obj -> obj.getStringUUID().equals(ownerEntityUUID.toString()))
                        .findFirst()
                        .orElse(null);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }
}
