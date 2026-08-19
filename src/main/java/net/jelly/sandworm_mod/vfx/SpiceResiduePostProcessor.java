package net.jelly.sandworm_mod.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;

public class SpiceResiduePostProcessor extends MultiInstancePostProcessor<SpiceResidueFx> {
    public static final SpiceResiduePostProcessor INSTANCE = new SpiceResiduePostProcessor();
    private EffectInstance effectSpiceResidue;

    @Override
    public ResourceLocation getPostChainLocation() {
        return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "spice_residue_post");
    }

    // Max amount of FxInstances that can be added to the post processor at once
    @Override
    protected int getMaxInstances() {
        return 8;
    }

    // center (3) + radius (1)
    @Override
    protected int getDataSizePerInstance() {
        return 4;
    }

    @Override
    public void init() {
        super.init();
        if (postChain != null) {
            effectSpiceResidue = effects[0];
        }
    }

    @Override
    public void beforeProcess(PoseStack viewModelStack) {
        super.beforeProcess(viewModelStack);
        setDataBufferUniform(effectSpiceResidue, "DataBuffer", "InstanceCount");
    }

    @Override
    public void afterProcess() {

    }
}
