#version 400 core

// model attributes
layout (location=0) in vec3 vertex;
layout (location=2) in vec3 vertex_colour;

out vec3 fragment_colour;

uniform vec2 position;
uniform vec2 size;
uniform vec3 colour;

void main(void) {
    vec3 pos = vec3(-1, 1, 0) + (vec3(position, 0) + vertex * vec3(size, 1)) * vec3(1, -1, 1);

	gl_Position = vec4(pos, 1.0);
    fragment_colour = vertex_colour * colour;
}
