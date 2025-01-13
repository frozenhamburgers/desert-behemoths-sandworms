package net.jelly.sandworm_mod.entity.IK;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class AbstractWormController extends AbstractIKController {

    public AbstractWormController(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected abstract void initWorm();

    public AbstractIKSegment head() {
        if(segments.isEmpty()) return null;
        return segments.get(0);
    }

    private void forwardIK() {
        fabrikForward();
    }

    public void tick() {
        super.tick();
        if(!this.level().isClientSide()) {
            // initialize segments if necessary
            if(segmentCount == 0) {
                initWorm();
            }

            if(!noNullSegments) return;
//            if(!segments.isEmpty()) {
//                forwardIK();
//            }
        }
    }

}
