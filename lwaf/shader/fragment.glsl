#version 400 core

// from vertex shader
in vec3 fragment_colour;

void main(void) {
	gl_FragColor = vec4(fragment_colour, 1.0);
}
