package net.jelly.sandworm_mod.entity.IK.physics;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A lightweight, non-entity physics point used to anchor a section of the worm's FABRIK chain
 * to the terrain, instead of letting that section hang purely on IK math between the root and
 * the head. Several of these spaced along the body let the chain sag/climb with the ground
 * independently at each anchor, instead of the whole body just draping backward from the head.
 * <p>
 * The only job here is to catch segments left suspended in open air (most
 * noticeable while riding), not to simulate realistic ground physics for the whole body - that
 * stays the head-while-mounted's job (see {@link WormPhysics#resolveGroundVelocity}). So this is
 * just gravity plus a speed cap, landing and stopping once it reaches a surface.
 */
public class WormControlPoint {
    private Vec3 position;
    private Vec3 velocity = Vec3.ZERO;
    private boolean grounded = false;

    public WormControlPoint(Vec3 initialPosition) {
        this.position = initialPosition;
    }

    public Vec3 getPosition() {
        return position;
    }

    public boolean isGrounded() {
        return grounded;
    }

    /**
     * Re-syncs to where the FABRIK chain currently has this anchor (correcting any solver
     * slack), then integrates one tick of simple gravity on top of it.
     */
    public void tick(Level level, Vec3 chainPosition, Vec3 gravity, double maxSpeed, double probeHalfWidth) {
        position = chainPosition;

        SurfaceNormalSampler.Sample ground = SurfaceNormalSampler.sample(level, position, probeHalfWidth);

        if (ground.contact() == SurfaceNormalSampler.Contact.EMBEDDED) {
            // solid ground already surrounds this point on every side - most of the body spends
            // most of its time like this while tunneling, and it needs no physics at all here,
            // just let it track wherever the chain solves to instead of tugging it down with
            // gravity for lack of a nearby exposed surface
            velocity = Vec3.ZERO;
            grounded = true;
            return;
        }

        velocity = velocity.add(gravity);
        if (velocity.length() > maxSpeed) velocity = velocity.normalize().scale(maxSpeed);
        position = position.add(velocity);

        // land on a surface once reached instead of falling through it forever - for a truly
        // airborne point (no surface found nearby) groundY() is -infinity and this never trips,
        // so it just keeps falling, which is the whole point of this class
        if (position.y <= ground.groundY()) {
            position = new Vec3(position.x, ground.groundY(), position.z);
            velocity = Vec3.ZERO;
            grounded = true;
        } else {
            grounded = false;
        }
    }
}
