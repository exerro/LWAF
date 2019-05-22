package lwaf_3D

import lwaf_core.*
import lwaf_primitive.IcoSphereVAO

import java.util.HashMap
import java.util.function.Supplier

import org.lwjgl.opengl.GL11.*

abstract class Light protected constructor(val intensity: Float, val colour: vec3) {

    protected val shader: GLShaderProgram
        get() = getShader(this.javaClass)

    abstract fun getTransformationMatrix(viewMatrix: mat4, projectionMatrix: mat4): mat4
    abstract fun render(buffer: GBuffer)

    open fun setUniforms(viewMatrix: mat4, projectionMatrix: mat4) {
        shader.setUniform("transform", getTransformationMatrix(viewMatrix, projectionMatrix))
        shader.setUniform("viewTransform", viewMatrix)
        shader.setUniform("projectionTransform", projectionMatrix)
    }

    class AmbientLight @JvmOverloads constructor(intensity: Float, colour: vec3 = vec3(1f, 1f, 1f)) : Light(intensity, colour) {

        init {

            registerShader(AmbientLight::class.java, {
                loadShaderProgramFiles(
                        "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                        "lwaf_3D/shader/ambient.fragment-3D.glsl",
                        false
                )
            })
        }

        override fun getTransformationMatrix(viewMatrix: mat4, projectionMatrix: mat4): mat4 {
            return mat4_identity
        }

        override fun setUniforms(viewMatrix: mat4, projectionMatrix: mat4) {
            super.setUniforms(viewMatrix, projectionMatrix)

            shader.setUniform("lightIntensity", intensity)
            shader.setUniform("lightColour", colour)
        }

        override fun render(buffer: GBuffer) {
            buffer.context.drawIndexedVAO(screen_quad)
        }
    }

    class DirectionalLight @JvmOverloads constructor(intensity: Float, direction: vec3, colour: vec3 = vec3(1f, 1f, 1f)) : Light(intensity, colour) {
        val direction: vec3

        init {

            this.direction = direction.normalise()

            registerShader(DirectionalLight::class.java, {
                loadShaderProgramFiles(
                        "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                        "lwaf_3D/shader/directional.fragment-3D.glsl",
                        false
                )
            })
        }

        override fun getTransformationMatrix(viewMatrix: mat4, projectionMatrix: mat4): mat4 {
            return mat4_identity
        }

        override fun setUniforms(viewMatrix: mat4, projectionMatrix: mat4) {
            super.setUniforms(viewMatrix, projectionMatrix)

            shader.setUniform("lightIntensity", intensity)
            shader.setUniform("lightDirection", direction)
            shader.setUniform("lightColour", colour)
        }

        override fun render(buffer: GBuffer) {
            buffer.context.drawIndexedVAO(screen_quad)
        }
    }

    open class PointLight @JvmOverloads constructor(intensity: Float, val position: vec3, val attenuation: vec3 = ATTENUATION, colour: vec3 = vec3(1f, 1f, 1f)) : Light(intensity, colour) {

        init {

            registerShader(PointLight::class.java, {
                loadShaderProgramFiles(
                        "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                        "lwaf_3D/shader/point.fragment-3D.glsl",
                        false
                )
            })

            if (vao == null) {
                vao = IcoSphereVAO(2)
            }
        }

        override fun getTransformationMatrix(viewMatrix: mat4, projectionMatrix: mat4): mat4 {
            val Lx = attenuation.x
            val Ly = attenuation.y
            val Lz = attenuation.z

            val radius: Float

            if (Lz != 0f)
                radius = (-Ly + Math.sqrt((Ly * Ly - 4f * Lz * (Lx - 256 * intensity)).toDouble()).toFloat()) / (2 * Lz)
            else
                radius = (256 * intensity - Lx) / Ly

            return projectionMatrix *
                    viewMatrix *
                    mat4_translate(-position) *
                    mat3_scale(radius * 1.1f).mat4()
        }

        override fun setUniforms(viewMatrix: mat4, projectionMatrix: mat4) {
            super.setUniforms(viewMatrix, projectionMatrix)

            shader.setUniform("lightIntensity", intensity)
            shader.setUniform("lightPosition", position)
            shader.setUniform("lightAttenuation", attenuation)
            shader.setUniform("lightColour", colour)
        }

        override fun render(buffer: GBuffer) {
            glEnable(GL_CULL_FACE)
            glFrontFace(GL_CW)
            buffer.context.drawIndexedVAO(vao!!)
            glFrontFace(GL_CCW)
            glDisable(GL_CULL_FACE)
        }

        companion object {

            private var vao: IcoSphereVAO? = null
            val ATTENUATION = vec3(1f, 0.09f, 0.032f)
        }
    }

    class SpotLight @JvmOverloads constructor(intensity: Float, position: vec3, val direction: vec3, attenuation: vec3 = ATTENUATION, val cutoff: vec2 = CUTOFF, colour: vec3 = vec3(1f, 1f, 1f)) : PointLight(intensity, position, attenuation, colour) {

        init {

            registerShader(SpotLight::class.java, {
                loadShaderProgramFiles(
                        "lwaf_3D/shader/pass-through.vertex-3D.glsl",
                        "lwaf_3D/shader/spot.fragment-3D.glsl",
                        false
                )
            })
        }

        constructor(intensity: Float, position: vec3, direction: vec3, attenuation: vec3, cutoff: Float, colour: vec3) : this(intensity, position, direction, attenuation, vec2(cutoff * 0.8f, cutoff), colour) {}

        override fun getTransformationMatrix(viewMatrix: mat4, projectionMatrix: mat4): mat4 {
            return mat4_identity
        }

        override fun setUniforms(viewMatrix: mat4, projectionMatrix: mat4) {
            super.setUniforms(viewMatrix, projectionMatrix)

            shader.setUniform("lightDirection", direction)
            shader.setUniform("lightCutoff", cutoff)
        }

        override fun render(buffer: GBuffer) {
            buffer.context.drawIndexedVAO(screen_quad)
        }

        companion object {

            val CUTOFF = vec2(0f, 0f)

            fun lightSpread(range: Float): Float {
                return Math.PI.toFloat() * range / 2
            }
        }
    }

    companion object {

        private val shaders = HashMap<Class<out Light>, GLShaderProgram>()

        fun registerShader(type: Class<out Light>, generator: () -> GLShaderProgram) {
            shaders.computeIfAbsent(type) { _ ->
                val shader = generator()

                shader.setUniform("colourMap", 0)
                shader.setUniform("positionMap", 1)
                shader.setUniform("normalMap", 2)
                shader.setUniform("lightingMap", 3)

                shader
            }
        }

        fun getShader(lightType: Class<out Light>): GLShaderProgram {
            return shaders[lightType]!!
        }

        // half brightness distance is the distance where the brightness is half its brightness at distance 1
        // this is kind of a terrible function
        fun attenuation(distance: Float, halfBrightnessDistance: Float): vec3 {
            // brightness factor = 1 / (Lx + d * (Ly + d * Lz))

            // at distance 1 , brightness = 1          : 1 / (Lx + 1  * (Ly + 1  * Lz)) = 1
            // at distance Dh, brightness = 1/2        : 1 / (Lx + Dh * (Ly + Dh * Lz)) = 1/2
            // at distance D , brightness = 1/256 ~= 0 : 1 / (Lx + D  * (Ly + D  * Lz)) = 1/256

            // Lx +      Ly +        Lz = 1
            // Lx + Dh * Ly + Dh^2 * Lz = 2
            // Lx + D  * Ly + D^2  * Lz = 256

            val Lz = (255 - (distance - 1) / (halfBrightnessDistance - 1)) / ((distance - 1) * (distance - halfBrightnessDistance))
            val Ly = (1 - (halfBrightnessDistance * halfBrightnessDistance - 1) * Lz) / (halfBrightnessDistance - 1)
            val Lx = 1f - Ly - Lz

            return vec3(Lx, Ly, Lz)
        }

        fun attenuation(distance: Float): vec3 {
            val Lz = 255 / (distance * distance)
            val Ly = 0
            val Lx = 1

            return vec3(Lx.toFloat(), Ly.toFloat(), Lz)
        }
    }

}
