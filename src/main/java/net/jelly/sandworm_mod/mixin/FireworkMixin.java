package net.jelly.sandworm_mod.mixin;

import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegment;
import net.jelly.sandworm_mod.entity.IK.worm.WormSegment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
                wormHead.hitSandwormHead(self.getOwner(), CommonConfigs.FIREWORK_DAMAGE.get());
            }
        }
    }

}