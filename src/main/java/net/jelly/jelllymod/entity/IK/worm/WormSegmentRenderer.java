package net.jelly.jelllymod.entity.IK.worm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.jelly.jelllymod.JellyMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import team.lodestar.lodestone.handlers.RenderHandler;
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry;
import team.lodestar.lodestone.systems.rendering.VFXBuilders;

import java.awt.*;

public class WormSegmentRenderer extends GeoEntityRenderer<WormSegment> {


    public WormSegmentRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WormSegmentModel());
    }

    protected static final ResourceLocation LIGHT_TRAIL = new ResourceLocation(JellyMod.MODID, "textures/vfx/light_trail.png");
    protected static final RenderType LIGHT_TYPE = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.apply(LIGHT_TRAIL);


    @Override
    public void render(WormSegment entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        Vec3 dirVec = entity.getDirectionVector();
        Vec3 upVec = entity.getUpVector();
        Matrix4f matrix = new Matrix4f();
        matrix.rotateTowards(dirVec.toVector3f(), upVec.toVector3f());
        Vec3 scaleVec = entity.getVisualScale();

        // rendering executes at the position of the entity (feet) but we want it in the middle
        poseStack.translate(0, scaleVec.y/2f, 0);
        poseStack.mulPoseMatrix(matrix);
        poseStack.scale((float)scaleVec.x, (float)scaleVec.y, (float)scaleVec.z);
         //poseStack.scale(7.5f, 7.5f, 5);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 15728640);
        poseStack.popPose();

//        VFXBuilders.WorldVFXBuilder builder = VFXBuilders.createWorld().setPosColorTexLightmapDefaultFormat();
//        builder.setColor(new Color(255, 0, 0));
//
//        VertexConsumer textureConsumer = RenderHandler.DELAYED_RENDER.getBuffer(LIGHT_TYPE);
//        poseStack.pushPose();
//        Vec3 position = entity.getPosition(partialTick);
//        poseStack.translate(-position.x, -position.y, -position.z);
//
//        Vec3 startPosition = position.add(entity.getDirectionVector().normalize().scale(5));
//        Vec3 startPositionUp = position.add(upVec.normalize().scale(5));
//
//        builder.renderBeam(textureConsumer, poseStack.last().pose(), startPosition, position, 0.1f);
//        builder.renderBeam(textureConsumer, poseStack.last().pose(), startPositionUp, position, 0.1f);
//
//        poseStack.popPose();
    }

}