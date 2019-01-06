#version 400 core

in vec2 uv;

uniform sampler2D colourMap;
uniform sampler2D positionMap;
uniform sampler2D normalMap;
uniform sampler2D lightingMap;

uniform float lightIntensity;
uniform vec3 lightColour;

void main(void) {
   	gl_FragColor = lightIntensity * vec4(lightColour, 1) * texture(colourMap, uv);
}
