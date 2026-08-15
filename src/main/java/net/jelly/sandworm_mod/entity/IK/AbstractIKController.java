package net.jelly.sandworm_mod.entity.IK;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AbstractIKController extends Entity {
    public float tolerance = 0.01f;
    public Vec3 target = new Vec3(0, 0, 0);
    public Vec3 targetV = new Vec3(0, 0, 0);
    public int segmentCount = 0;
    public List<AbstractIKSegment> segments = new ArrayList<>();
    public List<UUID> segmentsUUIDs = new ArrayList<>();
    protected boolean noNullSegments = false;
    private int discardTimer = 0;

    public AbstractIKController(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void tick() {
        super.tick();
        loadSavedSegments();

        // ping all segments so they know we can still find them
        noNullSegments = false;
        for (int i = 0; i < this.segmentCount; i++) {
            if (segments.get(i) != null && !segments.get(i).isRemoved()) {
                segments.get(i).pingByOwner(this);
            }
            else {
                System.out.println("segment " + i + " is null");
                discardTimer++;
                if(!this.level().isClientSide() && discardTimer > 120) this.discard();
                return;
            }
        }

        noNullSegments = true;
        updateSegmentUpVectors();
    }

    private void loadSavedSegments() {
        if (this.segmentCount != 0 && segments.isEmpty()) {
            // restore segments list
            List<AbstractIKSegment> nearbyChainSegs = this.level().getEntitiesOfClass(
                    AbstractIKSegment.class,
                    new AABB(this.position().add(200, 200, 200), this.position().add(-200, -200, -200))
            );
            for (int i = 0; i < this.segmentCount; i++) {
                UUID thisUUID = segmentsUUIDs.get(i);
                this.segments.add(
                        nearbyChainSegs.stream()
                                .filter(obj -> obj.getStringUUID().equals(thisUUID.toString()))
                                .findFirst()
                                .orElse(null)
                );
            }
        }
    }

    private void updateSegmentUpVectors() {
        // update upVector for every tick
        if (!segments.isEmpty()) {
            moveTo(segments.get(0).position());
            Vec3 rootToEnd = (segments.get(segmentCount - 1).position().subtract(segments.get(0).position())).cross(new Vec3(0, 0, 1));
            for (int i = 0; i < this.segmentCount; i++) {
                if (segments.get(i) != null) {
                    segments.get(i).setUpVector(rootToEnd);
                }
            }
        }
    }

    public void fabrik() {
        // if not possible to reach target
        float totalLength = 0;
        float distToTarget = (float) (target.subtract(position()).length());
        for (int i = 0; i < segmentCount; i++) totalLength += segments.get(i).getLength();
        if (distToTarget > totalLength) {
            Vec3 rootToTarget = target.subtract(position()).normalize();
            for (int i = 0; i < segmentCount; i++) {
                AbstractIKSegment currentSegment = segments.get(i);
                Vec3 lastPosition;
                Vec3 root = this.position();
                float length = currentSegment.getLength();
                if (i == 0) lastPosition = root;
                else {
                    lastPosition = segments.get(i - 1).position();
                }

                currentSegment.smoothMoveTo(lastPosition.add(rootToTarget.scale(length)));
                currentSegment.setDirectionVector(rootToTarget);
            }
            return;
        }

        // if possible to reach target
        // while head segment is not within tolerance range of target
        int i = 0;
        while (Math.abs(target.subtract(segments.get(segmentCount - 1).position()).length()) > tolerance && i <= 10) {
            fabrikForward();
            fabrikBackward();
            i++;
        }
    }


    /**
     * Solves the chain head-to-root: the head is set directly to the live AI {@code target}
     * (unconditional - nothing root-ward of it can ever move it), and every other segment aims
     * for its "natural" position - rigidly extended {@code length} behind its already-solved
     * head-ward neighbor - after being run through {@link #adjustSegmentPosition}, whose default
     * implementation is a no-op (so plain rigid-rope behavior is what you get unless a subclass
     * opts in to something else).
     * <p>
     * Whatever {@link #adjustSegmentPosition} returns is re-projected onto the sphere of radius
     * {@code nextSegment.getLength()} around the head-ward neighbor before being used, so bond
     * length is always preserved exactly - an override can only change which direction a bond
     * points, never its length. Skipping this and using an adjusted position directly would leave
     * that bond stretched or compressed relative to the rest of the chain, reading as a kink.
     */
    public void fabrikForward() {
        for (int i = segmentCount - 1; i >= 0; i--) {
            AbstractIKSegment currentSegment = segments.get(i);
            Vec3 lastPosition;
            Vec3 root = this.position();
            if (i == 0) {
                lastPosition = root;
            } else {
                AbstractIKSegment lastSegment = segments.get(i - 1);
                lastPosition = lastSegment.position();
            }

            Vec3 finalPosition;
            if (i == segmentCount - 1) {
                finalPosition = target;
            } else {
                AbstractIKSegment nextSegment = segments.get(i + 1);
                Vec3 anchor = nextSegment.position();
                float boneLength = nextSegment.getLength();
                Vec3 naturalPosition = anchor.subtract(nextSegment.getDirectionVector().scale(boneLength));
                Vec3 desiredPosition = adjustSegmentPosition(i, naturalPosition);
                Vec3 aimOffset = desiredPosition.subtract(anchor);
                finalPosition = aimOffset.lengthSqr() > 1.0E-6
                        ? anchor.add(aimOffset.normalize().scale(boneLength))
                        : naturalPosition;
            }

            currentSegment.smoothMoveTo(finalPosition);
            currentSegment.setDirectionVector(finalPosition.subtract(lastPosition));
        }
    }

    /**
     * Lets a subclass perturb a non-head segment's position during {@link #fabrikForward()} (e.g.
     * gravity/ground-conforming physics), given the live, this-tick natural position the rigid
     * chain would otherwise use. The default is a pure passthrough - plain rigid-rope behavior.
     * The caller re-projects whatever this returns onto the correct bond-length sphere, so an
     * override is free to return any position; only its direction from the anchor matters.
     */
    protected Vec3 adjustSegmentPosition(int i, Vec3 naturalPosition) {
        return naturalPosition;
    }

    public void fabrikBackward() {
        for (int i = 0; i < segmentCount; i++) {
            AbstractIKSegment currentSegment = segments.get(i);
            Vec3 lastPosition;
            Vec3 root = this.position();
            if (i == 0) {
                lastPosition = root;
            } else {
                AbstractIKSegment lastSegment = segments.get(i - 1);
                lastPosition = lastSegment.position();
            }

            currentSegment.setDirectionVector(currentSegment.position().subtract(lastPosition));
            currentSegment.smoothMoveTo(lastPosition.add(currentSegment.getDirectionVector().scale(currentSegment.getLength())));
        }
    }

    protected void addSegment(AbstractIKSegment chainSegment) {
        if (!this.level().isClientSide()) {
            if (segments.isEmpty()) {
                chainSegment.pingByOwner(this);
                Vec3 pos = position();
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
            } else {
                chainSegment.pingByOwner(this);
                Vec3 pos = segments.get(segments.size() - 1).position();
                chainSegment.moveTo(pos);
                segments.add(chainSegment);
                segmentsUUIDs.add(chainSegment.getUUID());
                level().addFreshEntity(chainSegment);
            }
        }
        segmentCount++;
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
        for (int i = 0; i < this.segmentCount; i++) {
            UUID thisUUID = pCompound.getUUID("segment_" + i);
            this.segmentsUUIDs.add(thisUUID);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("segment_count", segmentCount);
        if (this.segmentCount != 0 && segmentsUUIDs.isEmpty()) {
            readAdditionalSaveData(pCompound);
        }
        for (int i = 0; i < this.segmentCount; i++) {
            pCompound.putUUID("segment_" + i, segmentsUUIDs.get(i));
        }
    }

    @Override
    public void remove(RemovalReason pReason) {
        if (!level().isClientSide()) {
            for (int i = 0; i < segments.size(); i++) if (segments.get(i) != null) segments.get(i).discard();
        }
        super.remove(pReason);
    }

}
