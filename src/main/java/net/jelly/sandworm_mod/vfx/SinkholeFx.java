package net.jelly.sandworm_mod.vfx;

import org.joml.Vector3f;
import team.lodestar.lodestone.systems.postprocess.DynamicShaderFxInstance;

import java.util.function.BiConsumer;

public class SinkholeFx extends DynamicShaderFxInstance {
    public Vector3f center;
    public float radius;
    public float speed;
    public float magnitude;
    public float frequency;

    public SinkholeFx(Vector3f center, float radius, float speed, float magnitude, float frequency) {
        this.center = center;
        this.radius = radius;
        this.speed = speed;
        this.magnitude = magnitude;
        this.frequency = frequency;
    }

    @Override
    public void writeDataToBuffer(BiConsumer<Integer, Float> writer) {
        writer.accept(0, center.x());
        writer.accept(1, center.y());
        writer.accept(2, center.z());
        writer.accept(3, radius);
        writer.accept(4, speed);
        writer.accept(5, magnitude);
        writer.accept(6, frequency);
    }
}
