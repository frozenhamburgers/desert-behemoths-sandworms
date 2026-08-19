package net.jelly.sandworm_mod.vfx;

import org.joml.Vector3f;
import team.lodestar.lodestone.systems.postprocess.DynamicShaderFxInstance;

import java.util.function.BiConsumer;

// age/lifetime are driven explicitly from SpiceBlowWorldEvent's own tick countdown (same idiom
// SonicBoomWorldEvent uses for its fx fields) rather than relying on DynamicShaderFxInstance's
// own getTime() - that accumulates from Minecraft.getDeltaFrameTime(), whose units aren't used
// anywhere else in this codebase and weren't worth trusting blind for lifecycle-critical timing.
public class SpiceEruptionFx extends DynamicShaderFxInstance {
    public Vector3f base;
    public float baseRadius;
    public float topRadius;
    public float height;
    public float seed;
    public float age;
    public float lifetime;

    public SpiceEruptionFx(Vector3f base, float baseRadius, float topRadius, float height, float seed, float lifetime) {
        this.base = base;
        this.baseRadius = baseRadius;
        this.topRadius = topRadius;
        this.height = height;
        this.seed = seed;
        this.lifetime = lifetime;
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, base.x());
        writer.accept(1, base.y());
        writer.accept(2, base.z());
        writer.accept(3, baseRadius);
        writer.accept(4, topRadius);
        writer.accept(5, height);
        writer.accept(6, seed);
        writer.accept(7, age);
        writer.accept(8, lifetime);
    }
}
