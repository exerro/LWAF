package lwaf_3D;

import lwaf_core.GLShaderProgram;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ObjectLighting {
    private float diffuseLightingIntensity, specularLightingIntensity;
    private int specularLightingPower;

    public static final float DIFFUSE_LIGHTING_INTENSITY = 0.7f;
    public static final float SPECULAR_LIGHTING_INTENSITY = 0.4f;
    public static final int SPECULAR_LIGHTING_POWER = 5;

    public ObjectLighting(float diffuseLightingIntensity, float specularLightingIntensity, int specularLightingPower) {
        this.diffuseLightingIntensity = diffuseLightingIntensity;
        this.specularLightingIntensity = specularLightingIntensity;
        this.specularLightingPower = specularLightingPower;
    }

    public ObjectLighting(float diffuseLightingIntensity, float specularLightingIntensity) {
        this(diffuseLightingIntensity, specularLightingIntensity, SPECULAR_LIGHTING_POWER);
    }

    public ObjectLighting(float specularLightingIntensity) {
        this(DIFFUSE_LIGHTING_INTENSITY, specularLightingIntensity, SPECULAR_LIGHTING_POWER);
    }

    public ObjectLighting() {
        this(DIFFUSE_LIGHTING_INTENSITY, SPECULAR_LIGHTING_INTENSITY, SPECULAR_LIGHTING_POWER);
    }

    public float getDiffuseLightingIntensity() {
        return diffuseLightingIntensity;
    }

    public void setDiffuseLightingIntensity(float diffuseLightingIntensity) {
        this.diffuseLightingIntensity = diffuseLightingIntensity;
    }

    public float getSpecularLightingIntensity() {
        return specularLightingIntensity;
    }

    public void setSpecularLightingIntensity(float specularLightingIntensity) {
        this.specularLightingIntensity = specularLightingIntensity;
    }

    public int getSpecularLightingPower() {
        return specularLightingPower;
    }

    public void setSpecularLightingPower(int specularLightingPower) {
        this.specularLightingPower = specularLightingPower;
    }

    public void setShaderUniforms(GLShaderProgram shader) {
        shader.setUniform("diffuseLightingIntensity", getDiffuseLightingIntensity());
        shader.setUniform("specularLightingIntensity", getSpecularLightingIntensity());
        shader.setUniform("specularLightingPower", getSpecularLightingPower());
    }
}