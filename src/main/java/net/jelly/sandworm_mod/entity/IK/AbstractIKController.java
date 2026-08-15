package net.jelly.sandworm_mod.entity.IK;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

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

            // head segment
            if (i == segmentCount - 1) currentSegment.moveTo(target);
                // not head segment
            else {
                AbstractIKSegment nextSegment = segments.get(i + 1);
                Vec3 nextTail = nextSegment.position().subtract(nextSegment.getDirectionVector().scale(nextSegment.getLength()));
                currentSegment.smoothMoveTo(nextTail);
            }
            currentSegment.setDirectionVector(currentSegment.position().subtract(lastPosition));
        }
    }

    /**
     * Same forward-reaching pass as {@link #fabrikForward()}, but any non-head index present in
     * {@code controlPoints} gets to perturb its spot in the chain via the supplied function.
     * Each segment's "natural" position is computed exactly as {@link #fabrikForward()} always
     * has - rigidly extended off its already-solved head-ward neighbor - and a control point
     * function receives that live, this-tick natural position and returns the position to
     * actually use (e.g. nudged down by gravity), rather than being handed a stale position from
     * a separate pass.
 *
     * Because the pass still only ever walks head-to-root, and each control point only sees the
     * natural position already implied by the segment ahead of it, nothing a control point does
     * can ripple forward into the arc(s) ahead of it, let alone drag on the head itself.
     */
    public void fabrikForwardSegmented(Map<Integer, UnaryOperator<Vec3>> controlPoints) {
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

            Vec3 naturalPosition;
            if (i == segmentCount - 1) {
                naturalPosition = target;
            } else {
                AbstractIKSegment nextSegment = segments.get(i + 1);
                naturalPosition = nextSegment.position().subtract(nextSegment.getDirectionVector().scale(nextSegment.getLength()));
            }

            UnaryOperator<Vec3> controlPoint = controlPoints.get(i);
            Vec3 finalPosition = controlPoint != null ? controlPoint.apply(naturalPosition) : naturalPosition;

            currentSegment.smoothMoveTo(finalPosition);
            currentSegment.setDirectionVector(finalPosition.subtract(lastPosition));
        }
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
