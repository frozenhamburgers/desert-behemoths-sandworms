package net.jelly.sandworm_mod.event;

import dev.architectury.registry.ReloadListenerRegistry;
import mod.chloeprime.aaaparticles.client.loader.EffekAssetLoader;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.registry.client.ParticleRegistry;
import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.jelly.sandworm_mod.vfx.SinkholePostProcessor;
import net.jelly.sandworm_mod.vfx.SonicBoomPostProcessor;
import net.jelly.sandworm_mod.worldevents.WormBreachRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

import static team.lodestar.lodestone.registry.client.LodestoneWorldEventRendererRegistry.registerRenderer;


public class ClientEvents {
    // FORGE CLIENT EVENTS
    @Mod.EventBusSubscriber(modid= SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeClientEvents {
    }





    // MOD CLIENT EVENTS
    @Mod.EventBusSubscriber(modid= SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModClientEvents {
        // events that implement IModBusEvent are mod bus events

        // shaders
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // register shaders
            PostProcessHandler.addInstance(SinkholePostProcessor.INSTANCE);
            PostProcessHandler.addInstance(SonicBoomPostProcessor.INSTANCE);
            ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new EffekAssetLoader(), new ResourceLocation(SandwormMod.MODID, "effeks"));
        }

        @SubscribeEvent
        public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
            ParticleRegistry.registerParticleFactory(event);
        }

        @SubscribeEvent
        public static void registerRenderers(FMLClientSetupEvent event) {
            registerRenderer(WorldEventRegistry.WORM_BREACH, new WormBreachRenderer());
        }

    }


}
