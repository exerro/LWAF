#version 400 core

in vec2 uv;

uniform sampler2D colourMap;
uniform sampler2D positionMap;
uniform sampler2D normalMap;
uniform sampler2D lightingMap;

uniform float lightIntensity;
uniform vec3 lightDirection;
uniform vec3 lightColour;

uniform mat4 viewTransform;

void main(void) {
   	vec3 cameraPosition = (inverse(viewTransform)[3]).xyz;
   	vec4 colour = texture(colourMap, uv);
   	vec4 normal = texture(normalMap, uv);
   	vec4 position = texture(positionMap, uv);
   	vec4 lighting = texture(lightingMap, uv);

   	float diffuseLightingIntensity = lighting.x;
   	float specularLightingIntensity = lighting.y;
   	float specularLightingPower = lighting.z;

    float diffuseFactor = max(0, dot(
        normal.xyz,
        -normalize(lightDirection)
    ));

    float specularFactor = max(0, -dot(
        normalize(reflect(position.xyz - cameraPosition, normal.xyz)),
        lightDirection
    ));

    vec4 diffuseColour  = diffuseLightingIntensity * vec4(lightColour, 1.0) * colour * diffuseFactor;
    vec4 specularColour = specularLightingIntensity * vec4(lightColour, 1.0) * pow(specularFactor, specularLightingPower);

    gl_FragColor = diffuseColour + specularColour;
}
