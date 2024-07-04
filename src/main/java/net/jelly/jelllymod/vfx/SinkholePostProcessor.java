package net.jelly.jelllymod.vfx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.jelllymod.JellyMod;
import net.jelly.jelllymod.duck.IRenderTargetMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;

public class SinkholePostProcessor extends MultiInstancePostProcessor<SinkholeFx> {
    public static final SinkholePostProcessor INSTANCE = new SinkholePostProcessor();
    private EffectInstance effectSinkhole;
    protected RenderTarget cutoutRenderTarget;
    protected RenderTarget colorRenderTarget;
    public static RenderTarget particleRenderTarget;

    @Override
    public ResourceLocation getPostChainLocation() {
        return new ResourceLocation(JellyMod.MODID, "sinkhole");
    }
    // Max amount of FxInstances that can be added to the post processor at once
    @Override
    protected int getMaxInstances() {
        return 16;
    }

    // We passed in a total of 6 floats/uniforms to the shader inside our LightingFx class so this should return 6, will crash if it doesn't match
    @Override
    protected int getDataSizePerInstance() {
        return 7;
    }

    @Override
    public void init() {
        super.init();
        if (postChain != null) {
            effectSinkhole = effects[0];
            cutoutRenderTarget = this.postChain.getTempTarget("depthCutout");
            colorRenderTarget = this.postChain.getTempTarget("colorCutout");
        }
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);
        if(colorRenderTarget == null) System.out.println("colorrendertarget null");
        else postChain.passes.forEach(pass -> pass.getEffect().setSampler("CutoutDiffuseSampler", () -> colorRenderTarget.getColorTextureId()));
        setDataBufferUniform(effectSinkhole, "DataBuffer", "InstanceCount");
    }

    @Override
    public void afterProcess() {
    }

    public final void copyCutoutDepth() {
        if (this.isActive()) {
            if (this.postChain == null || this.cutoutRenderTarget == null) return;

            this.cutoutRenderTarget.copyDepthFrom(MC.getMainRenderTarget());
            GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, MC.getMainRenderTarget().frameBufferId);
        }
    }

    public final void copyColor() {
        if (this.isActive()) {
            if (this.postChain == null || this.colorRenderTarget == null) return;
            this.colorRenderTarget.copyDepthFrom(MC.getMainRenderTarget());
            ((IRenderTargetMixin) (Object) colorRenderTarget).aspect$copyColorFrom(MC.getMainRenderTarget());
            GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, MC.getMainRenderTarget().frameBufferId);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (this.postChain != null) {
            this.postChain.resize(width, height);
            if (this.cutoutRenderTarget != null) this.cutoutRenderTarget.resize(width, height, Minecraft.ON_OSX);
            if (this.colorRenderTarget != null) this.colorRenderTarget.resize(width, height, Minecraft.ON_OSX);
        }
    }
}
