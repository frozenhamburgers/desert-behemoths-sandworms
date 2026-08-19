package net.jelly.sandworm_mod.vfx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;

public class SpiceResiduePostProcessor extends MultiInstancePostProcessor<SpiceResidueFx> {
    public static final SpiceResiduePostProcessor INSTANCE = new SpiceResiduePostProcessor();
    private EffectInstance effectSpiceResidue;
    private RenderTarget blockDepthTarget;

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
            blockDepthTarget = postChain.getTempTarget("blockDepth");
        }
    }

    // Snapshots the depth buffer right after solid/cutout terrain renders and before
    // entities/block entities/translucents are drawn, so the shader's world-position
    // reconstruction only ever sees blocks. Called from ClientEvents on
    // RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS. Lodestone's own "depthMain"
    // target (what SonicBoomPostProcessor still relies on) is only populated at
    // Stage.AFTER_LEVEL - after the whole scene including entities is already drawn -
    // so it can't be reused here; this mirrors PostProcessor#copyDepthBuffer's own
    // RenderTarget.copyDepthFrom call, just at an earlier point in the frame.
    public void copyBlockDepthBuffer() {
        if (isActive() && blockDepthTarget != null) {
            blockDepthTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
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
