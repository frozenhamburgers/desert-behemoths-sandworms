package net.jelly.sandworm_mod.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.jelly.sandworm_mod.duck.IRenderTargetMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30C.GL_RGBA16F;

// Author: Its-Cryptic
// https://github.com/Its-Cryptic/Aspect
@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements IRenderTargetMixin {

    /**
     * Allows copying the color buffer from another render target to this render target using a duck interface
     *
     * @param renderTarget the render target to copy the color buffer from
     */
    @Override
    public void aspect$copyColorFrom(RenderTarget renderTarget) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, renderTarget.frameBufferId);
        GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, this.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, this.width, this.height, GlConst.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
    }

    @Redirect(method = "createBuffers",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
                    ordinal = 2
            )
    )
    private void onCreateBuffers(int p_84210_, int p_84211_, int p_84212_, int p_84213_, int p_84214_, int p_84215_, int p_84216_, int p_84217_, IntBuffer p_84218_) {
        //GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GlConst.GL_RGBA8, this.width, this.height, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, (IntBuffer)null);
        GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GL_RGBA16F, this.width, this.height, 0, GlConst.GL_RGBA, GlConst.GL_FLOAT, (IntBuffer)null);
    }

    @Shadow
    public int width;
    @Shadow public int height;
    @Shadow public int frameBufferId;
}