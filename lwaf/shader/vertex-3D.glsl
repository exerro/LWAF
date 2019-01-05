#version 400 core

// model attributes
layout (location=0) in vec3 vertex;
layout (location=1) in vec2 vertex_uv;
layout (location=2) in vec3 vertex_normal;
layout (location=3) in vec3 vertex_colour;

out vec3 fragment_position;
out vec3 fragment_colour;
out vec3 fragment_normal;
out vec2 fragment_uv;

uniform mat4 projectionTransform;
uniform mat4 viewTransform;
uniform mat4 transform;

void main(void) {
	gl_Position = projectionTransform * viewTransform * transform * vec4(vertex, 1);
	
	fragment_position = (transform * vec4(vertex, 1)).xyz;
	fragment_normal = normalize((transpose(inverse(transform)) * vec4(vertex_normal, 0)).xyz);
    fragment_colour = vertex_colour;
    fragment_uv = vertex_uv;
}
