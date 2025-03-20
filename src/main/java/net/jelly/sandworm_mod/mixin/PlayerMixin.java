package net.jelly.sandworm_mod.mixin;

import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegment;
import net.jelly.sandworm_mod.helper.IPlayerMixinAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PlayerMixin implements IPlayerMixinAccessor {

    /**
     * @author JellyCarbonara
     * @reason Causes firework explosions to damage sandworm
     */

    private boolean grappling;
    private int grapplingTimer = 0;

    public void setGrappling(boolean b) {
        grapplingTimer = 60;
        grappling = b;
    }

    public boolean getGrappling() {
        return grappling;
    }

    public void tickGrapplingTimer() {
        if(grapplingTimer > 0) grapplingTimer--;
    }

    public int getGrapplingTimer() {
        return grapplingTimer;
    }

}