package lwaf_3D;

import lwaf_core.GLShaderProgram;
import lwaf_core.GLTexture;
import lwaf_core.vec3;

public class Material {
    private final float diffuseLightingIntensity, specularLightingIntensity;
    private final int specularLightingPower;
    private final GLTexture texture;
    private final vec3 colour;

    public static final float DIFFUSE_LIGHTING_INTENSITY = 0.7f;
    public static final float SPECULAR_LIGHTING_INTENSITY = 0.4f;
    public static final int SPECULAR_LIGHTING_POWER = 5;

    private Material(float diffuseLightingIntensity, float specularLightingIntensity, int specularLightingPower, GLTexture texture, vec3 colour) {
        this.diffuseLightingIntensity = diffuseLightingIntensity;
        this.specularLightingIntensity = specularLightingIntensity;
        this.specularLightingPower = specularLightingPower;
        this.texture = texture;
        this.colour = colour;
    }

    public Material(GLTexture texture, vec3 colour) {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, texture, colour);
    }

    public Material(GLTexture texture) {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, texture, new vec3(1, 1, 1));
    }

    public Material(vec3 colour) {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, null, colour);
    }

    public Material() {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, null, new vec3(1, 1, 1));
    }

    public float getDiffuseLightingIntensity() {
        return diffuseLightingIntensity;
    }

    public Material setDiffuseLightingIntensity(float diffuseLightingIntensity) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public float getSpecularLightingIntensity() {
        return specularLightingIntensity;
    }

    public Material setSpecularLightingIntensity(float specularLightingIntensity) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public int getSpecularLightingPower() {
        return specularLightingPower;
    }

    public Material setSpecularLightingPower(int specularLightingPower) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public GLTexture getTexture() {
        return texture;
    }

    public Material setTexture(GLTexture texture) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public boolean hasTexture() {
        return texture != null;
    }

    public vec3 getColour() {
        return colour;
    }

    public Material setColour(vec3 colour) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public Material setColour(float r, float g, float b) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, new vec3(r, g, b));
    }

    public void setShaderUniforms(GLShaderProgram shader) {
        shader.setUniform("diffuseLightingIntensity", getDiffuseLightingIntensity());
        shader.setUniform("specularLightingIntensity", getSpecularLightingIntensity());
        shader.setUniform("specularLightingPower", getSpecularLightingPower());
        shader.setUniform("colour", getColour());
    }
}
