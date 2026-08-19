package net.jelly.sandworm_mod.event;

import dev.architectury.registry.ReloadListenerRegistry;
import mod.chloeprime.aaaparticles.client.loader.EffekAssetLoader;
import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.entity.IK.worm.WormSegment;
import net.jelly.sandworm_mod.registry.client.ParticleRegistry;
import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.jelly.sandworm_mod.vfx.SonicBoomPostProcessor;
import net.jelly.sandworm_mod.vfx.SpiceEruptionPostProcessor;
import net.jelly.sandworm_mod.vfx.SpiceResiduePostProcessor;
import net.jelly.sandworm_mod.worldevents.SpiceBlowRenderer;
import net.jelly.sandworm_mod.worldevents.WormBreachRenderer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;

import static team.lodestar.lodestone.registry.client.LodestoneWorldEventRendererRegistry.registerRenderer;

public class ClientEvents {
    // FORGE CLIENT EVENTS

    @Mod.EventBusSubscriber(modid = SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeClientEvents {
        @SubscribeEvent
        public static void onCameraSetup(ViewportEvent event) {
            Player player = Minecraft.getInstance().player;
            if (player.getVehicle() != null) {
                if (player.getVehicle() instanceof WormSegment) {
                    float scale = 1;
                    if (Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                            Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
                        event.getCamera().move(-event.getCamera().getMaxZoom(scale * 7.5F), 0F, 0);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onEntityMount(EntityMountEvent event) {
            System.out.println("mount");
            if (event.getEntityBeingMounted() instanceof WormSegment && event.getEntityMounting() == Minecraft.getInstance().player) {
                if(event.isMounting()) Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                else Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
            }
        }

        // Snapshot depth right after terrain (before entities/block entities/translucents) so
        // SpiceResiduePostProcessor's world-position reconstruction only ever sees blocks - see
        // SpiceResiduePostProcessor#copyBlockDepthBuffer for why Lodestone's own depthMain target
        // (populated at AFTER_LEVEL, once the whole scene including entities is drawn) can't be used.
        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
                SpiceResiduePostProcessor.INSTANCE.copyBlockDepthBuffer();
            }
        }

    }

    // MOD CLIENT EVENTS
    @Mod.EventBusSubscriber(modid= SandwormMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModClientEvents {
        // events that implement IModBusEvent are mod bus events

        // shaders
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // register shaders
            PostProcessHandler.addInstance(SonicBoomPostProcessor.INSTANCE);
            PostProcessHandler.addInstance(SpiceResiduePostProcessor.INSTANCE);
            PostProcessHandler.addInstance(SpiceEruptionPostProcessor.INSTANCE);
            ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new EffekAssetLoader(), ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "effeks"));
        }

        @SubscribeEvent
        public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
            ParticleRegistry.registerParticleFactory(event);
        }

        @SubscribeEvent
        public static void registerRenderers(FMLClientSetupEvent event) {
            registerRenderer(WorldEventRegistry.WORM_BREACH, new WormBreachRenderer());
            registerRenderer(WorldEventRegistry.SPICE_BLOW, new SpiceBlowRenderer());
        }

    }
}
