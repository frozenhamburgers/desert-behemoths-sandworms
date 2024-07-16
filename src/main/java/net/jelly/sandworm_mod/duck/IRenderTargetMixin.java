package net.jelly.sandworm_mod.duck;

import com.mojang.blaze3d.pipeline.RenderTarget;

// Author: Its-Cryptic
// https://github.com/Its-Cryptic/Aspect
public interface IRenderTargetMixin {
    /**
     * Allows copying the color buffer from another render target to this render target using a duck interface
     * @param renderTarget the render target to copy the color buffer from
     */
    public void aspect$copyColorFrom(RenderTarget renderTarget);
}