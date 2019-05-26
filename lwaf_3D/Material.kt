package lwaf_3D

import lwaf_core.GLShaderProgram
import lwaf_core.GLTexture
import lwaf_core.vec3

data class Material(
        val diffuseLightingIntensity: Float = DIFFUSE_LIGHTING_INTENSITY,
        val specularLightingIntensity: Float = SPECULAR_LIGHTING_INTENSITY,
        val specularLightingPower: Int = SPECULAR_LIGHTING_POWER,
        val texture: GLTexture? = null,
        val colour: vec3 = vec3(1f)
) {
    fun setDiffuseLightingIntensity(diffuseLightingIntensity: Float): Material {
        return Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour)
    }

    fun setSpecularLightingIntensity(specularLightingIntensity: Float): Material {
        return Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour)
    }

    fun setSpecularLightingPower(specularLightingPower: Int): Material {
        return Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour)
    }

    fun setTexture(texture: GLTexture): Material {
        return Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour)
    }

    fun hasTexture(): Boolean {
        return texture != null
    }

    fun setColour(colour: vec3): Material {
        return Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, colour)
    }

    fun setColour(r: Float, g: Float, b: Float): Material {
        return Material(diffuseLightingIntensity, specularLightingIntensity, specularLightingPower, texture, vec3(r, g, b))
    }

    fun setShaderUniforms(shader: GLShaderProgram) {
        shader.setUniform("diffuseLightingIntensity", diffuseLightingIntensity)
        shader.setUniform("specularLightingIntensity", specularLightingIntensity)
        shader.setUniform("specularLightingPower", specularLightingPower)
        shader.setUniform("colour", colour)
    }

    companion object {
        const val DIFFUSE_LIGHTING_INTENSITY = 1f
        const val SPECULAR_LIGHTING_INTENSITY = 0.4f
        const val SPECULAR_LIGHTING_POWER = 5
    }
}
