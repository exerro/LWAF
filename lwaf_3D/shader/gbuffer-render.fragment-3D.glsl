#version 400 core

in vec3 fragment_position;
in vec3 fragment_normal;
in vec3 fragment_colour;
in vec2 fragment_uv;

// view properties per scene (used to get camera position)
uniform mat4 viewTransform = mat4(1);

// lighting properties per object
uniform float diffuseLightingIntensity = 0.7;
uniform float specularLightingIntensity = 0.4;
uniform float specularLightingPower = 5;

// texturing properties per-object
uniform sampler2D textureSampler;
uniform bool useTexture = false;

layout (location = 0) out vec4 OutColour;
layout (location = 1) out vec4 OutPosition;
layout (location = 2) out vec4 OutNormal;
layout (location = 3) out vec4 OutLighting;

void main(void) {
    OutColour = vec4(fragment_colour, 1.0);
    OutPosition = vec4(fragment_position, 1.0);
    OutNormal = vec4(fragment_normal, 1.0);
    OutLighting = vec4(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, 1);

    if (useTexture) {
        OutColour *= texture(textureSampler, fragment_uv);
    }
}
