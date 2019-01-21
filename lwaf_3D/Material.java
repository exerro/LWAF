package lwaf_3D;

import lwaf.ShaderLoader;
import lwaf.Texture;
import lwaf.vec3f;

public class Material {
    private final float diffuseLightingIntensity, specularLightingIntensity;
    private final int specularLightingPower;
    private final Texture texture;
    private final vec3f colour;

    public static final float DIFFUSE_LIGHTING_INTENSITY = 0.7f;
    public static final float SPECULAR_LIGHTING_INTENSITY = 0.4f;
    public static final int SPECULAR_LIGHTING_POWER = 5;

    private Material(float diffuseLightingIntensity, float specularLightingIntensity, int specularLightingPower, Texture texture, vec3f colour) {
        this.diffuseLightingIntensity = diffuseLightingIntensity;
        this.specularLightingIntensity = specularLightingIntensity;
        this.specularLightingPower = specularLightingPower;
        this.texture = texture;
        this.colour = colour;
    }

    public Material(Texture texture, vec3f colour) {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, texture, colour);
    }

    public Material(Texture texture) {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, texture, vec3f.one);
    }

    public Material(vec3f colour) {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, null, colour);
    }

    public Material() {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER, null, vec3f.one);
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

    public Texture getTexture() {
        return texture;
    }

    public Material setTexture(Texture texture) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public boolean hasTexture() {
        return texture != null;
    }

    public vec3f getColour() {
        return colour;
    }

    public Material setColour(vec3f colour) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour);
    }

    public Material setColour(float r, float g, float b) {
        return new Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, new vec3f(r, g, b));
    }

    public void setShaderUniforms(ShaderLoader.Program shader) {
        shader.setUniform("diffuseLightingIntensity", getDiffuseLightingIntensity());
        shader.setUniform("specularLightingIntensity", getSpecularLightingIntensity());
        shader.setUniform("specularLightingPower", getSpecularLightingPower());
        shader.setUniform("colour", getColour());
    }
}
