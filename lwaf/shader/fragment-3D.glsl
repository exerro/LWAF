#version 400 core

const float SPECULAR_POWER = 5;
const float SPECULAR_LIGHT = 0.5;

in vec3 fragment_position;
in vec3 fragment_normal;
in vec3 fragment_colour;
in vec2 fragment_uv;

uniform mat4 viewTransform = mat4(1);

uniform sampler2D textureSampler;
uniform bool useTexture = false;

uniform vec3 colour = vec3(1, 1, 1);

uniform float lightMinimum = 0.3;
uniform vec3 lightColour = vec3(1, 1, 1);
uniform vec3 lightPosition = vec3(0, 10000, 0);

void main(void) {
    vec3 cameraPosition = (inverse(viewTransform)[3]).xyz;

    float diffuseScalar = max(0, dot(
        fragment_normal,
        normalize(lightPosition - fragment_position)
    ));

    float specularScalar = max(0, dot(
        normalize(reflect(fragment_position - cameraPosition, fragment_normal)),
        normalize(lightPosition - fragment_position)
    ));

    float diffuseContrib = mix(lightMinimum, 1, diffuseScalar);
    float specularContrib = mix(0, SPECULAR_LIGHT, pow(specularScalar, SPECULAR_POWER));

    vec3 naturalColour = colour * fragment_colour;
    vec3 litColour = (diffuseContrib + specularContrib) * lightColour * naturalColour;

    // mix(u, v, t)

    gl_FragColor = vec4(litColour, 1.0);

    if (useTexture) {
        gl_FragColor *= texture(textureSampler, fragment_uv);
    }
}
