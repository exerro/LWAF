package lwaf_3D

import lwaf_core.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13

import java.nio.file.Paths

import org.lwjgl.opengl.GL30.*

class GBuffer(width: Int, height: Int) {
    private val fbo: GLFramebuffer = GLFramebuffer(width, height)
    val colourTexture: GLTexture
    val positionTexture: GLTexture
    val normalTexture: GLTexture
    val lightingTexture: GLTexture

    val depthTexture: GLTexture
        get() = fbo.depthTexture

    init {
        colourTexture = fbo.attachTexture(createEmptyTexture(width, height), GL_COLOR_ATTACHMENT0)
        positionTexture = fbo.attachTexture(createEmptyTexture(width, height, GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT), GL_COLOR_ATTACHMENT1)
        normalTexture = fbo.attachTexture(createEmptyTexture(width, height, GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT), GL_COLOR_ATTACHMENT2)
        lightingTexture = fbo.attachTexture(createEmptyTexture(width, height, GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT), GL_COLOR_ATTACHMENT3)

        fbo.setDrawBuffers(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3)
    }

    fun bind() {
        fbo.bind()
    }

    fun unbind() {
        fbo.unbind()
    }

    fun bindReading() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTexture.textureID)
        GL13.glActiveTexture(GL13.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, positionTexture.textureID)
        GL13.glActiveTexture(GL13.GL_TEXTURE2)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalTexture.textureID)
        GL13.glActiveTexture(GL13.GL_TEXTURE3)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightingTexture.textureID)
    }

    fun unbindReading() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE1)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE2)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE3)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
    }

    fun destroy() {
        fbo.destroy()
        colourTexture.destroy()
        positionTexture.destroy()
        normalTexture.destroy()
        lightingTexture.destroy()
    }

    companion object {
        var FRAGMENT_SHADER_PATH = "lwaf_res/shader/draw-to-gbuffer.glsl"

        fun loadShader(vertexShader: String, instanced: Boolean): GLShaderProgram {
            return loadShaderProgramFiles(
                    Paths.get(vertexShader).toString(),
                    FRAGMENT_SHADER_PATH,
                    instanced
            )
        }
    }

}
