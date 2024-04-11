#version 150
// The game's render output
uniform sampler2D DiffuseSampler;
// The texture coordinate represented as a 2D vector (x,y)
in vec2 texCoord;
// The output color of each pixel represented as a 4D vector (r,g,b,a)
out vec4 fragColor;

void main() {
	// Extract the original color of the pixel from the DiffuseSampler
	vec4 original = texture(DiffuseSampler, texCoord);
	// Pink!
	vec3 tintColor = vec3(1.0,0.0,1.0);
	// Multiply each rgba value by the tint color.
	vec4 result = original + vec4(tintColor, 1.0);

	// Set the fragColor output as the result
	fragColor = original;
}