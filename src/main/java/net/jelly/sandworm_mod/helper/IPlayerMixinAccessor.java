package net.jelly.sandworm_mod.helper;

import net.minecraft.world.phys.Vec3;

public interface IPlayerMixinAccessor {

    boolean getGrappling();
    void setGrappling(boolean b);
    void tickGrapplingTimer();
    int getGrapplingTimer();
}
