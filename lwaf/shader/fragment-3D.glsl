#version 400 core

const float SPECULAR_POWER = 5;
const float SPECULAR_LIGHT = 0.5;

in vec3 fragment_position;
in vec3 fragment_normal;
in vec3 fragment_colour;
in vec2 fragment_uv;

// view properties per scene (used to get camera position)
uniform mat4 viewTransform = mat4(1);

// texturing properties per-object
uniform sampler2D textureSampler;
uniform bool useTexture = false;

// colour of the object - for recolouring
uniform vec3 colour = vec3(1, 1, 1);

// lighting properties per-object/scene
uniform float ambientLightingIntensity = 0.3;
uniform float diffuseLightingIntensity = 0.7;
uniform float specularLightingIntensity = 0.4;
uniform float specularLightingPower = 5;

// lighting properties per scene
uniform vec3 lightColour = vec3(1, 1, 1);
uniform vec3 lightPosition = vec3(0, 10000, 0);

void main(void) {
    vec3 cameraPosition = (inverse(viewTransform)[3]).xyz;

    float diffuseFactor = max(0, dot(
        fragment_normal,
        normalize(lightPosition - fragment_position)
    ));

    float specularFactor = max(0, dot(
        normalize(reflect(fragment_position - cameraPosition, fragment_normal)),
        normalize(lightPosition - fragment_position)
    ));

    vec3 ambientColour  = ambientLightingIntensity * lightColour * colour * fragment_colour;
    vec3 diffuseColour  = diffuseLightingIntensity * lightColour * colour * fragment_colour * diffuseFactor;
    vec3 specularColour = specularLightingIntensity * lightColour * pow(specularFactor, specularLightingPower);

    // mix(u, v, t)

    gl_FragColor = vec4(ambientColour + diffuseColour + specularColour, 1.0);

    if (useTexture) {
        gl_FragColor *= texture(textureSampler, fragment_uv);
    }
}
