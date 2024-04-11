package net.jelly.jelllymod.entity.IK.worm;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.jelllymod.JellyMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry;

public class WormHeadSegmentRenderer extends GeoEntityRenderer<WormHeadSegment> {


    public WormHeadSegmentRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WormHeadSegmentModel());
    }

    protected static final ResourceLocation LIGHT_TRAIL = new ResourceLocation(JellyMod.MODID, "textures/vfx/light_trail.png");
    protected static final RenderType LIGHT_TYPE = LodestoneRenderTypeRegistry.ADDITIVE_TEXTURE.apply(LIGHT_TRAIL);

    @Override
    public boolean shouldRender(WormHeadSegment pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public void render(WormHeadSegment entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, 15728640);
        poseStack.popPose();
    }
}



