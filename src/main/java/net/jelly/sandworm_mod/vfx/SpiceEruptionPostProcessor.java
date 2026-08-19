package net.jelly.sandworm_mod.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;

public class SpiceEruptionPostProcessor extends MultiInstancePostProcessor<SpiceEruptionFx> {
    public static final SpiceEruptionPostProcessor INSTANCE = new SpiceEruptionPostProcessor();
    private EffectInstance effectEruption;

    @Override
    public ResourceLocation getPostChainLocation() {
        return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "spice_eruption_post");
    }

    // Concurrent eruptions are rare and each one is raymarched per-pixel, so keep this low.
    @Override
    protected int getMaxInstances() {
        return 4;
    }

    // base.xyz, baseRadius, topRadius, height, seed, age, lifetime
    @Override
    protected int getDataSizePerInstance() {
        return 9;
    }

    @Override
    public void init() {
        super.init();
        if (postChain != null) {
            effectEruption = effects[0];
        }
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);
        setDataBufferUniform(effectEruption, "DataBuffer", "InstanceCount");
    }

    @Override
    public void afterProcess() {

    }
}
