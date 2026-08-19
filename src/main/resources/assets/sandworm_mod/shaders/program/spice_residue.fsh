#version 150

#moj_import <lodestone:common_math.glsl>

// Samplers
uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;
// Depth of the full current scene (blocks + entities + everything else),
// captured late - contrasted against MainDepthSampler (blocks only, captured
// right after terrain) to test whether something is now drawn in front of
// the ground here, so the mark doesn't paint over it. See
// SpiceResiduePostProcessor#copyBlockDepthBuffer for how MainDepthSampler is
// populated, and PostProcessor#copyDepthBuffer (Lodestone base class) for how
// this one still is.
uniform sampler2D SceneDepthSampler;
// Multi-Instance uniforms
uniform samplerBuffer DataBuffer;
uniform int InstanceCount;
// Matrices needed for world position calculation
uniform mat4 invProjMat;
uniform mat4 invViewMat;
// camera pos
uniform vec3 cameraPos;
uniform vec2 ScreenSize;
// time
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

// Vertical extent of the effect, in blocks, relative to each instance's center
// height. Kept small and asymmetric so the mark hugs the surface on uneven
// terrain without bleeding into caves below or painting anything airborne
// above (e.g. the sky, or entities standing over/near the patch).
const float V_RADIUS_UP = 1.5;
const float V_RADIUS_DOWN = 3.5;

// Cheap hand-written hash, same family as sonic_boom.fsh's rand().
float hash21(vec2 p) {
	return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

// Cheap 2D value noise (bilinear-interpolated hash) - the base octave fbm() builds on.
float noise2D(vec2 p) {
	vec2 i = floor(p);
	vec2 f = fract(p);
	float a = hash21(i);
	float b = hash21(i + vec2(1.0, 0.0));
	float c = hash21(i + vec2(0.0, 1.0));
	float d = hash21(i + vec2(1.0, 1.0));
	vec2 u = f * f * (3.0 - 2.0 * f);
	return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

// Two-octave fractal brownian motion.
float fbm(vec2 p) {
	float sum = noise2D(p) * 0.6;
	sum += noise2D(p * 2.3) * 0.4;
	return sum;
}

const float STREAK_COUNT = 28.0;

struct BlastMasks {
	float haze;           // broad, low-opacity dust field covering the whole
	                       // footprint, independent of any ray's position -
	                       // background texture only, kept weak so it doesn't
	                       // wash out the rays and read as a uniform "donut"
	                       // instead of a directional burst.
	float colorPatch;     // slow, broad field for blending the haze's color
	                       // between purple and indigo across large mottled
	                       // regions.
	float halo;           // soft, wide glow from the same rays as coreMask.
	float core;           // wide, petal-like rays - almost all starting close
	                       // to the untouched center and reaching fairly far
	                       // out, tapering to a point at their tip - with
	                       // heavy blur so edges stay soft, but with enough
	                       // contrast against the haze to read as a
	                       // directional blast rather than uniform noise.
	float coreColorShift; // [0,1] color roll of whichever single ray is
	                       // dominant at this pixel (see `c` in the loop
	                       // below) - fixed per ray, not spatial noise, so an
	                       // entire streak reads as one consistent color
	                       // instead of drifting along its own length.
};

// `offset` is worldPos.xz - center.xz, `dist` is its length.
BlastMasks blastMarkMasks(vec2 offset, float dist, vec3 worldPos, float radius, vec2 seed) {
	float angle01 = atan(offset.y, offset.x) * 0.15915494309 + 0.5;

	float halo = 0.0;
	float core = 0.0;
	float coreColorShift = 0.0;

	float cell = floor(angle01 * STREAK_COUNT);
	for (int k = -1; k <= 1; k++) {
		float c = mod(cell + float(k), STREAK_COUNT);

		// Exact angle of this candidate ray's centerline, jittered within its slot.
		float lineAngle01 = (c + hash21(seed + vec2(c, 1.0))) / STREAK_COUNT;
		float angleDelta = angle01 - lineAngle01;
		angleDelta -= floor(angleDelta + 0.5); // wrap to [-0.5, 0.5)
		// Perpendicular distance from this fragment to the ray's centerline,
		// in world units (arc length), not raw angle.
		float perp = abs(angleDelta) * 6.2831853 * dist;

		// Rays reach fairly far out and almost all start close to the center,
		// like a burst rather than debris scattered at random distances.
		float len = mix(radius * 0.55, radius * 1.0, hash21(seed + vec2(c, 2.0)));
		float startDist = hash21(seed + vec2(c, 5.0)) * radius * 0.12;
		float endDist = min(startDist + len, radius * 1.05);

		// Wide, petal-like rays - not hairline cracks.
		float baseWidth = mix(radius * 0.03, radius * 0.11, pow(hash21(seed + vec2(c, 3.0)), 1.4));

		// Wide, soft fades at both ends of the ray's own span.
		float rampIn = max(len * 0.2, radius * 0.03);
		float rampOut = max(len * 0.7, radius * 0.15);
		float fadeIn = smoothstep(startDist, startDist + rampIn, dist);
		float fadeOut = 1.0 - smoothstep(endDist - rampOut, endDist, dist);
		float alongFade = fadeIn * fadeOut;
		float taperedWidth = max(baseWidth * alongFade, radius * 0.0015);

		// A generous floor on how tight the transition can get, so rays read
		// as heavily feathered rather than having a discernible edge. The
		// wobble itself is sampled at a high, fixed frequency (independent of
		// radius) so its amplitude - which does scale with radius, same as
		// the blur band - always spans many noise cycles instead of just a
		// couple: fine sand grain at any blow size, rather than a handful of
		// big blobs that read as smeared paint on larger blows.
		float minBlur = radius * 0.07;
		float smudge = (fbm(worldPos.xz * 9.0 + seed + c) - 0.5) * minBlur * 1.4;
		float p = perp + smudge;

		halo = max(halo, (1.0 - smoothstep(taperedWidth * 0.5, taperedWidth * 2.2 + minBlur * 2.2, p)) * alongFade);

		float coreCandidate = (1.0 - smoothstep(taperedWidth * 0.15, taperedWidth * 0.7 + minBlur * 1.0, p)) * alongFade;
		if (coreCandidate > core) {
			core = coreCandidate;
			// Fixed roll for this specific ray (`c`), biased toward small
			// shifts so most streaks stay close to deep indigo and only a
			// few drift noticeably toward the pinker ray color.
			coreColorShift = pow(hash21(seed + vec2(c, 6.0)), 2.0) * 0.55;
		}
	}

	// Broad dust haze covering the whole footprint - kept low-opacity so it
	// reads as background texture, not competing with the rays for attention.
	// Sampled at a fine, fixed frequency for a sandy grain rather than a
	// blurry, paint-like wash.
	float mottle = fbm(worldPos.xz * 6.0 + seed) * 0.6 + fbm(worldPos.xz * 14.0 + seed + 19.0) * 0.4;
	float haze = mix(0.25, 0.55, mottle);

	// Slow, broad noise field for blending haze color between purple and
	// indigo across large mottled regions.
	float colorPatch = fbm(worldPos.xz * 0.9 + seed + 50.0);

	// Leave the very center of the mark untouched - a softer transition for
	// the haze/halo, a tighter one for the core. The boundary itself is
	// perturbed by broad, low-frequency noise so it reads as an irregular
	// blast edge instead of a perfect circle.
	float centerErosion = fbm(worldPos.xz * 2.8 + seed + 80.0);
	float centerDist = max(dist + (centerErosion - 0.5) * radius * 0.16, 0.0);
	float untouchedRadius = radius * 0.12;
	float centerMaskSoft = smoothstep(untouchedRadius * 0.5, untouchedRadius * 3.0, centerDist);
	float centerMaskTight = smoothstep(untouchedRadius, untouchedRadius * 1.6, centerDist);

	// The haze is strongest right outside the untouched center, gradually
	// weakening further out.
	float radialDecay = mix(1.0, 0.5, smoothstep(radius * 0.5, radius * 1.0, dist));

	BlastMasks result;
	result.haze = haze * centerMaskSoft * radialDecay;
	result.colorPatch = colorPatch;
	result.halo = halo * centerMaskSoft;
	result.core = core * centerMaskTight;
	result.coreColorShift = coreColorShift;
	return result;
}

void main() {
	vec4 diffuseColor = texture(DiffuseSampler, texCoord);
	fragColor = diffuseColor;

	vec3 worldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);

	// Occlusion test: compare the terrain-only depth against the full current
	// scene's depth (same pixel). If something is now drawn nearer to the
	// camera than the ground was, this pixel is covered (by an entity, a
	// dropped item, translucent geometry, etc.) and the mark shouldn't show
	// through it. Compared in linear view-space Z so the transition band is a
	// consistent size in world units regardless of distance from the camera.
	float blockViewZ = viewSpaceFromDepth(getDepth(MainDepthSampler, texCoord), texCoord, invProjMat).z;
	float sceneViewZ = viewSpaceFromDepth(getDepth(SceneDepthSampler, texCoord), texCoord, invProjMat).z;
	float visibility = 1.0 - smoothstep(0.0, 0.25, sceneViewZ - blockViewZ);
	if (visibility <= 0.001) return;

	vec3 vividPurple = vec3(0.60, 0.26, 0.72); // saturated purple/magenta, the ray color
	vec3 deepIndigo = vec3(0.16, 0.09, 0.42);  // deeper blue-violet, ray cores and haze patches

	for (int instance = 0; instance < InstanceCount; instance++) {
		int index = instance * 4; // Each instance has 4 values (center.xyz, radius)
		vec3 center = fetch3(DataBuffer, index);
		float radius = fetch(DataBuffer, index + 3);

		vec2 offset = worldPos.xz - center.xz;
		float dist = length(offset);

		// Bound the effect to a vertically-squashed ellipsoid around the
		// instance's center instead of an infinite vertical column, so it
		// doesn't seep underground or stain the sky/entities above.
		float vOffset = worldPos.y - center.y;
		float vRadius = vOffset >= 0.0 ? V_RADIUS_UP : V_RADIUS_DOWN;
		float ellipsoidDist = length(vec2(dist / radius, vOffset / vRadius));

		// Loosened gate (vs. the eroded boundary below) so noise can push the
		// visible edge slightly past the nominal ellipsoid in places without
		// those pixels getting skipped outright.
		if (dist <= radius * 1.2 && ellipsoidDist <= 1.2) {
			vec2 seed = center.xz * 0.073; // per-instance variety from world position

			BlastMasks masks = blastMarkMasks(offset, dist, worldPos, radius, seed);

			// Fade the whole mark out near the ellipsoid boundary (horizontal
			// or vertical, whichever is reached first) - perturbed by broad
			// noise so the outer edge reads as an irregular blast boundary
			// instead of a perfect ellipse.
			float edgeErosion = fbm(worldPos.xz * 3.2 + seed + 130.0);
			float erodedEllipsoidDist = ellipsoidDist + (edgeErosion - 0.5) * 0.3;
			float radialFalloff = 1.0 - smoothstep(0.5, 1.0, erodedEllipsoidDist);

			// Background haze first - weak, mottled between purple and indigo.
			vec3 hazeColor = mix(vividPurple, deepIndigo, smoothstep(0.3, 0.75, masks.colorPatch));
			float hazeAlpha = masks.haze * radialFalloff * 0.4 * visibility;
			fragColor.rgb = mix(fragColor.rgb, hazeColor, hazeAlpha);

			// Rays layer on top, clearly stronger than the haze so the burst
			// actually reads as directional.
			float haloAlpha = masks.halo * radialFalloff * 0.6 * visibility;
			fragColor.rgb = mix(fragColor.rgb, vividPurple, haloAlpha);

			// Cracks mostly read as deep indigo, but each whole streak can
			// roll a shift toward the pinker ray color instead of always
			// being one fixed shade (see BlastMasks.coreColorShift).
			vec3 coreColor = mix(deepIndigo, vividPurple, masks.coreColorShift);
			float coreAlpha = masks.core * radialFalloff * 0.5 * visibility;
			fragColor.rgb = mix(fragColor.rgb, coreColor, coreAlpha);
		}
	}
}
