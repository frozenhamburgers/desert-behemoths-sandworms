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

// returns tex coord in xy, with whether or not the tex coord is visible in z.
vec3 textureCoord(vec3 worldPos) {
	// Step 1: Convert world space to view space
	vec4 viewSpacePosition = inverse(invViewMat) * vec4(worldPos - cameraPos, 1.0);

	// Step 2: Convert view space to clip space
	vec4 clipSpacePosition = inverse(invProjMat) * viewSpacePosition;

	// Step 3: Convert clip space to NDC
	vec3 ndc = clipSpacePosition.xyz / clipSpacePosition.w;

	// Step 4: Convert NDC to texture coordinates
	vec2 texCoord = (ndc.xy * 0.5) + 0.5;

	// Check visibility: NDC should be in range [-1, 1] for visible coordinates
	//bool isVisible = all(lessThanEqual(abs(ndc), vec3(1.5)));
	bool isVisible = viewSpacePosition.z < 0;

	// Return texture coordinates and visibility as a vec3 (x, y, visibility)
	return vec3(texCoord, isVisible ? 1.0 : 0.0);
}

float rand(vec2 co){
	return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
	vec4 diffuseColor = texture(DiffuseSampler, texCoord);
	vec3 worldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);
	// Its important to set the fragColor to the diffuseColor before applying the effect!
	fragColor = diffuseColor;

	float radius = 70.0; // Change this value to modify the falloff of the light, or make it a uniform
	for (int instance = 0; instance < InstanceCount; instance++) {
		int index = instance * 7;// Each instance has 6 values
		vec3 center = fetch3(DataBuffer, index);
		float effectRadius = fetch(DataBuffer, index + 3);
		float rippleSpeed = fetch(DataBuffer, index + 4);
		float rippleMagnitude = fetch(DataBuffer, index + 5);
		float rippleFrequency = fetch(DataBuffer, index + 6);

		vec3 centerTexCoord = textureCoord(center);
		if(centerTexCoord.z == 1.0) {
		float dist = distance(texCoord, centerTexCoord.xy);
			// Modulate the ripple effect based on distance and angle to create a more realistic distortion
			if (dist >= 0 && dist <= effectRadius) {
				float distanceFalloff =  1.0 - clamp(distance(cameraPos, center) / radius, 0.0, 1.0);
				// Calculate a distortion factor that decreases with distance
				float distortionFactor = 1.0 - (dist / effectRadius);
				//distortionFactor = (rand(texCoord)+1)*pow((dist / effectRadius)-1, 2.0);
				distortionFactor = (pow((dist / effectRadius)-1, 2.0));
				// Scale the ripple effect based on the distortion factor
				float x = dist * rippleFrequency - rippleSpeed* time;
				float scale = distanceFalloff * rippleMagnitude * distortionFactor;
				float ripple = sin(x) * scale;

				// Apply distortion to texture coordinates
				vec2 distortedTexCoord = texCoord + ripple;

				// Re-sample the texture with the distorted texture coordinates
				vec4 distortedColor = texture(DiffuseSampler, distortedTexCoord);

				// Mix the original and distorted colors based on the distortion factor
				fragColor = distortedColor;
//				float falloff = 1.0 - clamp(dist / effectRadius, 0.0, 1.0);
//				float distanceFalloff =  1.0 - clamp(distance(cameraPos, center) / radius, 0.0, 1.0);
//				fragColor.rgb *= (color * falloff * distanceFalloff + 1.0);
			}
		}
	}
}