#version 400 core

in vec2 uv;

uniform sampler2D colourMap;
uniform sampler2D positionMap;
uniform sampler2D normalMap;
uniform sampler2D lightingMap;

uniform float lightIntensity;
uniform vec3 lightPosition;
uniform vec3 lightDirection;
uniform vec3 lightAttenuation;
uniform vec2 lightCutoff;
uniform vec3 lightColour;

uniform mat4 viewTransform;

void main(void) {
   	vec3 cameraPosition = (inverse(viewTransform)[3]).xyz;
   	vec4 colour = texture(colourMap, uv);
   	vec4 normal = texture(normalMap, uv);
   	vec4 position = texture(positionMap, uv);
   	vec4 lighting = texture(lightingMap, uv);
   	vec3 lightDirectionToFragment = normalize(position.xyz - lightPosition);

   	float diffuseLightingIntensity = lighting.x;
   	float specularLightingIntensity = lighting.y;
   	float specularLightingPower = lighting.z;

    float diffuseFactor = max(0, dot(
        -normal.xyz,
        lightDirectionToFragment
    ));

    float specularFactor = max(0, -dot(
        normalize(reflect(position.xyz - cameraPosition, normal.xyz)),
        lightDirectionToFragment
    ));

    float distance = length(position.xyz - lightPosition);
    float attenuationFactor = 1 / (lightAttenuation.x + distance * (lightAttenuation.y + distance * lightAttenuation.z));
    float theta = acos(dot(lightDirectionToFragment, normalize(lightDirection)));
    float spotFactor = clamp(1 -
        (theta - lightCutoff.y)
      / (lightCutoff.x - lightCutoff.y)
      , 0, 1);

    vec4 diffuseColour  = spotFactor * attenuationFactor * lightIntensity * diffuseLightingIntensity  * vec4(lightColour, 1.0) * colour * diffuseFactor;
    vec4 specularColour = spotFactor * attenuationFactor * lightIntensity * specularLightingIntensity * vec4(lightColour, 1.0) * pow(specularFactor, specularLightingPower);

    gl_FragColor = diffuseColour + specularColour;
}
