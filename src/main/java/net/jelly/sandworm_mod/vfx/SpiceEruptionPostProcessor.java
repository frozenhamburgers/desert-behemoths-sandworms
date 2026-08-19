package net.jelly.sandworm_mod.vfx;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import team.lodestar.lodestone.systems.postprocess.MultiInstancePostProcessor;

public class SpiceEruptionPostProcessor extends MultiInstancePostProcessor<SpiceEruptionFx> {
    public static final SpiceEruptionPostProcessor INSTANCE = new SpiceEruptionPostProcessor();

    // PostChain#updateOrthoMatrix computes ONE shared screen sized ortho matrix and applies it to
    // every pass, regardless of that pass's own output target size. That's fine for every other
    // pass here (all screen-sized), but the raymarch pass's own output (eruptionLowRes) is a
    // fixed 480x270 target. without this fix its fullscreen quad only fills the bottom-left
    // fraction of that targetg.
    private EffectInstance effectEruption;

    @Override
    public ResourceLocation getPostChainLocation() {
        return ResourceLocation.fromNamespaceAndPath(SandwormMod.MODID, "spice_eruption_post");
    }

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
            applyLowResFixups();
        }
    }

    @Override
    public void resize(int width, int height) {
        // ortho matrix back to the shared screen-sized one - reapply the fix after that happens.
        super.resize(width, height);
        applyLowResFixups();
    }

    private void applyLowResFixups() {
        if (postChain == null) return;
        RenderTarget lowRes = postChain.getTempTarget("eruptionLowRes");
        if (lowRes == null) return;

        // bilinear upscale
        lowRes.setFilterMode(GL11.GL_LINEAR);

        if (postChain.passes.isEmpty()) return;
        // raymarch pass is declared first in spice_eruption_post.json.
        PostPass raymarchPass = postChain.passes.get(0);
        raymarchPass.setOrthoMatrix(new Matrix4f().setOrtho(0.0f, lowRes.width, 0.0f, lowRes.height, 0.1f, 1000.0f));
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
