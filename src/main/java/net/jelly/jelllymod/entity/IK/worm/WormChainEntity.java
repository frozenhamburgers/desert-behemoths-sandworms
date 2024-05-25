package net.jelly.jelllymod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.entity.IK.ChainSegment;
import net.jelly.jelllymod.entity.IK.KinematicChainEntity;
import net.jelly.jelllymod.entity.ModEntities;
import net.jelly.jelllymod.networking.ModMessages;
import net.jelly.jelllymod.networking.packet.ExampleS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import team.lodestar.lodestone.network.screenshake.PositionedScreenshakePacket;
import team.lodestar.lodestone.registry.common.LodestonePacketRegistry;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WormChainEntity extends KinematicChainEntity {
    private static float SPEED_SCALE = 1.3f;
    private boolean breaching = false;
    private int soundFrequencyCount = 0;
    private static final ParticleEmitterInfo SAND_IMPACT = new ParticleEmitterInfo(new ResourceLocation(JellyMod.MODID, "sandimpact"));
    private static final ParticleEmitterInfo SAND_SMOKE = new ParticleEmitterInfo(new ResourceLocation(JellyMod.MODID, "sandsmoke"));
    public LivingEntity aggroTargetEntity;
    public boolean removed = false;
    private int discardTimer = 0;
    private int noPlayerDiscardTimer = 0;
    private boolean isChasing = false;
    public WormChainEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        if(!this.level().isClientSide()) {
            // save & load stuff
            if(this.segmentCount != 0 && segments.isEmpty()) {
                // restore segments listw
                List<ChainSegment> nearbyChainSegs = this.level().getEntitiesOfClass(
                        ChainSegment.class,
                        new AABB(this.position().add(200, 200, 200), this.position().add(-200, -200, -200))
                );
                for(int i=0; i<this.segmentCount; i++) {
                    UUID thisUUID = segmentsUUIDs.get(i);
                    this.segments.add(
                            nearbyChainSegs.stream()
                                    .filter(obj -> obj.getStringUUID().equals(thisUUID.toString()))
                                    .findFirst()
                                    .orElse(null)
                    );
                }
            }


            // if a segment is null or the segments array is lost
            // regenerate the worm
            // if(this.level().getNearestPlayer(this, 100) == null) this.discard();
            // check if worm is fully loaded
//            boolean wormFullyLoaded = true;
//            for (int j = 0; j < this.segments.size(); j++) {
//                if (segments.get(j) != null) {
//                    ChainSegment thisSegment = segments.get(j);
////                    if(thisSegment.level().hasChunksAt(thisSegment.blockPosition().getX()-80, thisSegment.blockPosition().getZ()-80,
////                                                       thisSegment.blockPosition().getX()+80, thisSegment.blockPosition().getX()+80))
////                    {
////                        wormFullyLoaded = false;
////                        break;
////                    }
//                }
//            }
            // if its not fully loaded, wait
//            if (!wormFullyLoaded) return;
            // if it is fully loaded and null segments are still discovered, discard
//            else {
            for (int j = 0; j < this.segments.size(); j++) {
                if (segments.get(j) == null) {
                    if(discardTimer < 100) discardTimer++;
                    else this.discard();
                    return;
                }
            }
            discardTimer = 0;
//            }

            // despawn if no players within 400 blocks for 3 seconds
            if(this.level().getNearestPlayer(this, 400) == null) {
                if(this.noPlayerDiscardTimer < 60) noPlayerDiscardTimer++;
                //else this.discard();
                return;
            }
            else noPlayerDiscardTimer = 0;

            // init spawn segments
            if(segmentCount == 0) {
                // addTailSegment(0.35f*5, new Vec3(0,1,0));
                for(int i=0; i<10; i++) addWormSegment(0.35f*5, new Vec3(1,0,0), new Vec3(7.5*((i+2)/11f),7.5*((i+2)/11f),5));
                for(int i=0; i<80; i++) addWormSegment(0.35f*5, new Vec3(1,0,0), new Vec3(7.5,7.5,5));
                addHeadSegment(0.35f*5, new Vec3(1,0,0), new Vec3(7.5,7.5,5));
                if(aggroTargetEntity == null) {
                    retarget(200);
                }
                ChainSegment head = segments.get(segmentCount - 1);
                if(aggroTargetEntity != null) {
                    System.out.println("initial head direction vector:" + aggroTargetEntity.position().subtract(this.position()).normalize());
                    Vec3 lookAtAggroEntity = aggroTargetEntity.position().subtract(this.position()).normalize();
                    for(int i=0; i<this.segmentCount; i++) {
                            segments.get(i).setDirectionVector(lookAtAggroEntity);
                    }
                }
            }

            // keep position on root
            // update upVector for every tick
            if (!segments.isEmpty()) {
                setPos(segments.get(0).position());
                Vec3 rootToEnd = (segments.get(segmentCount-1).position().subtract(segments.get(0).position())).cross(new Vec3(0,0,1));
                for(int i=0; i<this.segmentCount; i++) {
                    if(segments.get(i) != null) {
                        segments.get(i).setUpVector(rootToEnd);
                    }
                }
            }

            // AI
            if(!segments.isEmpty()) {
                ChainSegment head = segments.get(segmentCount - 1);

                if(aggroTargetEntity == null || aggroTargetEntity.isRemoved() || aggroTargetEntity.isDeadOrDying()) {
                    retarget(200);
                }
                // AI
                else {
                    // if too far, chase by dolphining
                    //System.out.println(head.position().subtract(aggroTargetEntity.position()).horizontalDistance());
                    if (head.position().subtract(aggroTargetEntity.position()).horizontalDistance() > 50) {
                        if(!isChasing) {
                            Vec3 towardTarget = (aggroTargetEntity.position().subtract(head.position())).normalize().multiply(20,0,20);
                            goal = head.position().add(towardTarget.x, 0, towardTarget.z);
                            goal = new Vec3(goal.x, aggroTargetEntity.getY(), goal.z);
                            System.out.println("chasing:" + goal);
                            isChasing = true;
                        }
                        else if(head.position().subtract(goal).horizontalDistance() <= 10) {
                            Vec3 towardTarget = (aggroTargetEntity.position().subtract(head.position())).normalize().multiply(20,0,20);
                            goal = head.position().add(towardTarget.x, 0, towardTarget.z);
                            goal = new Vec3(goal.x, aggroTargetEntity.getY(), goal.z);
                            System.out.println("re chasing:" + goal);
                        }
                    }
                    else isChasing = false;

                    // if not chasing, assign goal accordingly
                    if(!isChasing) {
                        if (!(stage == 0 && head.distanceTo(aggroTargetEntity) < 15 * SPEED_SCALE))
                            goal = aggroTargetEntity.position();
                        else if (goal == null) goal = aggroTargetEntity.position();
                    }

                    if (this.goal != null) {
                        var impactBuilder = WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE);
                        Color startingColor = new Color(255, 123, 13);
                        Color endingColor = new Color(255, 221, 135);
                        impactBuilder
                                .setScaleData(GenericParticleData.create(random.nextFloat() * 3 + 2, random.nextFloat() * 5 + 3).build())
                                .setTransparencyData(GenericParticleData.create(0.5f, 0.1f).setEasing(Easing.QUAD_IN_OUT).build())
                                .setColorData(ColorParticleData.create(startingColor, endingColor).setCoefficient(1.4f).setEasing(Easing.ELASTIC_IN_OUT).build())
                                .setLifetime(15)
                                .setSpinData(SpinParticleData.create(random.nextFloat(), random.nextFloat()).build())
                                .enableNoClip()
                                .setShouldCull(false)
                                .spawn(this.level(), goal.x, goal.y, goal.z);
                    }

                    // apply target velocity to target
                    target = head.position().add(targetV);
                    Vec3 moveTowardVec = (goal.subtract(head.position())).normalize();

                    // control
                    if (stage == 0) {
                        applyAcceleration(moveTowardVec.scale(0.1 * SPEED_SCALE));

                        // if worm is close enough & not "looking" at target, it has charged past it
                        if (moveTowardVec.dot(head.getDirectionVector()) < 0.25 || !(this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox())))
                            stage = 1;
                    }
                    // no control, charging
                    if (stage == 1) {
                        // if in the air
                        if (!(this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox()))) {
                            if(aggroTargetEntity.position().y - head.position().y > 20) retarget(30);
                            // gravity
                            applyAcceleration(new Vec3(0, -0.0375, 0));
                            // small targeting acceleration
                            if (moveTowardVec.y < 0) moveTowardVec = new Vec3(moveTowardVec.x, 0, moveTowardVec.z);
                            if (goal.y - head.position().y < 20) applyAcceleration(moveTowardVec.scale(0.01));
                        }
                        // otherwise, must be 16 blocks away & at least 10 blocks deeper than target to retarget
                        else if (head.position().distanceTo(goal) > 4 * SPEED_SCALE && (goal.y - head.position().y >= 20 * SPEED_SCALE))
                            stage = 0;
                        else applyAcceleration(new Vec3(0, -0.02 * SPEED_SCALE, 0));
                    }
                    fabrik();
                }
            }

            // VFX
            if(!segments.isEmpty()) {
                WormSegment head = (WormSegment)segments.get(segmentCount - 1);
                List<Player> players = (List<Player>) this.level().players();

                // if burrowing
                if (this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox())) {
                    // screenshake (packets sent per player)
                    for (int i = 0; i < players.size(); i++) {
                        Player thisPlayer = players.get(i);
                        float dist = thisPlayer.distanceTo(head);
                        if (dist <= 100) {
                            // quadratic
                            //double intensity = Math.pow(dist-48, 2)/Math.pow(40,2);
                            // linear
                            //float intensity = -dist/(48/1.2f) + 1.2f;
                            float intensity = (float) Math.pow((1f + Math.pow(1.1f, dist - 17.5f)), -1) + 0.2f;
                            // burrowing cues

                            LodestonePacketRegistry.LODESTONE_CHANNEL.send((PacketDistributor.PLAYER.with(() -> (ServerPlayer) thisPlayer)),
                                    new PositionedScreenshakePacket(20, head.position(), 200, 100).setEasing(Easing.CUBIC_OUT).setIntensity(0.65f * intensity, 0));
                        }
                    }

                    // burrowing sounds (serverside)
                    if(aggroTargetEntity != null) {
                        float dist = aggroTargetEntity.distanceTo(head);
                        float intensity = (float) Math.pow((1f + Math.pow(1.1f, dist - 17.5f)), -1) + 0.2f;
                        if (soundFrequencyCount >= 10 - (intensity * 10)) {
                            level().playSound(null, head, SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 80f * intensity, intensity);
                            soundFrequencyCount = 0;
                        } else soundFrequencyCount++;
                    }
                }

                // breaching vfx
                if(!breaching && predictBreach(this.level(), head)) {
                    // System.out.println("breached");
                    breaching = true;
                    Vec3 particlePos = head.position().add(head.getDirectionVector().scale(8));
                    AAALevel.addParticle(this.level(), true, SAND_IMPACT.clone().scale(2.0f).position(particlePos));
                    AAALevel.addParticle(this.level(), true, SAND_SMOKE.clone().position(particlePos.add(0,2,0)));
                }
                else if(breaching && !predictBreach(this.level(), head)) {
                    breaching = false;
                    Vec3 particlePos = head.position().add(head.getDirectionVector().scale(8));
                    AAALevel.addParticle(this.level(), true, SAND_IMPACT.clone().scale(2.0f).position(particlePos));
                    AAALevel.addParticle(this.level(), true, SAND_SMOKE.clone().position(particlePos.add(0,2,0)));
                }
            }
        }
        // Clientside
        else {
        }
    }

    private void retarget(float yRange) {
        Vec3 headPos = segments.get(segmentCount - 1).position();
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(headPos.x+150, headPos.y+150, headPos.z+150, headPos.x-150, headPos.y-150, headPos.z-150));
        LivingEntity closestEntity = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (LivingEntity entity : nearbyEntities) {
            double deltaX = entity.getX() - headPos.x;
            double deltaZ = entity.getZ() - headPos.z;
            double distanceSq = deltaX * deltaX + deltaZ * deltaZ;
            double deltaY = entity.getY() - headPos.y;

            if (distanceSq >= 400 && deltaY <= yRange) { // 20 blocks away (20 * 20 = 400)
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntity = entity;
                }
            }
        }
        aggroTargetEntity = closestEntity;
    }

    private boolean predictBreach(Level level, WormSegment head) {
        Vec3 futurePos = head.position().add(head.getDirectionVector().scale(10));
        BlockPos futureBPos = new BlockPos((int)futurePos.x,(int)futurePos.y,(int)futurePos.z);
        return !level.getBlockState(futureBPos).isSuffocating(level, futureBPos);
    }

    private void addWormSegment(float length, Vec3 dirVec, Vec3 scale) {
        if(!this.level().isClientSide()) {
            if (segments.isEmpty()) {
                WormSegment chainSegment = new WormSegment(ModEntities.WORM_SEGMENT.get(), level());
                chainSegment.setLength(length);
                chainSegment.setDirectionVector(dirVec);
                chainSegment.setVisualScale(scale);
                chainSegment.setOwnerEntityUUID(this.getUUID());
                Vec3 pos = position();
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
                //System.out.println("spawning head segment");
            } else {
                WormSegment chainSegment = new WormSegment(ModEntities.WORM_SEGMENT.get(), level());
                chainSegment.setLength(length);
                chainSegment.setDirectionVector(dirVec);
                chainSegment.setVisualScale(scale);
                chainSegment.setOwnerEntityUUID(this.getUUID());
                Vec3 pos = segments.get(segments.size() - 1).position();
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
                //System.out.println("spawning next segment");
            }
        }
        segmentCount++;
    }

    private void addHeadSegment(float length, Vec3 dirVec, Vec3 scale) {
        if(!this.level().isClientSide()) {
            if (segments.isEmpty()) {
                System.err.println("worm head spawned not as last segment");
                return;
            } else {
                WormSegment chainSegment = new WormHeadSegment(ModEntities.WORM_HEAD_SEGMENT.get(), level());
                chainSegment.setLength(length);
                chainSegment.setDirectionVector(dirVec);
                chainSegment.setVisualScale(scale);
                chainSegment.setOwnerEntityUUID(this.getUUID());
                Vec3 pos = segments.get(segments.size() - 1).position().add(dirVec.normalize().scale(length));
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
                System.out.println("spawning head segment");
            }
        }
        segmentCount++;
    }

    @Override
    public void fabrik() {
        int i = 0;
        while(Math.abs(target.subtract(segments.get(segmentCount-1).position()).length()) > tolerance && i <= 10) {
            fabrikForward();
            i++;
        }
    }

    @Override
    public void applyAcceleration(Vec3 accel) {
        super.applyAcceleration(accel);
        if(targetV.length() > SPEED_SCALE) targetV = targetV.normalize().scale(SPEED_SCALE);
    }

    public void setAggroTargetEntity(LivingEntity target) {
        this.aggroTargetEntity = target;
    }

    @Override
    public void remove(RemovalReason pReason) {
        if(!level().isClientSide()) {
            for(int i=0; i<segments.size(); i++) if(segments.get(i) != null) segments.get(i).discard();
        }
        super.remove(pReason);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.removed = pCompound.getBoolean("removed");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("removed", removed);
    }
}
