package lwaf_3D

import lwaf_core.*

private val shaders: MutableMap<String, GLShaderProgram> = HashMap()

interface Light {
    val colour: vec3
    val intensity: Float
    val shader: GLShaderProgram

    fun render(shader: GLShaderProgram, context: DrawContext3D)
}

data class AmbientLight(
        override val intensity: Float,
        override val colour: vec3
) : Light {
    override val shader = getLightingShader("lighting.ambient") {
        loadShaderProgramFiles("lwaf_res/shader/pass-through.vertex-3D.glsl", "lwaf_res/shader/lighting/ambient.fragment-3D.glsl")
    }

    override fun render(shader: GLShaderProgram, context: DrawContext3D) {
        shader.setUniform("lightColour", colour)
        shader.setUniform("lightIntensity", intensity)
        shader.setUniform("transform", mat4_identity)
        context.drawIndexedVAO(screen_quad)
    }
}

data class DirectionalLight(
        val direction: vec3,
        override val intensity: Float,
        override val colour: vec3
): Light {
    override val shader = getLightingShader("lighting.directional") {
        loadShaderProgramFiles("lwaf_res/shader/pass-through.vertex-3D.glsl", "lwaf_res/shader/lighting/directional.fragment-3D.glsl")
    }

    override fun render(shader: GLShaderProgram, context: DrawContext3D) {
        shader.setUniform("lightColour", colour)
        shader.setUniform("lightIntensity", intensity)
        shader.setUniform("lightDirection", direction)
        shader.setUniform("transform", mat4_identity)
        context.drawIndexedVAO(screen_quad)
    }
}

data class PointLight(
        val position: vec3,
        override val intensity: Float,
        val attenuation: vec3 = ATTENUATION,
        override val colour: vec3
): Light {
    override val shader = getLightingShader("lighting.point") {
        loadShaderProgramFiles("lwaf_res/shader/pass-through.vertex-3D.glsl", "lwaf_res/shader/lighting/point.fragment-3D.glsl")
    }

    override fun render(shader: GLShaderProgram, context: DrawContext3D) {
        shader.setUniform("lightColour", colour)
        shader.setUniform("lightIntensity", intensity)
        shader.setUniform("lightPosition", position)
        shader.setUniform("lightAttenuation", attenuation)
        shader.setUniform("transform", mat4_identity)

        context.drawIndexedVAO(screen_quad)
    }

    companion object {
        val ATTENUATION = vec3(1f, 0.09f, 0.032f)
    }
}

data class SpotLight(
        val position: vec3,
        val direction: vec3,
        val spread: vec2,
        override val intensity: Float,
        val attenuation: vec3 = PointLight.ATTENUATION,
        override val colour: vec3
): Light {
    override val shader = getLightingShader("lighting.spot") {
        loadShaderProgramFiles("lwaf_res/shader/pass-through.vertex-3D.glsl", "lwaf_res/shader/lighting/spot.fragment-3D.glsl")
    }

    override fun render(shader: GLShaderProgram, context: DrawContext3D) {
        shader.setUniform("lightColour", colour)
        shader.setUniform("lightDirection", direction)
        shader.setUniform("lightCutoff", spread)
        shader.setUniform("lightIntensity", intensity)
        shader.setUniform("lightPosition", position)
        shader.setUniform("lightAttenuation", attenuation)
        shader.setUniform("transform", mat4_identity)

        context.drawIndexedVAO(screen_quad)
    }
}

fun getLightingShader(id: String, getShader: () -> GLShaderProgram): GLShaderProgram {
    if (shaders.containsKey(id))
        return shaders[id]!!

    val shader = getShader()

    shader.setUniform("colourMap", 0)
    shader.setUniform("positionMap", 1)
    shader.setUniform("normalMap", 2)
    shader.setUniform("lightingMap", 3)

    shaders[id] = shader

    return shader
}

fun getLightingAttenuation(distance: Float, halfBrightnessDistance: Float): vec3 {
    // brightness factor = 1 / (Lx + d * (Ly + d * Lz))

    // at distance 1 , brightness = 1          : 1 / (Lx + 1  * (Ly + 1  * Lz)) = 1
    // at distance Dh, brightness = 1/2        : 1 / (Lx + Dh * (Ly + Dh * Lz)) = 1/2
    // at distance D , brightness = 1/256 ~= 0 : 1 / (Lx + D  * (Ly + D  * Lz)) = 1/256

    // Lx +      Ly +        Lz = 1
    // Lx + Dh * Ly + Dh^2 * Lz = 2
    // Lx + D  * Ly + D^2  * Lz = 256

    val lZ = (255 - (distance - 1) / (halfBrightnessDistance - 1)) /
            ((distance-1) * (distance - halfBrightnessDistance))
    val lY = (1 - (halfBrightnessDistance * halfBrightnessDistance - 1) * lZ) /
            (halfBrightnessDistance - 1)
    val lX = 1 - lY - lZ

    return vec3(lX, lY, lZ)
}


fun getLightingAttenuation(distance: Float): vec3 {
    val lZ = 255 / (distance * distance)
    val lY = 0f
    val lX = 1f

    return vec3(lX, lY, lZ)
}

