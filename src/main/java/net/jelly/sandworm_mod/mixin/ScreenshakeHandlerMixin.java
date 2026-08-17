package net.jelly.sandworm_mod.mixin;

import net.jelly.sandworm_mod.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

/**
 * @author JellyCarbonara
 * @reason Scales all incoming screenshake (including from other mods using Lodestone)
 * by this client's configured screenshake strength preference.
 */
@Mixin(value = ScreenshakeHandler.class, remap = false)
public class ScreenshakeHandlerMixin {

    @Inject(method = "addScreenshake", at = @At("HEAD"))
    private static void sandworm_mod$scaleIntensity(ScreenshakeInstance instance, CallbackInfo ci) {
        float strength = ClientConfig.SCREENSHAKE_STRENGTH.get().floatValue();
        instance.intensity1 *= strength;
        instance.intensity2 *= strength;
        instance.intensity3 *= strength;
    }
}
