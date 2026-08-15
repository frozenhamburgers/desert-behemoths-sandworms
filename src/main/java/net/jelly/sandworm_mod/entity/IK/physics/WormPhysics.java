package net.jelly.sandworm_mod.entity.IK.physics;

import net.minecraft.world.phys.Vec3;

/**
 * Shared force-resolution math for anything moving along the sandworm's ground: the rider's
 * steering point at the head, and the body's control points. Given a velocity, an external
 * force (rider steering, or none), gravity, and a sampled ground normal, applies gravity,
 * projects out the into-the-ground component against the surface normal, and applies friction.
 */
public class WormPhysics {
    private WormPhysics() {}

    public static Vec3 resolveGroundVelocity(Vec3 velocity, Vec3 externalForce, Vec3 gravity,
                                               SurfaceNormalSampler.Sample ground, double frictionCoefficient) {
        Vec3 netForce = externalForce.add(gravity);
        if (!ground.grounded()) {
            return velocity.add(netForce);
        }

        Vec3 normal = ground.normal();
        Vec3 normalForce = normal.scale(-netForce.dot(normal));
        Vec3 result = velocity.add(netForce).add(normalForce);

        Vec3 frictionForce = result.scale(normalForce.length()).scale(-frictionCoefficient);
        result = result.add(frictionForce);

        // remove any remaining into-the-ground component of velocity
        if (result.dot(normal) < 0) {
            result = result.subtract(normal.scale(result.dot(normal)));
        }
        return result;
    }
}
