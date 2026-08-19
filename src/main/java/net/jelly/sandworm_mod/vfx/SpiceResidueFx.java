package net.jelly.sandworm_mod.vfx;

import org.joml.Vector3f;
import team.lodestar.lodestone.systems.postprocess.DynamicShaderFxInstance;

import java.util.function.BiConsumer;

public class SpiceResidueFx extends DynamicShaderFxInstance {
    public Vector3f center;
    public float radius;

    public SpiceResidueFx(Vector3f center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, center.x());
        writer.accept(1, center.y());
        writer.accept(2, center.z());
        writer.accept(3, radius);
    }
}
