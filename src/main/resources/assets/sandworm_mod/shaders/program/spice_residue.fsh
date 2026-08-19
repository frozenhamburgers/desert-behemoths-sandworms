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

// Placeholder residue look: tints ground within radius of each instance pink.
// Will be replaced with a proper spice-sand texture/depletion-grid sample later.
void main() {
	vec4 diffuseColor = texture(DiffuseSampler, texCoord);
	fragColor = diffuseColor;

	vec3 worldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);
	vec3 spiceColor = vec3(1.0, 0.35, 0.75);

	for (int instance = 0; instance < InstanceCount; instance++) {
		int index = instance * 4; // Each instance has 4 values (center.xyz, radius)
		vec3 center = fetch3(DataBuffer, index);
		float radius = fetch(DataBuffer, index + 3);

		float dist = distance(worldPos.xz, center.xz);
		if (dist <= radius) {
			float falloff = 1.0 - smoothstep(radius * 0.8, radius, dist);
			fragColor.rgb = mix(fragColor.rgb, spiceColor, falloff * 0.55);
		}
	}
}
