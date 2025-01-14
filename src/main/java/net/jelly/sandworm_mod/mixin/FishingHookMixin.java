package net.jelly.sandworm_mod.mixin;

import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormHeadSegment;
import net.jelly.sandworm_mod.entity.IK.worm.WormSegment;
import net.jelly.sandworm_mod.helper.IPlayerMixinAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    /**
     * @author JellyCarbonara
     * @reason Allows player to launch off hooking to sandworm
     */
    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void retrieveHook(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook)(Object)this;
        if(self.level().isClientSide()) return;
        Player player = self.getPlayerOwner();
        Entity hooked = self.getHookedIn();
        if (hooked != null) {
            System.out.println(self.getHookedIn());
            ((IPlayerMixinAccessor)player).setGrappling(true);
            player.hurtMarked = true;
            player.setDeltaMovement(hooked.position().subtract(player.position()).normalize().scale(Math.min(hooked.distanceTo(player)/3,4)));
        }
    }

}