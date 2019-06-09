package lwaf_core

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30.*

class GLFramebuffer(val width: Int, val height: Int): GLResource {
    private val frameBufferID: Int = glGenFramebuffers()
    val depthTexture: GLTexture = attachTexture(
            createEmptyTexture(width, height, GL14.GL_DEPTH_COMPONENT32, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT),
            GL_DEPTH_ATTACHMENT
    )

    init {
        Logging.log("framebuffer.create") { "Creating ($width x $height) framebuffer" }
    }

    fun attachTexture(texture: GLTexture, attachment: Int): GLTexture {
        bind()
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, texture.textureID, 0)
        unbind()

        return texture
    }

    fun setDrawBuffers(vararg attachments: Int) {
        bind()
        GL20.glDrawBuffers(attachments)
        unbind()
    }

    fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID)
    }

    fun unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun destroy() {
        glDeleteFramebuffers(frameBufferID)
        depthTexture.destroy()
    }
}
