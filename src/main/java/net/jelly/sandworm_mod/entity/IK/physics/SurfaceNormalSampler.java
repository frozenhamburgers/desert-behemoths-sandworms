package net.jelly.sandworm_mod.entity.IK.physics;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Samples the terrain under a point/box to estimate a ground normal, used to keep the
 * sandworm's head and body control points conforming to the surface instead of a flat plane.
 * <p>
 * Compared to a plain "4 corners, cross the diagonals" normal, this requires a center sample
 * to confirm there is actually solid ground directly below the point (previously computed but
 * never used), skips any corner that has no ground within the probe range instead of folding a
 * missing/defaulted sample into the normal (e.g. a point hanging over a ledge), and reports
 * AIRBORNE outright when there isn't enough data to trust a normal, so airborne points fall
 * instead of inheriting a stale or fabricated one.
 * <p>
 * The point-based {@link #sample(Level, Vec3, double, int, int)} overload also distinguishes
 * EMBEDDED (the point is itself inside solid ground) from SURFACE (in open air, but close to a
 * surface below) - most of the sandworm's body spends most of its time tunneling deep
 * underground, nowhere near an exposed surface, and that is not the same thing as falling.
 */
public class SurfaceNormalSampler {

    public enum Contact { EMBEDDED, SURFACE, AIRBORNE }

    public record Sample(Contact contact, Vec3 normal, double groundY) {
        static final Sample AIRBORNE = new Sample(Contact.AIRBORNE, new Vec3(0, 1, 0), Double.NEGATIVE_INFINITY);

        public boolean grounded() {
            return contact != Contact.AIRBORNE;
        }
    }

    public static Sample sampleAABB(Level level, AABB box) {
        int minBX = Mth.floor(box.minX);
        int maxBX = Mth.floor(box.maxX);
        int minBZ = Mth.floor(box.minZ);
        int maxBZ = Mth.floor(box.maxZ);
        int topY = Mth.floor(box.maxY);
        int bottomY = Mth.floor(box.minY) - 1;
        int centerX = Mth.floor((box.minX + box.maxX) / 2.0);
        int centerZ = Mth.floor((box.minZ + box.maxZ) / 2.0);
        return sampleSurface(level, minBX, maxBX, minBZ, maxBZ, centerX, centerZ, topY, bottomY);
    }

    public static Sample sample(Level level, Vec3 center, double halfWidth, int probeUp, int probeDown) {
        BlockPos pointPos = BlockPos.containing(center);
        BlockState pointState = level.getBlockState(pointPos);
        if (pointState.isSuffocating(level, pointPos)) {
            // solid ground already supports this point on all sides - no need to simulate
            // anything here, it's not "resting on a surface", it's just buried in one
            return new Sample(Contact.EMBEDDED, new Vec3(0, 1, 0), center.y);
        }

        int minBX = Mth.floor(center.x - halfWidth);
        int maxBX = Mth.floor(center.x + halfWidth);
        int minBZ = Mth.floor(center.z - halfWidth);
        int maxBZ = Mth.floor(center.z + halfWidth);
        int topY = Mth.floor(center.y) + probeUp;
        int bottomY = Mth.floor(center.y) - probeDown;
        return sampleSurface(level, minBX, maxBX, minBZ, maxBZ, Mth.floor(center.x), Mth.floor(center.z), topY, bottomY);
    }

    public static Sample sample(Level level, Vec3 center, double halfWidth) {
        // a point already known to be in open air only needs a short "is the ground about to
        // catch it" check, not a long search - a distant surface far below isn't underfoot yet.
        // probeDown comfortably exceeds a falling control point's per-tick speed cap so a fast
        // fall can't skip past the surface without ever sampling it as nearby.
        return sample(level, center, halfWidth, 1, 5);
    }

    private static Sample sampleSurface(Level level, int minBX, int maxBX, int minBZ, int maxBZ,
                                         int centerX, int centerZ, int topY, int bottomY) {
        BlockPos centerPos = findSurfaceBelow(level, centerX, centerZ, topY, bottomY);
        // no solid ground directly under the point at all - don't fabricate a normal from
        // whatever corners happen to still have ground, just report freefall
        if (centerPos == null) return Sample.AIRBORNE;

        BlockPos[] corners = new BlockPos[]{
                findSurfaceBelow(level, minBX, minBZ, topY, bottomY),
                findSurfaceBelow(level, maxBX, minBZ, topY, bottomY),
                findSurfaceBelow(level, maxBX, maxBZ, topY, bottomY),
                findSurfaceBelow(level, minBX, maxBZ, topY, bottomY)
        };

        int validCorners = 0;
        for (BlockPos p : corners) if (p != null) validCorners++;
        double groundY = centerPos.getY() + 1.0;
        // not enough surrounding terrain to fit a slope (e.g. right at a ledge) - treat as flat
        if (validCorners < 2) return new Sample(Contact.SURFACE, new Vec3(0, 1, 0), groundY);

        // fan-triangulate the 4 corners around the center sample and average the triangle
        // normals, skipping any triangle with a missing corner, rather than a single
        // 2-diagonal cross product that either needs all 4 corners or produces garbage
        Vec3 centerVec = centerPos.getCenter();
        Vec3 normalSum = Vec3.ZERO;
        int triangles = 0;
        for (int i = 0; i < 4; i++) {
            BlockPos a = corners[i];
            BlockPos b = corners[(i + 1) % 4];
            if (a == null || b == null) continue;
            Vec3 triNormal = a.getCenter().subtract(centerVec).cross(b.getCenter().subtract(centerVec));
            if (triNormal.lengthSqr() < 1.0E-6) continue;
            triNormal = triNormal.normalize();
            if (triNormal.y < 0) triNormal = triNormal.scale(-1);
            normalSum = normalSum.add(triNormal);
            triangles++;
        }
        if (triangles == 0) return new Sample(Contact.SURFACE, new Vec3(0, 1, 0), groundY);
        return new Sample(Contact.SURFACE, normalSum.scale(1.0 / triangles).normalize(), groundY);
    }

    private static BlockPos findSurfaceBelow(Level level, int x, int z, int topY, int bottomY) {
        for (int y = topY; y >= bottomY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.isSuffocating(level, pos) && level.getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }
        return null;
    }
}
