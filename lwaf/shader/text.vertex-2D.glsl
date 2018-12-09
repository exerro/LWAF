#version 400 core

// model attributes
layout (location=0) in vec2 vertex;
layout (location=4) in vec2 uv;

out vec3 fragment_colour;
out vec2 fragment_uv;

uniform vec2 position;
uniform vec2 scale;
uniform vec3 colour;

void main(void) {
    vec3 pos = vec3(-1, 1, 0) + (vec3(position, 0) + vec3(vertex * scale, 0)) * vec3(1, -1, 1);

	gl_Position = vec4(pos, 1.0);
    fragment_colour = colour;
    fragment_uv = uv;
}
