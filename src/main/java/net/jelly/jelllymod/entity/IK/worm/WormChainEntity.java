package net.jelly.jelllymod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.entity.IK.ChainSegment;
import net.jelly.jelllymod.entity.IK.KinematicChainEntity;
import net.jelly.jelllymod.entity.ModEntities;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;
import software.bernie.example.registry.SoundRegistry;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.network.screenshake.PositionedScreenshakePacket;
import team.lodestar.lodestone.registry.common.LodestonePacketRegistry;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WormChainEntity extends KinematicChainEntity {
    private static float SPEED_SCALE = 1.7f;
    private boolean breaching = false;
    private int soundFrequencyCount = 0;
    private static final ParticleEmitterInfo SAND_IMPACT = new ParticleEmitterInfo(new ResourceLocation(JellyMod.MODID, "sandimpact"));
    private static final ParticleEmitterInfo SAND_SMOKE = new ParticleEmitterInfo(new ResourceLocation(JellyMod.MODID, "sandsmoke"));
    public WormChainEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        if(!this.level().isClientSide()) {
            // save & load stuff
            if(this.segmentCount != 0 && segments.isEmpty()) {
                // restore segments list
                List<ChainSegment> nearbyChainSegs = this.level().getEntitiesOfClass(
                        ChainSegment.class,
                        new AABB(this.position().add(100, 100, 100), this.position().add(-100, -100, -100))
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
            if(segments.size() == 0) segmentCount = 0;
            else {
                for(int j=0; j<this.segments.size(); j++) {
                    if (segments.get(j) == null) {
                        System.out.println("regenerating");
                        for (int i = 0; i < segments.size(); i++) if (segments.get(i) != null) segments.get(i).discard();
                        segments = new ArrayList<>();
                        segmentCount = 0;
                        break;
                    }
                }
            }

            // init spawn segments
            if(segmentCount == 0) {
                // addTailSegment(0.35f*5, new Vec3(0,1,0));
                for(int i=0; i<10; i++) addWormSegment(0.35f*5, new Vec3(0,1,0), new Vec3(7.5*((i+2)/11f),7.5*((i+2)/11f),5));
                for(int i=0; i<80; i++) addWormSegment(0.35f*5, new Vec3(0,1,0), new Vec3(7.5,7.5,5));
                addHeadSegment(0.35f*5, new Vec3(0,1,0), new Vec3(7.5,7.5,5));
            }

            // keep position on root
            // update upVector for every
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
            //Entity nearestPlayer = this.level().getNearestPlayer(this, 1000);
            if(!segments.isEmpty()) {
                ChainSegment head = segments.get(segmentCount - 1);
                Entity nearestPlayer = this.level().getNearestEntity(LivingEntity.class, TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting(), null, head.position().x, head.position().y, head.position().z, new AABB(150, 150, 150, -150, -150, -150));
                System.out.println(nearestPlayer);

                if (nearestPlayer != null) {
                    if (!(stage == 0 && head.distanceTo(nearestPlayer) < 15 * SPEED_SCALE))
                        goal = nearestPlayer.position();
                    else if (goal == null) goal = nearestPlayer.position();

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
                        applyAcceleration(moveTowardVec.scale(0.3 * SPEED_SCALE));

                        // if worm is close enough & not "looking" at target, it has charged past it
                        if (moveTowardVec.dot(head.getDirectionVector()) < 0.25 || !(this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox())))
                            stage = 1;
                    }
                    // no control, charging
                    if (stage == 1) {
                        // if in the air
                        if (!(this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox()))) {
                            // gravity
                            applyAcceleration(new Vec3(0, -0.03, 0));
                            // small targeting acceleration
                            if (moveTowardVec.y < 0) moveTowardVec = new Vec3(moveTowardVec.x, 0, moveTowardVec.z);
                            System.out.println(moveTowardVec);
                            if (goal.y - head.position().y < 20) applyAcceleration(moveTowardVec.scale(0.01));
                        }
                        // otherwise, must be 16 blocks away & at least 10 blocks deeper than target to retarget
                        else if (head.position().distanceTo(goal) > 4 * SPEED_SCALE && (goal.y - head.position().y >= 20 * SPEED_SCALE))
                            stage = 0;
                        else applyAcceleration(new Vec3(0, -0.04 * SPEED_SCALE, 0));
                    }
                    fabrik();
                }
            }

            // VFX
            if(!segments.isEmpty()) {
                WormSegment head = (WormSegment)segments.get(segmentCount - 1);
                List<Player> players = (List<Player>) this.level().players();
                for(int i=0; i<players.size(); i++) {
                    Player thisPlayer = players.get(i);
                    float dist = thisPlayer.distanceTo(head);
                    if(dist <= 100) {
                        // quadratic
                        //double intensity = Math.pow(dist-48, 2)/Math.pow(40,2);
                        // linear
                        //float intensity = -dist/(48/1.2f) + 1.2f;
                        float intensity = (float)Math.pow((1f+Math.pow(1.1f,dist-17.5f)), -1) + 0.2f;
                        // burrowing cues
                        if(this.level().collidesWithSuffocatingBlock(null,head.getBoundingBox())) {
                            LodestonePacketRegistry.LODESTONE_CHANNEL.send((PacketDistributor.PLAYER.with(() -> (ServerPlayer)thisPlayer)),
                                    new PositionedScreenshakePacket(20, head.position(), 200, 100).setEasing(Easing.CUBIC_OUT).setIntensity(0.65f * intensity, 0));
                            if(soundFrequencyCount >= 10-(intensity*10)) {
                                level().playSound(null, head, SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 20f*intensity, intensity);
                                soundFrequencyCount = 0;
                            }
                            else soundFrequencyCount++;
                        }
                        // breaching vfx
                        if(!breaching && predictBreach(this.level(), head)) {
                            // System.out.println("breached");
                            breaching = true;
                            Vec3 particlePos = head.position().add(head.getDirectionVector().scale(8));
                            AAALevel.addParticle(this.level(), true, SAND_IMPACT.clone().scale(2.0f).position(particlePos));
                            AAALevel.addParticle(this.level(), true, SAND_SMOKE.clone().scale(1.5f,1, 1.5f).position(particlePos.add(0,2,0)));
                        }
                        else if(breaching && !predictBreach(this.level(), head)) {
                            breaching = false;
                            Vec3 particlePos = head.position().add(head.getDirectionVector().scale(8));
                            AAALevel.addParticle(this.level(), true, SAND_IMPACT.clone().scale(2.0f).position(particlePos));
                            AAALevel.addParticle(this.level(), true, SAND_SMOKE.clone().scale(1.5f,1, 1.5f).position(particlePos.add(0,2,0)));
                        }
                    }
                }

            }
        }
        // Clientside
        else {
        }
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
                Vec3 pos = position().add(dirVec.normalize().scale(length));
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
                System.out.println("spawning head segment");
            } else {
                WormSegment chainSegment = new WormSegment(ModEntities.WORM_SEGMENT.get(), level());
                chainSegment.setLength(length);
                chainSegment.setDirectionVector(dirVec);
                chainSegment.setVisualScale(scale);
                Vec3 pos = segments.get(segments.size() - 1).position().add(dirVec.normalize().scale(length));
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
                System.out.println("spawning next segment");
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

    @Override
    public void remove(RemovalReason pReason) {
        if(!level().isClientSide()) {
            System.out.println(pReason);
            for(int i=0; i<segments.size(); i++) if(segments.get(i) != null) segments.get(i).discard();
        }
        super.remove(pReason);
    }
}
