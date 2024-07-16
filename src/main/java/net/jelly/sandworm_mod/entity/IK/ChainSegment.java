package net.jelly.sandworm_mod.entity.IK;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class ChainSegment extends Entity {
    // note position() is used as the end position (head) of the chain segment
    private static final EntityDataAccessor<Vector3f> DIR_VEC = SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> VISUAL_SCALE = SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> UP_VEC = SynchedEntityData.defineId(ChainSegment.class, EntityDataSerializers.VECTOR3);

    public ChainSegment(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    // synch data
    @Override
    protected void defineSynchedData() {
        this.entityData.define(LENGTH, 10.0f);
        this.entityData.define(VISUAL_SCALE, new Vec3(1,1,1).toVector3f());
        this.entityData.define(DIR_VEC, new Vec3(0,2,0).toVector3f());
        this.entityData.define(UP_VEC, new Vec3(0,1,0).toVector3f());
    }

    @Override
    public void tick() {
        super.tick();
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

//    @Override
//    public EntityDimensions getDimensions(Pose pPose) {
//        return super.getDimensions(pPose).scale((float)getVisualScale().x, (float)getVisualScale().y);
//    }

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
}
