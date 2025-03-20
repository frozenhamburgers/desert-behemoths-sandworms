package net.jelly.sandworm_mod.entity.IK.worm;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.block.ModBlockEntities;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.AbstractWormController;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.item.ModItems;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.jelly.sandworm_mod.worldevents.SonicBoomWorldEvent;
import net.jelly.sandworm_mod.worldevents.WormBreachWorldEvent;
import net.jelly.sandworm_mod.worldevents.WormRippleWorldEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.joml.Vector3f;
import team.lodestar.lodestone.handlers.WorldEventHandler;
import team.lodestar.lodestone.network.screenshake.PositionedScreenshakePacket;
import team.lodestar.lodestone.registry.common.LodestonePacketRegistry;
import team.lodestar.lodestone.systems.easing.Easing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.jelly.sandworm_mod.helper.BiomeHelper.isDesertBiome;

public class WormChainEntity extends AbstractWormController {
    private static float SPEED_SCALE = 1.3f;
    private boolean breaching = false;
    private int soundFrequencyCount = 0;
    private static final ParticleEmitterInfo SLOWER_SAND_IMPACT = new ParticleEmitterInfo(new ResourceLocation(SandwormMod.MODID, "slowersandimpact"));
    public LivingEntity aggroTargetEntity;
    public boolean removed = false;
    private int discardTimer = 0;
    private int noTargetEscapeTimer = 0;
    private boolean escaping = false;
    private int noPlayerDiscardTimer = 0;
    private boolean isChasing = false;
    public int dmgTaken = 0;
    private WormHeadSegment head;
    public Vec3 thumperTarget = null;
    private int despawnTimer = 0;
    Vec3 goal;
    int stage;
    public boolean vulnerable = true;
    public int mountableTimer = 0;
    public boolean mounted = false;
    private int dismountTimer = 0;
    Player rider;

    public WormChainEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void initWorm() {
        System.out.println("init Worm");
        if(segmentCount == 0) {
            System.out.println("adding seg");
            for(int i=0; i<10; i++) addWormSegment(0.35f*5, new Vec3(1,0,0), new Vec3(7.5*((i+2)/11f),7.5*((i+2)/11f),5));
            for(int i=0; i<80; i++) addWormSegment(0.35f*5, new Vec3(1,0,0), new Vec3(7.5,7.5,5));
            addHeadSegment(0.35f*5, new Vec3(1,0,0), new Vec3(7.5,7.5,5));
            if(aggroTargetEntity == null && thumperTarget == null) {
                retarget(50);
            }
            Vec3 targetedObjectPos = getTargetedObjectPos();
            if(targetedObjectPos != null) {
//                System.out.println("initial head direction vector:" + aggroTargetEntity.position().subtract(this.position()).normalize());
                Vec3 lookAtAggroEntity = targetedObjectPos.subtract(this.position()).normalize();
                for(int i=0; i<this.segmentCount; i++) {
                    segments.get(i).setDirectionVector(lookAtAggroEntity);
                }
            }
        }
    }

    // returns 0 if normal, 1 if freeze ticking
    private int despawnBehavior() {
        for (int j = 0; j < this.segments.size(); j++) {
            if (segments.get(j) == null) {
                if(discardTimer < 100) discardTimer++;
                else this.discard();
                return 1;
            }
        }
        discardTimer = 0;
        // despawn if no players within 400 blocks for 3 seconds
        if(this.level().getNearestPlayer(this, 400) == null) {
            if(this.noPlayerDiscardTimer < 60) noPlayerDiscardTimer++;
            else this.discard();
            return 1;
        }
        else noPlayerDiscardTimer = 0;
        return 0;
    }

    private void fikBehavior() {
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
    }

    private void dolphinBehavior() {
        Vec3 targetedObjectPos = getTargetedObjectPos();
        if(!isChasing) {
            Vec3 towardTarget = (targetedObjectPos.subtract(head.position())).normalize().multiply(20,0,20);
            goal = head.position().add(towardTarget.x, 0, towardTarget.z);
            goal = new Vec3(goal.x, targetedObjectPos.y, goal.z);
//            System.out.println("chasing:" + goal);
            isChasing = true;
        }
        else if(head.position().subtract(goal).horizontalDistance() <= 10) {
            Vec3 towardTarget = (targetedObjectPos.subtract(head.position())).normalize().multiply(20,0,20);
            goal = head.position().add(towardTarget.x, 0, towardTarget.z);
            goal = new Vec3(goal.x, targetedObjectPos.y, goal.z);
//            System.out.println("re chasing:" + goal);
        }
    }

    private void VFXSFXBehavior() {
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
            Vec3 targetedObjectPos = getTargetedObjectPos();
            if(targetedObjectPos != null) {
                double dist = targetedObjectPos.distanceTo(head.position());
                float intensity = (float) Math.pow((1f + Math.pow(1.1f, dist - 17.5f)), -1) + 0.2f;
                if (soundFrequencyCount >= 10 - (intensity * 10)) {
                    level().playSound(null, head, SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 80f * intensity, intensity);
                    head.playSound(ModSounds.WORM_BURROW.get(), 80f * intensity, intensity);
                    soundFrequencyCount = 0;
                } else soundFrequencyCount++;
            }
        }
        // breaching vfx
        if(!breaching && predictBreach(this.level(), head)) {
            // System.out.println("breached");
            breaching = true;
            Vec3 particlePos = head.position().add(head.getDirectionVector().scale(8));
            AAALevel.addParticle(this.level(), true, SLOWER_SAND_IMPACT.clone().scale(1.25f).position(particlePos));
            smokeParticles(this.level(), particlePos.add(0,-9,0));
            if (!escaping) this.playSound(ModSounds.WORM_BREACH.get(), 10f, 1f);
            //AAALevel.addParticle(this.level(), true, SAND_SMOKE.clone().position(particlePos.add(0,2,0)));
        }
        else if(breaching && !predictBreach(this.level(), head)) {
            breaching = false;
            Vec3 particlePos = head.position().add(head.getDirectionVector().scale(8));
            AAALevel.addParticle(this.level(), true, SLOWER_SAND_IMPACT.clone().scale(1.25f).position(particlePos));
            smokeParticles(this.level(), particlePos.add(0,-9,0));
            if (!escaping) this.playSound(ModSounds.WORM_LAND.get(), 10f, 1f);
            //AAALevel.addParticle(this.level(), true, SAND_SMOKE.clone().position(particlePos.add(0,2,0)));
        }
    }

    // returns
    private void wormAIBehavior() {
        System.out.println(this.mountableTimer);
        // retarget if target entity is gone
        if(thumperTarget == null && (aggroTargetEntity == null || aggroTargetEntity.isRemoved() || aggroTargetEntity.isDeadOrDying() || !isDesertBiome(aggroTargetEntity))) {
            retarget(50);
            if(noTargetEscapeTimer >= 60) escaping = true;
        }
        else if (!escaping) {
            // if too far, chase by dolphining
            Vec3 targetedObjectPos = getTargetedObjectPos();
            if(targetedObjectPos == null) {
                target = head.position().add(targetV);
                System.out.println("no target! targetV:" + targetV);
                return;
            }
            if (head.position().subtract(targetedObjectPos).horizontalDistance() > 50) {
                dolphinBehavior();
            }
            else isChasing = false;

            // if not chasing, assign goal accordingly
            if(!isChasing) {
                // assign goal to aggro entity until close enough
                if (!(stage == 0 && targetedObjectPos.distanceTo(head.position()) < 22 * SPEED_SCALE))
                    goal = targetedObjectPos;
                else if (goal == null) goal = targetedObjectPos;
                else if (stage == 0) sinkHole(this.level(), goal);
            }

            // apply target velocity to target
            target = head.position().add(targetV);
            Vec3 moveTowardVec = (goal.subtract(head.position())).normalize();

            // stage 0: controlled movement
            if (stage == 0) {
                applyAcceleration(moveTowardVec.scale(0.1 * SPEED_SCALE));

                // make vulnerable to attacks again
                vulnerable = true;

                // if worm is close enough & not "looking" at target, it has charged past it
                if (moveTowardVec.dot(head.getDirectionVector()) < 0.25 || !(this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox())))
                    stage = 1;
            }
            // stage 1: no control, charging, post-breach and briefly after post-entry
            if (stage == 1) {
                // if in the air
                if (!(this.level().collidesWithSuffocatingBlock(null, head.getBoundingBox()))) {
                    if(targetedObjectPos.y - head.position().y > 30) retarget(50);
                    // gravity
                    applyAcceleration(new Vec3(0, -0.0375, 0));
                    // small
                    // targeting acceleration
                    if (moveTowardVec.y < 0) moveTowardVec = new Vec3(moveTowardVec.x, 0, moveTowardVec.z);
                    if (goal.y - head.position().y < 20) applyAcceleration(moveTowardVec.scale(0.01));
                }
                // otherwise, must be 16 blocks away & at least 10 blocks deeper than target to lock back onto target entity
                else if (head.position().distanceTo(goal) > 4 * SPEED_SCALE && (goal.y - head.position().y >= 20 * SPEED_SCALE))
                    stage = 0;
                else applyAcceleration(new Vec3(0, -0.02 * SPEED_SCALE, 0));
            }
        }

        // escape behavior
        if(escaping) {
            applyAcceleration(new Vec3(0, -0.0375, 0));
            target = head.position().add(targetV);
        }

    }


    private @Nullable BlockPos findTopY(int x, int z, int topY, int bottomY) {
        // Start from the top Y value and move downward
        for (int y = topY; y >= bottomY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = this.level().getBlockState(pos);

            // Check if this block is solid and has air above it
            if (!state.isAir() && this.level().getBlockState(pos.above()).isAir()) {
                return new BlockPos(x,y,z);
            }
        }

        // If no suitable block is found, return the bottom Y (default to ground level)
        return new BlockPos(x,bottomY,z);
    }

    private void mountedWormAIBehavior() {
        // if dismount
        if(segments.get(segmentCount - 3).getPassengers().isEmpty()) {
            this.mounted = false;
            this.escaping = true;
            return;
        }

        // SURFACE NORMAL CALCULATION
        AABB wormAABB = head.getBoundingBox();
        int minBX = (int)Math.floor(wormAABB.minX);
        int maxBX = (int)Math.floor(wormAABB.maxX);
        int minBZ = (int)Math.floor(wormAABB.minZ);
        int maxBZ = (int)Math.floor(wormAABB.maxZ);
        int minBY = (int)Math.floor(wormAABB.minY);
        int maxBY = (int)Math.floor(wormAABB.maxY);

        List<BlockPos> sampledPositions = new ArrayList<>();
        sampledPositions.add(findTopY(minBX, minBZ, maxBY, minBY-1)); // Bottom-left
        sampledPositions.add(findTopY(maxBX, minBZ, maxBY, minBY-1)); // Bottom-right
        sampledPositions.add(findTopY(minBX, maxBZ, maxBY, minBY-1)); // Top-left
        sampledPositions.add(findTopY(maxBX, maxBZ, maxBY, minBY-1)); // Top-right
        sampledPositions.add(findTopY((minBX + maxBX) / 2, (minBZ + maxBZ) / 2, maxBY, minBY-1)); // Center

        Vec3 normal = new Vec3(0,0,0);
        boolean onSurface = false;
        for(int i=0; i<4; i++) {
            if(this.level().getBlockState(sampledPositions.get(i)).isSuffocating(this.level(), sampledPositions.get(i))) {
                onSurface = true;
                break;
            }
        }
        if(onSurface) {
            Vec3 p1 = sampledPositions.get(0).getCenter();
            Vec3 p2 = sampledPositions.get(1).getCenter();
            Vec3 p3 = sampledPositions.get(2).getCenter();
            Vec3 p4 = sampledPositions.get(3).getCenter();

            Vec3 v1 = p4.subtract(p1);
            Vec3 v2 = p3.subtract(p2);

            normal = v2.cross(v1).normalize();
        }
//        System.out.println("normal: " + normal);

        // gravity
        Vec3 gForce = new Vec3(0,-0.016,0);
        // steering
        Vec3 riderForce = new Vec3(0,0,0);
        if(normal.length() > 0) {
            riderForce = rider.getLookAngle().normalize().scale(0.04);
            if(rider.zza > 0) riderForce = riderForce.add(rider.getLookAngle().normalize().scale(0.04));
        }
        // net force is all forces before normal & friciton are applied
        Vec3 netForce = new Vec3(0,0,0).add(riderForce).add(gForce);
        // normal
        Vec3 normalForce = normal.scale(-netForce.dot(normal));
        // friction
        double frictionCoefficient = 0.8; // Example coefficient of friction
        Vec3 frictionForce = targetV.scale(normalForce.length()).scale(-frictionCoefficient);
        targetV = targetV.add(netForce).add(normalForce).add(frictionForce);
        if (targetV.dot(normal) < 0) {
            targetV = targetV.subtract(normal.scale(targetV.dot(normal))); // Remove downward normal component of velocity
        }
        target = head.position().add(targetV);

        // dismount if leave biome for 10+ seconds
        if(this.dismountTimer > 200 || this.escaping) this.segments.get(segmentCount - 3).getPassengers().get(0).dismountTo(head.position().x, head.position().y+5, head.position().z);
        if(!isDesertBiome(head)) this.dismountTimer++;
        else this.dismountTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide()) {
            if(mountableTimer > 0) mountableTimer--;
            // check if despawn is necessary every tick
            if (despawnBehavior() == 1) return;

            // forward inverse kinematics for worm
            fikBehavior();

            // flee if targeting on non survival player for more than x minutes
            if(aggroTargetEntity != null) {
                // System.out.println(despawnTimer);
                if(!(aggroTargetEntity instanceof Player)) despawnTimer++;
                else if (((Player) aggroTargetEntity).isCreative()) despawnTimer++;
                else despawnTimer = 0;
                if(despawnTimer >= CommonConfigs.DESPAWN_TIMER.get()*20) escaping = true;
            }

            if(!segments.isEmpty()) {
                // update/assign head if necessary
                if(head == null) {
                    if(segments.get(segmentCount - 1).getClass() == WormHeadSegment.class) head = (WormHeadSegment)segments.get(segmentCount - 1);
                }
                if(thumperTarget != null) {
                    BlockPos thumperBPos = BlockPos.containing(thumperTarget.x, thumperTarget.y, thumperTarget.z);
                    if (this.head.isColliding(thumperBPos, this.level().getBlockState(thumperBPos))) {
                        this.level().destroyBlock(thumperBPos, false);
                    }
                }
                if(!mounted) wormAIBehavior();
                else mountedWormAIBehavior();
                VFXSFXBehavior();
                fabrik();
            }
        }
    }

    // retarget to an entity in this priority:
    // 1. non-creative player at least 20 horizontal blocks away within 300 blocks
    // 2. farthest non-creative player within 20 horizontal block range
    // 3. mob (or creative player) at least 20 horizontal blocks away within 300 blocks
    // 4. farthest mob (or creative player) within a 20 block range
    private void retarget(float yRange) {
        Vec3 headPos = segments.get(segmentCount - 1).position();;
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(headPos.x+150, headPos.y+150, headPos.z+150, headPos.x-150, headPos.y-150, headPos.z-150));
        List<Player> nearbyPlayers = this.level().getNearbyPlayers(TargetingConditions.forNonCombat(), null,
                new AABB(headPos.x+150, headPos.y+150, headPos.z+150, headPos.x-150, headPos.y-150, headPos.z-150));
        LivingEntity closestEntity = null;
        double closestDistanceSq = Double.MAX_VALUE;
        LivingEntity farthestTooCloseEntity = null;
        double fartestTooCloseDistanceSq = 0;

        for (Player entity : nearbyPlayers) {
            // do not retarget onto mobs outside the desert or underground
            if(!isDesertBiome(entity) || this.level().getBrightness(LightLayer.SKY, entity.blockPosition()) <= 0 || entity.isSpectator() || entity.isCreative()) continue;
            double deltaX = entity.getX() - headPos.x;
            double deltaZ = entity.getZ() - headPos.z;
            double distanceSq = deltaX * deltaX + deltaZ * deltaZ;
            double deltaY = entity.getY() - headPos.y;

            if (distanceSq >= 400 && deltaY <= yRange) { // 20 blocks away (20^2 = 400)
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntity = entity;
                }
            }
            else if (deltaY <= yRange) {
                if (distanceSq > fartestTooCloseDistanceSq) {
                    fartestTooCloseDistanceSq = distanceSq;
                    farthestTooCloseEntity = entity;
                }
            }
        }

        if(closestEntity == null & farthestTooCloseEntity == null) {
            closestDistanceSq = Double.MAX_VALUE;
            fartestTooCloseDistanceSq = 0;
            for (LivingEntity entity : nearbyEntities) {
                // do not retarget onto mobs outside the desert or underground
                if (!isDesertBiome(entity) || this.level().getBrightness(LightLayer.SKY, entity.blockPosition()) <= 0)
                    continue;
                double deltaX = entity.getX() - headPos.x;
                double deltaZ = entity.getZ() - headPos.z;
                double distanceSq = deltaX * deltaX + deltaZ * deltaZ;
                double deltaY = entity.getY() - headPos.y;

                if (distanceSq >= 400 && deltaY <= yRange) { // 20 blocks away (20 * 20 = 400)
                    if (distanceSq < closestDistanceSq) {
                        closestDistanceSq = distanceSq;
                        closestEntity = entity;
                    }
                } else if (deltaY <= yRange) {
                    if (distanceSq > fartestTooCloseDistanceSq) {
                        fartestTooCloseDistanceSq = distanceSq;
                        farthestTooCloseEntity = entity;
                    }
                }
            }
        }
        if(closestEntity == null) closestEntity = farthestTooCloseEntity;
        aggroTargetEntity = closestEntity;
        if(closestEntity == null) noTargetEscapeTimer++;
        else noTargetEscapeTimer = 0;
    }

    private boolean predictBreach(Level level, WormSegment head) {
        Vec3 targetedObjectPos = getTargetedObjectPos();
        if(targetedObjectPos != null && targetedObjectPos.y - head.getY() > 20) return false;
        Vec3 futurePos = head.position().add(head.getDirectionVector().scale(10));
        BlockPos futureBPos = new BlockPos((int)futurePos.x,(int)futurePos.y,(int)futurePos.z);
        return (!level.getBlockState(futureBPos).isSuffocating(level, futureBPos) && !level.getBlockState(futureBPos).is(Blocks.CAVE_AIR));
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
//                System.out.println("spawning head segment");
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

    public void blastHit(int dmg) {
        if((!mounted && !vulnerable) || escaping) return;
        vulnerable = false;
        System.out.println(dmgTaken);
        int totalHealth = CommonConfigs.HEALTH.get();
        head.playSound(ModSounds.WORM_ROAR.get(), 10f, 1f);
        if((dmgTaken < totalHealth/3 && dmgTaken+dmg >= totalHealth/3) || (dmgTaken < 2*totalHealth/3 && dmgTaken+dmg >= 2*totalHealth/3)) {
            targetV = new Vec3(targetV.x*0.075, targetV.y+1.2, targetV.z*0.075);
            mountableTimer = 240;
            sonicBoom();
        }
        else targetV = new Vec3(targetV.x*0.2, targetV.y*0.2, targetV.z*0.2);

        dmgTaken+=dmg;
        // if mounted, any source of damage causes a tooth to drop and the worm to escape
        if(dmgTaken >= totalHealth || mounted) {
            sonicBoom();
            ItemEntity toothItem = new ItemEntity(this.level(), head.getX(), head.getY(), head.getZ(), new ItemStack(ModItems.WORM_TOOTH.get(), 1));
            this.level().addFreshEntity(toothItem);
            escaping = true;
        }
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

    private int smokeCount = 0;
    private void smokeParticles(Level level, Vec3 pos) {
        smokeCount++;
        WormBreachWorldEvent breachEvent = new WormBreachWorldEvent().setPosition(pos);
        breachEvent.start(level);
        breachEvent.setDirty();
        WorldEventHandler.addWorldEvent(level, breachEvent);
    }

    private void sinkHole(Level level, Vec3 pos) {
        WormRippleWorldEvent breachEvent = new WormRippleWorldEvent().spawnRipple(pos);
        breachEvent.start(level);
        breachEvent.setDirty();
        WorldEventHandler.addWorldEvent(level, breachEvent);
    }

    private void sonicBoom() {
        if(head == null) return;
        SonicBoomWorldEvent breachEvent = new SonicBoomWorldEvent().spawnRipple(this.head);
        breachEvent.start(this.level());
        breachEvent.setDirty();
        WorldEventHandler.addWorldEvent(this.level(), breachEvent);

        List<Player> players = (List<Player>) this.level().players();
        // screenshake (packets sent per player)
        players.forEach(player -> {
            float dist = player.distanceTo(head);
            if (dist <= 100) {
                LodestonePacketRegistry.LODESTONE_CHANNEL.send((PacketDistributor.PLAYER.with(() -> (ServerPlayer) player)),
                        new PositionedScreenshakePacket(80, head.position(), 200, 100).setEasing(Easing.CUBIC_OUT).setIntensity(0.85f));
            }
        });
    }

    private Vec3 getTargetedObjectPos() {
        if(thumperTarget != null) {
            BlockEntity thumper = this.level().getExistingBlockEntity(BlockPos.containing(thumperTarget.x, thumperTarget.y, thumperTarget.z));
            if(thumper == null || thumper.getType() != ModBlockEntities.THUMPER_ENTITY.get()) thumperTarget = null;
            return thumperTarget;
        }
        if(aggroTargetEntity != null) return aggroTargetEntity.position();
        return null;
    }

    public void mountPlayer(Player player) {
        player.startRiding(segments.get(segmentCount - 3));
        mounted = true;
        rider = player;
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
