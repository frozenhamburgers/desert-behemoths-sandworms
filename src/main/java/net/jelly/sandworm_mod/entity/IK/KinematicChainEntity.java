package net.jelly.sandworm_mod.entity.IK;

import net.jelly.sandworm_mod.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KinematicChainEntity extends Entity {
    public float tolerance = 0.01f;
    public Vec3 target = new Vec3(0,0,0);
    protected Vec3 targetV = new Vec3(0,0,0);
    public Vec3 goal = null;
    public int segmentCount = 0;
    public List<ChainSegment> segments = new ArrayList<>();
    public List<UUID> segmentsUUIDs = new ArrayList<>();
    public int stage = 0;
    public KinematicChainEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick() {
        super.tick();
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
            // keep position on root
            // update upVector for every segment
            if (!segments.isEmpty()) {
                setPos(segments.get(0).position());
                Vec3 rootToEnd = (segments.get(segmentCount-1).position().subtract(segments.get(0).position())).cross(new Vec3(0,0,1));
                for(int i=0; i<this.segmentCount; i++) {
                    if(segments.get(i) != null) segments.get(i).setUpVector(rootToEnd);
                }
            }

            if(segmentCount == 0) {
                for(int i=0; i<100; i++) addSegment(0.35f*5, new Vec3(0,1,0));
//                addSegment(1, new Vec3(1,2,3));
//                addSegment(2, new Vec3(1,2,3));
//                addSegment(3, new Vec3(1,2,3));
            }
        }
    }


    public void fabrik() {
        // if not possible to reach target
        float totalLength = 0;
        float distToTarget = (float)(target.subtract(position()).length());
        for(int i=0; i<segmentCount; i++) totalLength += segments.get(i).getLength();
        if(distToTarget > totalLength) {
//            System.out.println("target too far:" + distToTarget + " > " + totalLength);
            Vec3 rootToTarget = target.subtract(position()).normalize();
            for(int i=0; i<segmentCount; i++) {
                ChainSegment currentSegment = segments.get(i);
                Vec3 lastPosition;
                Vec3 root = this.position();
                float length = currentSegment.getLength();
                if(i==0) lastPosition = root;
                else {
                    lastPosition = segments.get(i-1).position();
                }

                currentSegment.setPos(lastPosition.add(rootToTarget.scale(length)));
                currentSegment.setDirectionVector(rootToTarget);
            }
            return;
        }

        // if possible to reach target
        // while head segment is not within tolerance range of target
        int i = 0;
        while(Math.abs(target.subtract(segments.get(segmentCount-1).position()).length()) > tolerance && i <= 10) {
            fabrikForward();
            // fabrikBackward();
            i++;
        }
    }


    public void fabrikForward() {
        for(int i=segmentCount-1; i>=0; i--) {
            ChainSegment currentSegment = segments.get(i);
            Vec3 lastPosition;
            Vec3 root = this.position();
            if (i == 0) {
                lastPosition = root;
            }
            else {
                ChainSegment lastSegment = segments.get(i - 1);
                lastPosition = lastSegment.position();
            }

            // head segment
            if(i==segmentCount-1) currentSegment.setPos(target);
            // not head segment
            else {
                ChainSegment nextSegment = segments.get(i + 1);
                Vec3 nextTail = nextSegment.position().subtract(nextSegment.getDirectionVector().scale(nextSegment.getLength()));
                currentSegment.setPos(nextTail);
            }
            currentSegment.setDirectionVector(currentSegment.position().subtract(lastPosition));
        }
    }

    public void fabrikBackward() {
        for(int i=0; i<segmentCount; i++) {
            ChainSegment currentSegment = segments.get(i);
            Vec3 lastPosition;
            Vec3 root = this.position();
            if (i == 0) {
                lastPosition = root;
            }
            else {
                ChainSegment lastSegment = segments.get(i - 1);
                lastPosition = lastSegment.position();
            }

            currentSegment.setDirectionVector(currentSegment.position().subtract(lastPosition));
            currentSegment.setPos(lastPosition.add(currentSegment.getDirectionVector().scale(currentSegment.getLength())));
        }
    }

    private void addSegment(float length, Vec3 dirVec) {
//        if(!this.level().isClientSide()) {
//            if (segments.isEmpty()) {
//                ChainSegment chainSegment = new ChainSegment(ModEntities.CHAIN_SEGMENT.get(), level());
//                chainSegment.setLength(length);
//                chainSegment.setDirectionVector(dirVec);
//                Vec3 pos = position().add(dirVec.normalize().scale(length));
//                chainSegment.moveTo(pos);
//                segments.add(chainSegment);
//                segmentsUUIDs.add(chainSegment.getUUID());
//                level().addFreshEntity(chainSegment);
////                System.out.println("spawning first segment");
//            } else {
//                ChainSegment chainSegment = new ChainSegment(ModEntities.CHAIN_SEGMENT.get(), level());
//                chainSegment.setLength(length);
//                chainSegment.setDirectionVector(dirVec);
//                Vec3 pos = segments.get(segments.size() - 1).position().add(dirVec.normalize().scale(length));
//                chainSegment.moveTo(pos);
//                segments.add(chainSegment);
//                segmentsUUIDs.add(chainSegment.getUUID());
//                level().addFreshEntity(chainSegment);
////                System.out.println("spawning next segment");
//            }
//        }
//        segmentCount++;
    }

    public void applyAcceleration(Vec3 accel) {
        targetV = targetV.add(accel);
    }

    // DATA SAVING & SYNCHING

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.segmentCount = pCompound.getInt("segment_count");
        this.segmentsUUIDs = new ArrayList<>();
        // restore segments list too
        for(int i=0; i<this.segmentCount; i++) {
            UUID thisUUID = pCompound.getUUID("segment_" + i);
            this.segmentsUUIDs.add(thisUUID);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("segment_count", segmentCount);
        if(this.segmentCount != 0 && segmentsUUIDs.isEmpty()) {
            readAdditionalSaveData(pCompound);
        }
        for (int i = 0; i < this.segmentCount; i++) {
            pCompound.putUUID("segment_" + i, segmentsUUIDs.get(i));
        }
    }
}
