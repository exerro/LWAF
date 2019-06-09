#version 400 core

// model attributes
layout (location=0) in vec3 vertex;
layout (location=1) in vec2 vertex_uv;
layout (location=3) in vec3 vertex_colour;

out vec3 fragment_colour;
out vec2 fragment_uv;

uniform mat4 transform;

void main(void) {
	gl_Position = transform * vec4(vertex, 1);
    fragment_colour = vertex_colour;
    fragment_uv = vertex_uv;
}
