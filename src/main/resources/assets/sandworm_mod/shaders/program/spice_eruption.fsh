#version 150

#moj_import <lodestone:common_math.glsl>

uniform sampler2D MainDepthSampler;
uniform samplerBuffer DataBuffer;
uniform int InstanceCount;
uniform mat4 invProjMat;
uniform mat4 invViewMat;
uniform vec3 cameraPos;
uniform vec2 ScreenSize;
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

const int MAX_STEPS = 40;
const float RENDER_DISTANCE_CAP = 512.0;

float hash31(vec3 p) {
	p = fract(p * vec3(0.1031, 0.1030, 0.0973));
	p += dot(p, p.yzx + 33.33);
	return fract((p.x + p.y) * p.z);
}

// Trilinear 8-corner value noise, same smoothstep-interpolation style as this mod's 2D noise2D.
float noise3D(vec3 p) {
	vec3 i = floor(p);
	vec3 f = fract(p);
	vec3 u = f * f * (3.0 - 2.0 * f);

	float n000 = hash31(i + vec3(0.0, 0.0, 0.0));
	float n100 = hash31(i + vec3(1.0, 0.0, 0.0));
	float n010 = hash31(i + vec3(0.0, 1.0, 0.0));
	float n110 = hash31(i + vec3(1.0, 1.0, 0.0));
	float n001 = hash31(i + vec3(0.0, 0.0, 1.0));
	float n101 = hash31(i + vec3(1.0, 0.0, 1.0));
	float n011 = hash31(i + vec3(0.0, 1.0, 1.0));
	float n111 = hash31(i + vec3(1.0, 1.0, 1.0));

	float nx00 = mix(n000, n100, u.x);
	float nx10 = mix(n010, n110, u.x);
	float nx01 = mix(n001, n101, u.x);
	float nx11 = mix(n011, n111, u.x);

	float nxy0 = mix(nx00, nx10, u.y);
	float nxy1 = mix(nx01, nx11, u.y);

	return mix(nxy0, nxy1, u.z);
}

float fbm3(vec3 p) {
	float sum = noise3D(p) * 0.55;
	sum += noise3D(p * 2.15) * 0.30;
	sum += noise3D(p * 4.6) * 0.15;
	return sum;
}

// Far-plane world position for this texCoord.
vec3 getFarWorldPos(vec2 uv) {
	vec4 clipSpacePosition = vec4(uv * 2.0 - 1.0, 1.0, 1.0);
	vec4 viewSpacePosition = invProjMat * clipSpacePosition;
	viewSpacePosition /= viewSpacePosition.w;
	vec4 localSpacePosition = invViewMat * viewSpacePosition;
	return cameraPos + localSpacePosition.xyz;
}

// Analytic ray intersection against a vertical cylinder (radius `boundRadius`, axis through
// base.xz) clipped to the Y-slab [base.y, base.y+height]. O(1) - lets pixels that never touch
// the column skip the raymarch loop entirely, and bounds the loop's march interval otherwise.
bool intersectColumnBounds(vec3 ro, vec3 rd, vec3 base, float boundRadius, float height,
		out float tEnter, out float tExit) {
	float ocx = ro.x - base.x;
	float ocz = ro.z - base.z;
	float a = rd.x * rd.x + rd.z * rd.z;
	float cylEnter, cylExit;
	if (a < 1e-6) {
		// Near-vertical ray: horizontal position barely changes, so it's either always
		// inside the infinite cylinder or never.
		if (ocx * ocx + ocz * ocz > boundRadius * boundRadius) return false;
		cylEnter = -1e6;
		cylExit = 1e6;
	} else {
		float b = 2.0 * (ocx * rd.x + ocz * rd.z);
		float c = ocx * ocx + ocz * ocz - boundRadius * boundRadius;
		float disc = b * b - 4.0 * a * c;
		if (disc < 0.0) return false;
		float sq = sqrt(disc);
		cylEnter = (-b - sq) / (2.0 * a);
		cylExit = (-b + sq) / (2.0 * a);
	}

	float yEnter, yExit;
	if (abs(rd.y) < 1e-6) {
		if (ro.y < base.y || ro.y > base.y + height) return false;
		yEnter = -1e6;
		yExit = 1e6;
	} else {
		float t1 = (base.y - ro.y) / rd.y;
		float t2 = (base.y + height - ro.y) / rd.y;
		yEnter = min(t1, t2);
		yExit = max(t1, t2);
	}

	tEnter = max(max(cylEnter, yEnter), 0.0);
	tExit = min(cylExit, yExit);
	return tEnter < tExit;
}

// Density in [0,1] at world position `p` for one eruption instance. `t` seeds the noise's
// vertical scroll - driven by the instance's own age (not the global time uniform) so
// multiple simultaneous eruptions don't billow in lockstep.
float densityAt(vec3 p, vec3 base, float baseRadius, float topRadius, float height,
		float seed, float growFrac, float t) {
	float h = clamp((p.y - base.y) / height, 0.0, 1.0);
	// The column visibly grows upward over its own early lifetime rather than
	// popping in fully-formed.
	if (h > growFrac) return 0.0;

	float colRadius = mix(baseRadius, topRadius, pow(h, 0.7));
	float r = distance(p.xz, base.xz);
	float radialMask = 1.0 - smoothstep(colRadius * 0.6, colRadius, r);
	if (radialMask <= 0.0) return 0.0;

	// Domain-warped noise scrolling downward relative to the column so it reads as
	// continuously rising/billowing.
	vec3 noiseP = vec3(p.x * 0.15 + seed, p.y * 0.12 - t * 0.6, p.z * 0.15 + seed * 1.3);
	float n = fbm3(noiseP);

	float topFade = 1.0 - smoothstep(growFrac * 0.85, growFrac, h);
	return radialMask * clamp(n * 1.3 - 0.15, 0.0, 1.0) * topFade;
}

// Color at world position `p`: brighter/hotter near the column's axis and base, darker/cooler
// toward the outer radius and top - same purple palette as the residue ground shader for
// thematic continuity, plus a bright internal-glow core. The core falloff is deliberately wide
// so most of the volume reads as a glowing purple haze rather than dark indigo - a camera close
// to or inside the column (many consecutive dense samples, alpha saturates fast) should look
// like standing in a bright glowing mist, not a dark wall.
vec3 eruptionColor(vec3 p, vec3 base, float topRadius, float height) {
	vec3 deepIndigo = vec3(0.16, 0.09, 0.42);
	vec3 vividPurple = vec3(0.60, 0.26, 0.72);
	vec3 hotCore = vec3(1.00, 0.75, 0.95);

	float h = clamp((p.y - base.y) / height, 0.0, 1.0);
	float r = distance(p.xz, base.xz) / max(topRadius, 0.001);
	float core = (1.0 - smoothstep(0.0, 0.85, r)) * (1.0 - h * 0.35);

	vec3 col = mix(deepIndigo, vividPurple, clamp(core * 1.4, 0.0, 1.0));
	col = mix(col, hotCore, clamp(core * 1.4 - 0.5, 0.0, 1.0) * (1.0 - h * 0.5));
	return col;
}

void main() {
	fragColor = vec4(0.0);

	vec3 rayOrigin = cameraPos;
	vec3 rayDir = normalize(getFarWorldPos(texCoord) - cameraPos);

	float sceneDepth = getDepth(MainDepthSampler, texCoord);
	float tMaxScene = RENDER_DISTANCE_CAP;
	if (sceneDepth < 0.9999) {
		vec3 sceneWorldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);
		tMaxScene = length(sceneWorldPos - cameraPos);
	}

	for (int instance = 0; instance < InstanceCount; instance++) {
		if (fragColor.a > 0.97) break;

		int idx = instance * 9;
		vec3 base = fetch3(DataBuffer, idx);
		float baseRadius = fetch(DataBuffer, idx + 3);
		float topRadius = fetch(DataBuffer, idx + 4);
		float height = fetch(DataBuffer, idx + 5);
		float seed = fetch(DataBuffer, idx + 6);
		float age = fetch(DataBuffer, idx + 7);
		float lifetime = fetch(DataBuffer, idx + 8);

		float lifeFrac = clamp(age / max(lifetime, 0.001), 0.0, 1.0);
		float growFrac = smoothstep(0.0, 0.25, lifeFrac);
		float fadeOut = 1.0 - smoothstep(0.75, 1.0, lifeFrac);
		if (fadeOut <= 0.001 || growFrac <= 0.001) continue;

		float boundRadius = topRadius * 1.25;
		float tEnter, tExit;
		if (!intersectColumnBounds(rayOrigin, rayDir, base, boundRadius, height, tEnter, tExit)) continue;
		tExit = min(tExit, tMaxScene);
		if (tEnter >= tExit) continue;

		float dt = (tExit - tEnter) / float(MAX_STEPS);
		float t = tEnter;
		for (int i = 0; i < MAX_STEPS; i++) {
			if (fragColor.a > 0.97) break;
			vec3 p = rayOrigin + rayDir * t;
			float dens = densityAt(p, base, baseRadius, topRadius, height, seed, growFrac, age) * fadeOut;
			if (dens > 0.003) {
				float stepAlpha = clamp(dens * dt * 0.4, 0.0, 1.0);
				vec3 stepColor = eruptionColor(p, base, topRadius, height);
				fragColor.rgb = mix(fragColor.rgb, stepColor, stepAlpha * (1.0 - fragColor.a));
				fragColor.a += (1.0 - fragColor.a) * stepAlpha;
			}
			t += dt;
		}
	}

	// Never fully occludes the screen, even with the camera close to or inside the column
	fragColor.a = min(fragColor.a, 0.82);
}
