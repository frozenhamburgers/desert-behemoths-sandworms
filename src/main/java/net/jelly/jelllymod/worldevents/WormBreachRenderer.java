package net.jelly.jelllymod.worldevents;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.jelllymod.registry.client.ParticleRegistry;
import net.jelly.jelllymod.vfx.SinkholePostProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import team.lodestar.lodestone.handlers.RenderHandler;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.SimpleParticleOptions;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;
import team.lodestar.lodestone.systems.particle.world.LodestoneWorldParticle;
import team.lodestar.lodestone.systems.worldevent.WorldEventRenderer;

import java.awt.*;
import java.util.function.Consumer;

public class WormBreachRenderer extends WorldEventRenderer<WormBreachWorldEvent> {

    @Override
    public void render(WormBreachWorldEvent instance, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {

        if(!instance.spawnedParticles) {
            Consumer<LodestoneWorldParticle> rise = p -> p.setParticleSpeed(p.getParticleSpeed().x, Math.pow(20,(instance.lifetime+2)/-15),p.getParticleSpeed().z);
            // DARK
            for(int i=0; i<36; i++)
            WorldParticleBuilder.create(ParticleRegistry.CRINGE_PARTICLE)
                    .enableNoClip()
                    .addRenderActor(rise)
                    .enableForcedSpawn()
                    .setRenderType(LodestoneWorldParticleRenderType.TRANSPARENT.withDepthFade())
                    .setSpritePicker(SimpleParticleOptions.ParticleSpritePicker.RANDOM_SPRITE)
                    .setColorData(ColorParticleData.create(new Color(171, 128, 97), new Color(171, 128, 97)).build())
                    .setTransparencyData(GenericParticleData.create(0.25f, 0.25f, 0).setEasing(Easing.EXPO_IN_OUT).build())
                    .setLifetime(480)
                    .setSpinData(SpinParticleData.createRandomDirection(RandomSource.create(), 0.005f).build())
                    .setRandomMotion(0.075f, 0.03f, 0.075f)
                    .setRandomOffset(12,2,12)
                    .setScaleData(GenericParticleData.create(10f,10f).build())
                    .setDiscardFunction(SimpleParticleOptions.ParticleDiscardFunctionType.ENDING_CURVE_INVISIBLE)
                    .spawn(Minecraft.getInstance().level, instance.position.x, instance.position.y, instance.position.z);
            for(int i=0; i<27; i++)
            WorldParticleBuilder.create(ParticleRegistry.CRINGE_PARTICLE)
                    .enableNoClip()
                    .addRenderActor(rise)
                    .enableForcedSpawn()
                    .setRenderType(LodestoneWorldParticleRenderType.TRANSPARENT.withDepthFade())
                    .setSpritePicker(SimpleParticleOptions.ParticleSpritePicker.RANDOM_SPRITE)
                    .setColorData(ColorParticleData.create(new Color(157, 136, 108), new Color(157, 136, 108)).build())
                    .setTransparencyData(GenericParticleData.create(0.25f, 0.25f, 0).setEasing(Easing.EXPO_IN_OUT).build())
                    .setLifetime(450)
                    .setSpinData(SpinParticleData.createRandomDirection(RandomSource.create(), 0.005f).build())
                    .setRandomMotion(0.085f, 0.03f, 0.085f)
                    .setRandomOffset(20,2,20)
                    .setScaleData(GenericParticleData.create(8f,8f).build())
                    .setDiscardFunction(SimpleParticleOptions.ParticleDiscardFunctionType.ENDING_CURVE_INVISIBLE);
                    //.spawn(Minecraft.getInstance().level, instance.position.x, instance.position.y, instance.position.z);
            for(int i=0; i<36; i++)
            WorldParticleBuilder.create(ParticleRegistry.CRINGE_PARTICLE)
                    .enableNoClip()
                    .addRenderActor(rise)
                    .enableForcedSpawn()
                    .setRenderType(LodestoneWorldParticleRenderType.TRANSPARENT.withDepthFade())
                    .setSpritePicker(SimpleParticleOptions.ParticleSpritePicker.RANDOM_SPRITE)
                    .setColorData(ColorParticleData.create(new Color(215, 194, 173), new Color(188, 155, 130)).build())
                    .setTransparencyData(GenericParticleData.create(0.05f, 0.25f, 0).setEasing(Easing.EXPO_IN_OUT).build())
                    .setLifetime(450)
                    .setSpinData(SpinParticleData.createRandomDirection(RandomSource.create(), 0.005f).build())
                    .setRandomMotion(0.085f, 0.04f, 0.085f)
                    .setRandomOffset(12,2,12)
                    .setScaleData(GenericParticleData.create(3f,10f).build())
                    .setDiscardFunction(SimpleParticleOptions.ParticleDiscardFunctionType.ENDING_CURVE_INVISIBLE);
                    //.spawn(Minecraft.getInstance().level, instance.position.x, instance.position.y, instance.position.z);
            instance.spawnedParticles = true;
        }
    }

    @Override
    public boolean canRender(WormBreachWorldEvent instance) {
        return true;
    }
}
