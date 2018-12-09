#version 400 core

in vec3 fragment_colour;
in vec2 fragment_uv;

uniform sampler2D textureSampler;

void main(void) {
	gl_FragColor = vec4(1, 1, 1, 1);
	gl_FragColor = vec4(fragment_colour, 1.0) * texture(textureSampler, fragment_uv);
	gl_FragColor = texture(textureSampler, fragment_uv);
}
