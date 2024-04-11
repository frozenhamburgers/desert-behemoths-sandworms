package net.jelly.jelllymod.event;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.registry.ReloadListenerRegistry;
import mod.chloeprime.aaaparticles.client.loader.EffekAssetLoader;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.entity.IK.worm.WorldEventTypes;
import net.jelly.jelllymod.entity.IK.worm.WormBreachRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Vector3f;
import team.lodestar.lodestone.handlers.RenderHandler;
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry;
import team.lodestar.lodestone.registry.client.LodestoneWorldEventRendererRegistry;
import team.lodestar.lodestone.systems.rendering.VFXBuilders;


public class ClientEvents {
    // FORGE CLIENT EVENTS
    @Mod.EventBusSubscriber(modid= JellyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeClientEvents {
//        @SubscribeEvent
//        public static void onKeyInput(InputEvent.Key event) {
//            if(KeyBindings.DRINKING_KEY.consumeClick()) {
//                ModMessages.sendToServer(new ExampleC2SPacket());
//            }
//        }

        // RENDERING
        private static final ResourceLocation MAGIC_CIRCLE = new ResourceLocation(JellyMod.MODID,"textures/vfx/magic_circle.png");
        private static final RenderType MAGIC_CIRCLE_TYPE = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.apply(MAGIC_CIRCLE);
        @SubscribeEvent
        public static void onRenderEntity(RenderLivingEvent.Pre event) {
            if(!(event.getEntity() instanceof Player) && event.getEntity().hasEffect(MobEffects.CONDUIT_POWER)) {
                float height = 0.0f;
                float width = 1.5f;
                VertexConsumer textureConsumer = RenderHandler.DELAYED_RENDER.getBuffer(LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(MAGIC_CIRCLE));
                Vector3f[] positions = new Vector3f[]{new Vector3f(-width, height, width), new Vector3f(width, height, width), new Vector3f(width, height, -width), new Vector3f(-width, height, -width)};
                VFXBuilders.WorldVFXBuilder builder = VFXBuilders.createWorld().setPosColorTexLightmapDefaultFormat();

                event.getPoseStack().pushPose();
                event.getPoseStack().translate(0,0.0001f, 0);
                builder.renderQuad(textureConsumer, event.getPoseStack(), positions, 4.0f);
                builder.setPosColorLightmapDefaultFormat();

                event.getPoseStack().popPose();
            }
//            event.getEntity().getCapability(MagicCircleTrackedProvider.MAGIC_CIRCLE).ifPresent(magicCircleTracked -> {
//                if(magicCircleTracked.getMagicCircleType() == 1) {
//                    float height = 0.0f;
//                    float width = 1.5f;
//                    VertexConsumer textureConsumer = RenderHandler.DELAYED_RENDER.getBuffer(LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.applyAndCache(MAGIC_CIRCLE));
//                    Vector3f[] positions = new Vector3f[]{new Vector3f(-width, height, width), new Vector3f(width, height, width), new Vector3f(width, height, -width), new Vector3f(-width, height, -width)};
//                    VFXBuilders.WorldVFXBuilder builder = VFXBuilders.createWorld().setPosColorTexLightmapDefaultFormat();
//
//                    event.getPoseStack().pushPose();
//                    event.getPoseStack().translate(0,0.0001f, 0);
//                    builder.renderQuad(textureConsumer, event.getPoseStack(), positions, 4.0f);
//                    builder.setPosColorLightmapDefaultFormat();
//
//                    event.getPoseStack().popPose();
//                }
//            });
        }
    }





    // MOD CLIENT EVENTS
    @Mod.EventBusSubscriber(modid= JellyMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModClientEvents {
        // events that implement IModBusEvent are mod bus events

        // shaders
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // register shaders
            // PostProcessHandler.addInstance(TestShader.INSTANCE);
            ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, new EffekAssetLoader(), new ResourceLocation(JellyMod.MODID, "effeks"));
        }

        @SubscribeEvent
        public static void registerRenderers(FMLClientSetupEvent event) {
            // register world event renderers
            LodestoneWorldEventRendererRegistry.registerRenderer(WorldEventTypes.WORM_BREACH, new WormBreachRenderer());
        }

    }


}
