package net.jelly.jelllymod.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.jelllymod.JellyMod;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;

public class SonicBoomPostProcessor extends MultiInstancePostProcessor<SonicBoomFx> {
    public static final SonicBoomPostProcessor INSTANCE = new SonicBoomPostProcessor();
    private EffectInstance effectGlow;

    @Override
    public ResourceLocation getPostChainLocation() {
        return new ResourceLocation(JellyMod.MODID, "sonic_boom_post");
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
            effectGlow = effects[0];
        }
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);
        setDataBufferUniform(effectGlow, "DataBuffer", "InstanceCount");
    }

    @Override
    public void afterProcess() {

    }
}
