package net.jelly.sandworm_mod.mixin;

import net.jelly.sandworm_mod.config.ServerConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegment;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkMixin {

    /**
     * @author JellyCarbonara
     * @reason Causes firework explosions to damage sandworm
     */
    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void disableCollisionForSpider(CallbackInfo ci) {
        explodeSandworm();
    }

    private void explodeSandworm() {
        FireworkRocketEntity self = (FireworkRocketEntity)(Object)this;
        for(WormHeadSegment wormHead : self.level().getEntitiesOfClass(WormHeadSegment.class, self.getBoundingBox().inflate(5.0D))) {
            if (!(self.distanceToSqr(wormHead) > 25.0D)) {
                wormHead.hitSandwormHead(self.getOwner(), ServerConfigs.FIREWORK_DAMAGE.get());
            }
        }
    }

}