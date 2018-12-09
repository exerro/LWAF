#version 400 core

in vec3 fragment_colour;
in vec2 fragment_uv;

uniform sampler2D textureSampler;
uniform vec3 colour;
uniform bool useTexture;

void main(void) {
    gl_FragColor = vec4(colour * fragment_colour, 1.0);
    if (useTexture) gl_FragColor *= texture(textureSampler, fragment_uv);
}
