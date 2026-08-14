package net.jelly.sandworm_mod.entity.IK;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public abstract class AbstractIKSegment extends Entity {
    // note position() is used as the end position (head) of the chain segment
    private static final EntityDataAccessor<Vector3f> DIR_VEC = SynchedEntityData.defineId(AbstractIKSegment.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(AbstractIKSegment.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> VISUAL_SCALE = SynchedEntityData.defineId(AbstractIKSegment.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> UP_VEC = SynchedEntityData.defineId(AbstractIKSegment.class, EntityDataSerializers.VECTOR3);
    private AbstractIKController owner = null;
    private int discardTimer = 0;

    // vanilla Entity (unlike LivingEntity) does not smooth incoming network position/rotation
    // updates over multiple ticks -> snaps & jerky movement
    // these fields replicate LivingEntity's client-side lerp so segments (esp. the head) glide
    // between synced positions instead of stair-stepping once per tracker update.
    // maybe should've used LivingEntity from the start :/
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYRot, lerpXRot;

    // DIR_VEC/UP_VEC: Track the previous and current resolved values here so the renderer can blend between them using partialTick.
    private boolean firstDirUpTick = true;
    private Vec3 renderOldDirVec = new Vec3(0, 1, 0);
    private Vec3 renderNewDirVec = new Vec3(0, 1, 0);
    private Vec3 renderOldUpVec = new Vec3(0, 1, 0);
    private Vec3 renderNewUpVec = new Vec3(0, 1, 0);

    public AbstractIKSegment(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
        this.lerpX = pX;
        this.lerpY = pY;
        this.lerpZ = pZ;
        this.lerpYRot = pYRot;
        this.lerpXRot = pXRot;
        this.lerpSteps = pLerpSteps;
    }

    private void clientInterpolationTick() {
        if (firstDirUpTick) {
            renderOldDirVec = renderNewDirVec = getDirectionVector();
            renderOldUpVec = renderNewUpVec = getUpVector();
            firstDirUpTick = false;
        } else {
            renderOldDirVec = renderNewDirVec;
            renderOldUpVec = renderNewUpVec;
            renderNewDirVec = getDirectionVector();
            renderNewUpVec = getUpVector();
        }

        if (lerpSteps > 0) {
            double x = getX() + (lerpX - getX()) / lerpSteps;
            double y = getY() + (lerpY - getY()) / lerpSteps;
            double z = getZ() + (lerpZ - getZ()) / lerpSteps;
            float yRot = (float) (getYRot() + Mth.wrapDegrees(lerpYRot - getYRot()) / lerpSteps);
            float xRot = (float) (getXRot() + Mth.wrapDegrees(lerpXRot - getXRot()) / lerpSteps);
            lerpSteps--;
            setOldPosAndRot();
            setPos(x, y, z);
            setRot(yRot, xRot);
        }
    }

    public Vec3 getRenderDirectionVector(float partialTick) {
        return renderOldDirVec.lerp(renderNewDirVec, partialTick).normalize();
    }

    public Vec3 getRenderUpVector(float partialTick) {
        return renderOldUpVec.lerp(renderNewUpVec, partialTick).normalize();
    }


    // synch data
    @Override
    protected void defineSynchedData() {
        this.entityData.define(LENGTH, 1.0f);
        this.entityData.define(VISUAL_SCALE, new Vec3(1,1,1).toVector3f());
        this.entityData.define(DIR_VEC, new Vec3(0,1,0).toVector3f());
        this.entityData.define(UP_VEC, new Vec3(0,1,0).toVector3f());
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            clientInterpolationTick();
        }
        super.tick();
        if(!this.level().isClientSide()) {
            discardTimer++;
            if (discardTimer > 120) this.discard();
        }
    }

    public float getLength() { return entityData.get(LENGTH);}
    public void setLength(float length) { entityData.set(LENGTH, length);}
    public Vec3 getVisualScale() { return new Vec3(entityData.get(VISUAL_SCALE));}
    public void setVisualScale(Vec3 scaleVec) {
        entityData.set(VISUAL_SCALE, scaleVec.toVector3f());
    }
    public Vec3 getDirectionVector() { return new Vec3(entityData.get(DIR_VEC));}
    public void setDirectionVector(Vec3 dirVec) { entityData.set(DIR_VEC, dirVec.normalize().toVector3f());}
    public Vec3 getUpVector() { return new Vec3(entityData.get(UP_VEC));}
    public void setUpVector(Vec3 upVec) { entityData.set(UP_VEC, upVec.normalize().toVector3f());}

    // save as persistent data
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.entityData.set(LENGTH, pCompound.getFloat("segment_length"));
        this.entityData.set(DIR_VEC, new Vector3f(pCompound.getFloat("dir_vec_x"),
                pCompound.getFloat("dir_vec_y"),
                pCompound.getFloat("dir_vec_z")));
        this.entityData.set(VISUAL_SCALE, new Vector3f(pCompound.getFloat("scale_vec_x"),
                pCompound.getFloat("scale_vec_y"),
                pCompound.getFloat("scale_vec_z")));

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putFloat("segment_length", this.entityData.get(LENGTH));
        Vector3f dir_vec = this.entityData.get(DIR_VEC);
        pCompound.putFloat("dir_vec_x", dir_vec.get(0));
        pCompound.putFloat("dir_vec_y", dir_vec.get(1));
        pCompound.putFloat("dir_vec_z", dir_vec.get(2));
        Vector3f scale_vec = this.entityData.get(VISUAL_SCALE);
        pCompound.putFloat("scale_vec_x", scale_vec.get(0));
        pCompound.putFloat("scale_vec_y", scale_vec.get(1));
        pCompound.putFloat("scale_vec_z", scale_vec.get(2));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    // lets the segment know it is discoverable by its controller
    public void pingByOwner(AbstractIKController controller) {
        owner = controller;
        discardTimer = 0;
    }

    @Override
    protected float getEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        return pDimensions.height * 0.5F;
    }

    public Vec3 centeredPosition() {
        return position().add(0, this.getEyeHeight(), 0);
    }

    public void smoothMoveTo(Vec3 pos) {
        this.moveTo(pos);
//        else {
//            System.out.println("moving on server: TRUE");
//            this.moveTo(pos);
//            var server = this.getServer();
//            server.getPlayerList().getPlayers().forEach(player -> {
//                System.out.println("sending packet");
//                PacketHandler.sendToPlayer(new SegmentMovePacket(this.getId(), pos.x, pos.y, pos.z), player);
//            });
//        }
    }

}
