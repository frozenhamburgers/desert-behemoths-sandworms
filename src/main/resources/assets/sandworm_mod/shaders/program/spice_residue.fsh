#version 150

#moj_import <lodestone:common_math.glsl>

// Samplers
uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;
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

// Cheap hand-written hash, same family as sonic_boom.fsh's rand().
float hash21(vec2 p) {
	return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

// Periodic 1D value-noise over an angle in [0,1). freq = number of hashed
// cells around the full circle; wraps seamlessly at 0/1 so there's no seam
// at the -PI/PI boundary. Two hash lookups + one smoothstep-mix.
float angularNoise(float angle01, float freq, vec2 seed) {
	float a = angle01 * freq;
	float i0 = mod(floor(a), freq);
	float i1 = mod(i0 + 1.0, freq);
	float f = fract(a);
	float h0 = hash21(seed + vec2(i0, 0.0));
	float h1 = hash21(seed + vec2(i1, 0.0));
	return mix(h0, h1, smoothstep(0.0, 1.0, f));
}

// Procedural blastmark shape in [0,1]: solid crater core + irregular
// radiating streaks + noise-eroded outer edge, all driven by a per-instance
// seed. `offset` is worldPos.xz - center.xz.
float blastMarkMask(vec2 offset, float radius, vec2 seed) {
	float dist = length(offset);
	// atan(y,x) in [-PI, PI], remapped to [0,1) without needing a PI constant.
	float angle01 = atan(offset.y, offset.x) * 0.15915494309 + 0.5;

	// Coarse rays + finer jaggedness on top, blended.
	float n1 = angularNoise(angle01, 9.0, seed);
	float n2 = angularNoise(angle01, 23.0, seed + vec2(17.3, 5.1));
	float rayNoise = n1 * 0.7 + n2 * 0.3;

	// Per-angle streak length: mostly short, occasional long spikes.
	float streakExtent = mix(radius * 0.32, radius, pow(rayNoise, 1.6));

	// Warp the sampled distance so both the core and streak boundaries get a
	// consistent ragged, organic edge instead of a smooth circle/rays.
	float edgeErosion = angularNoise(angle01, 42.0, seed + vec2(3.3, 44.0));
	float erodedDist = dist - (edgeErosion - 0.5) * radius * 0.14;

	float coreRadius = radius * 0.2;
	float core = 1.0 - smoothstep(coreRadius * 0.55, coreRadius, erodedDist);
	float ray = 1.0 - smoothstep(streakExtent * 0.65, streakExtent, erodedDist);

	return max(core, ray);
}

void main() {
	vec4 diffuseColor = texture(DiffuseSampler, texCoord);
	fragColor = diffuseColor;

	vec3 worldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);

	vec3 craterColor = vec3(0.35, 0.05, 0.12); // dark rust core
	vec3 streakColor = vec3(0.62, 0.18, 0.55); // spice purple/magenta rays

	for (int instance = 0; instance < InstanceCount; instance++) {
		int index = instance * 4; // Each instance has 4 values (center.xyz, radius)
		vec3 center = fetch3(DataBuffer, index);
		float radius = fetch(DataBuffer, index + 3);

		vec2 offset = worldPos.xz - center.xz;
		float dist = length(offset);

		if (dist <= radius) {
			vec2 seed = center.xz * 0.073; // per-instance variety from world position

			float mask = blastMarkMask(offset, radius, seed);

			// Fade the whole mark out near the outer radius boundary.
			float radialFalloff = 1.0 - smoothstep(radius * 0.85, radius, dist);
			mask *= radialFalloff;

			float coreAmount = 1.0 - smoothstep(radius * 0.02, radius * 0.12, dist);
			vec3 markColor = mix(streakColor, craterColor, coreAmount);

			fragColor.rgb = mix(fragColor.rgb, markColor, mask * 0.6);
		}
	}
}
