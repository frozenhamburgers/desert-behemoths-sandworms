package net.jelly.jelllymod.entity.IK;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.jelly.jelllymod.JellyMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import team.lodestar.lodestone.handlers.RenderHandler;
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry;
import team.lodestar.lodestone.systems.rendering.VFXBuilders;

import java.awt.*;

public class ChainSegmentRenderer extends EntityRenderer<ChainSegment> {
    public ChainSegmentRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(ChainSegment pEntity) {
        return null;
    }

    protected static final ResourceLocation LIGHT_TRAIL = new ResourceLocation(JellyMod.MODID, "textures/vfx/light_trail.png");
    protected static final RenderType LIGHT_TYPE = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.apply(LIGHT_TRAIL);

    @Override
    public void render(ChainSegment pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);


        VFXBuilders.WorldVFXBuilder builder = VFXBuilders.createWorld().setPosColorTexLightmapDefaultFormat();
        builder.setColor(new Color(255, 0, 0));

        VertexConsumer textureConsumer = RenderHandler.DELAYED_RENDER.getBuffer(LIGHT_TYPE);
        pPoseStack.pushPose();
        Vec3 position = pEntity.getPosition(pPartialTick);
        pPoseStack.translate(-position.x, -position.y, -position.z);

        Vec3 startPosition = position.add(pEntity.getDirectionVector().normalize().scale(-pEntity.getLength()));

        builder.renderBeam(textureConsumer, pPoseStack.last().pose(), startPosition, position, 0.1f);

        pPoseStack.popPose();
    }
}
