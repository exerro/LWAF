package lwaf_core

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class GLTexture internal constructor(val textureID: Int, val width: Int, val height: Int): GLResource {
    fun bind() {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureID)
    }

    fun unbind() {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun destroy() {
        glDeleteTextures(textureID)
    }
}

@JvmOverloads
fun createEmptyTexture(width: Int, height: Int, internalFormat: Int = GL_RGBA, format: Int = GL_RGBA, type: Int = GL_UNSIGNED_BYTE): GLTexture {
    val textureID = glGenTextures()

    Logging.log("texture.create") { "Creating ($width x $height) empty texture" }

    glBindTexture(GL_TEXTURE_2D, textureID)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null as ByteBuffer?)
    glBindTexture(GL_TEXTURE_2D, 0)

    return GLTexture(textureID, width, height)
}

fun loadTexture(filePath: String): GLTexture {
    val textureID: Int = glGenTextures()
    var width = 0
    var height = 0
    var data: ByteBuffer? = null

    Logging.log("texture.load") { "Loading texture '$filePath'" }

    MemoryStack.stackPush().use { stack ->
        val w = stack.mallocInt(1)
        val h = stack.mallocInt(1)
        val channels = stack.mallocInt(1)

        data = STBImage.stbi_load(filePath, w, h, channels, STBImage.STBI_rgb_alpha)

        if (data == null)
            throw RuntimeException(STBImage.stbi_failure_reason())

        width = w.get()
        height = h.get()
    }

    glBindTexture(GL_TEXTURE_2D, textureID)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data)
    glBindTexture(GL_TEXTURE_2D, 0)

    STBImage.stbi_image_free(data!!)

    return GLTexture(textureID, width, height)
}
