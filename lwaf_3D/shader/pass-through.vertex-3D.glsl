#version 400 core

layout (location=0) in vec3 vertex;
layout (location=1) in vec2 uv_in;

uniform mat4 transform;

out vec2 uv;

void main(void) {
	gl_Position = transform * vec4(vertex, 1);
	uv = uv_in;
}
