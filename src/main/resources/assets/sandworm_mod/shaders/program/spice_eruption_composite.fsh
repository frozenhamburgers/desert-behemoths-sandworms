#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D CloudSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
	vec4 bg = texture(DiffuseSampler, texCoord);
	vec4 cloud = texture(CloudSampler, texCoord);
	fragColor = vec4(mix(bg.rgb, cloud.rgb, clamp(cloud.a, 0.0, 1.0)), bg.a);
}
