#version 150

#moj_import <lodestone:common_math.glsl>

// Samplers
uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D CutoutDepthSampler;
uniform sampler2D CutoutDiffuseSampler;
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

vec2 getTexCoord(vec2 ScreenSize, vec3 worldPos, mat4 invProjMat, mat4 invViewMat) {
    vec4 clipSpacePos = inverse(invProjMat) * (inverse(invViewMat) * vec4(worldPos, 1.0));
    vec3 ndcSpacePos = clipSpacePos.xyz / clipSpacePos.w;
    return ((ndcSpacePos.xy + 1.0) / 2.0) * ScreenSize;
}

float rand(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 diffuseColor = texture(CutoutDiffuseSampler, texCoord);
    vec3 worldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);
    float mainDepth = getDepth(MainDepthSampler, texCoord);
    float cutoutDepth = getDepth(CutoutDepthSampler, texCoord);

    bool isCutout = cutoutDepth > mainDepth;
    fragColor = diffuseColor;

//    float effectRadius = 20.0;// Radius of the ripple effect
//    float rippleFrequency = 4.0;// Frequency of ripple propagation
//    float rippleSpeed = 2.0; // speed of ripple propagation
//    float rippleMagnitude = 0.1;// Magnitude of the ripple effect
    vec3 color = vec3(1,1,1);

    if(!isCutout) {
        for (int instance = 0; instance < InstanceCount; instance++) {
            int index = instance * 7;// Each instance has 7 values
            vec3 center = fetch3(DataBuffer, index);
            float effectRadius = fetch(DataBuffer, index + 3);
            float rippleSpeed = fetch(DataBuffer, index + 4);
            float rippleMagnitude = fetch(DataBuffer, index + 5);
            float rippleFrequency = fetch(DataBuffer, index + 6);

            // Original texture sampling
            vec4 diffuseColor = texture(CutoutDiffuseSampler, texCoord);

            // Calculate distance from the center of the ripple
            float dist = distance(worldPos, center);

            // Modulate the ripple effect based on distance and angle to create a more realistic distortion
            if (dist <= effectRadius) {
                // Calculate a distortion factor that decreases with distance
                float distortionFactor = 1.0 - (dist / effectRadius);
                distortionFactor = (rand(texCoord)+1)*pow((dist / effectRadius)-1, 2.0);
                // Scale the ripple effect based on the distortion factor
                float x = dist * rippleFrequency - rippleSpeed* -time;
                float scale = rippleMagnitude * distortionFactor;
                float ripple = sin(x) * scale;

                // Apply distortion to texture coordinates
                vec2 distortedTexCoord = texCoord + ripple;

                // Re-sample the texture with the distorted texture coordinates
                vec4 distortedColor = texture(CutoutDiffuseSampler, distortedTexCoord);

                // Mix the original and distorted colors based on the distortion factor
                fragColor = distortedColor;

                // hole
                float distance = length(worldPos - center);
                float falloff = 1.0 - clamp(distance / effectRadius * 3, 0.0, 1.0);
                vec3 holeColor = vec3(clamp(pow(2.0, (color.x * falloff + 1.0) - 0.1), 0.0, 10.0),
                                      clamp(pow(2.0, (color.y * falloff + 1.0) - 0.1), 0.0, 10.0),
                                      clamp(pow(2.0, (color.z * falloff + 1.0) - 0.1), 0.0, 10.0));
                fragColor.rgb /= (10 * color * falloff + 1.0);
            } else {
                // Outside the effect radius, render without distortion
                fragColor = texture(DiffuseSampler, texCoord);
            }
        }
    }
    else fragColor = texture(DiffuseSampler, texCoord);
}