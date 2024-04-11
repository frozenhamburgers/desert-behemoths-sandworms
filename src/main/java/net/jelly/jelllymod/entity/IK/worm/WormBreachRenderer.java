package net.jelly.jelllymod.entity.IK.worm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;
import team.lodestar.lodestone.systems.easing.Easing;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.worldevent.WorldEventRenderer;

import java.awt.*;

public class WormBreachRenderer extends WorldEventRenderer<WormBreachWorldEvent> {
    @Override
    public void render(WormBreachWorldEvent instance, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {

    }
}
