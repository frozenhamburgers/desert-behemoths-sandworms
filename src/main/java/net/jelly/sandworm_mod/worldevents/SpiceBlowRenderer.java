package net.jelly.sandworm_mod.worldevents;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.sandworm_mod.registry.client.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
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

// Particle spawning for SIGNAL/ERUPT lives here (not in SpiceBlowWorldEvent#tick) because
// render() runs at the point in the frame Lodestone expects world-particle spawns from -
// spawning from tick() re-enters RenderHandler's particle batching mid-iteration and crashes.
public class SpiceBlowRenderer extends WorldEventRenderer<SpiceBlowWorldEvent> {

    private static final int SIGNAL_BURST_INTERVAL = 15;

    @Override
    public boolean canRender(SpiceBlowWorldEvent instance) {
        return true;
    }

    @Override
    public void render(SpiceBlowWorldEvent instance, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        switch (instance.phase) {
            case SIGNAL -> {
                if (instance.phaseTimer % SIGNAL_BURST_INTERVAL == 0 && instance.phaseTimer != instance.lastSignalBurstAt) {
                    instance.lastSignalBurstAt = instance.phaseTimer;
                    spawnSignalParticles(instance);
                }
            }
            case ERUPT -> {
                if (!instance.spawnedEruptBurst) {
                    instance.spawnedEruptBurst = true;
                    spawnEruptParticles(instance);
                }
            }
            default -> {
            }
        }
    }

    private void spawnSignalParticles(SpiceBlowWorldEvent instance) {
        Consumer<LodestoneWorldParticle> drift = p -> p.setParticleSpeed(p.getParticleSpeed().x, 0.02, p.getParticleSpeed().z);
        for (int i = 0; i < 4; i++) {
            WorldParticleBuilder.create(ParticleRegistry.SMOKE_CLOUD)
                    .enableNoClip()
                    .addRenderActor(drift)
                    .enableForcedSpawn()
                    .setRenderType(LodestoneWorldParticleRenderType.TRANSPARENT.withDepthFade())
                    .setSpritePicker(SimpleParticleOptions.ParticleSpritePicker.RANDOM_SPRITE)
                    .setColorData(ColorParticleData.create(new Color(214, 178, 122), new Color(191, 145, 92)).build())
                    .setTransparencyData(GenericParticleData.create(0.2f, 0.2f, 0).setEasing(Easing.SINE_IN_OUT).build())
                    .setLifetime(60)
                    .setSpinData(SpinParticleData.createRandomDirection(RandomSource.create(), 0.01f).build())
                    .setRandomMotion(0.05f, 0.02f, 0.05f)
                    .setRandomOffset(instance.radius() * 0.5f, 0.5f, instance.radius() * 0.5f)
                    .setScaleData(GenericParticleData.create(2f, 4f).build())
                    .setDiscardFunction(SimpleParticleOptions.ParticleDiscardFunctionType.ENDING_CURVE_INVISIBLE)
                    .spawn(Minecraft.getInstance().level, instance.position.x, instance.position.y, instance.position.z);
        }
    }

    private void spawnEruptParticles(SpiceBlowWorldEvent instance) {
        Consumer<LodestoneWorldParticle> burst = p -> p.setParticleSpeed(p.getParticleSpeed().x, 0.15, p.getParticleSpeed().z);
        for (int i = 0; i < 48; i++) {
            WorldParticleBuilder.create(ParticleRegistry.SMOKE_CLOUD)
                    .enableNoClip()
                    .addRenderActor(burst)
                    .enableForcedSpawn()
                    .setRenderType(LodestoneWorldParticleRenderType.TRANSPARENT.withDepthFade())
                    .setSpritePicker(SimpleParticleOptions.ParticleSpritePicker.RANDOM_SPRITE)
                    .setColorData(ColorParticleData.create(new Color(224, 158, 96), new Color(178, 108, 74)).build())
                    .setTransparencyData(GenericParticleData.create(0.5f, 0.5f, 0).setEasing(Easing.EXPO_IN_OUT).build())
                    .setLifetime(40)
                    .setSpinData(SpinParticleData.createRandomDirection(RandomSource.create(), 0.02f).build())
                    .setRandomMotion(0.2f, 0.15f, 0.2f)
                    .setRandomOffset(instance.radius(), 1f, instance.radius())
                    .setScaleData(GenericParticleData.create(6f, 10f).build())
                    .setDiscardFunction(SimpleParticleOptions.ParticleDiscardFunctionType.ENDING_CURVE_INVISIBLE)
                    .spawn(Minecraft.getInstance().level, instance.position.x, instance.position.y, instance.position.z);
        }
    }
}
