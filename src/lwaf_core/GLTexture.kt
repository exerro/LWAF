package lwaf_core

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class GLTexture internal constructor(val textureID: Int, val width: Int, val height: Int, private val resID: String): Resource, GLResource {
    override fun getResourceID(): String = resID

    override fun free() {
        destroy()
    }

    fun bind() {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureID)
    }

    fun unbind() {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun destroy() {
        Logging.log("resource.texture.destroy") { "Destroying texture (ID: $textureID)" }
        glDeleteTextures(textureID)
    }
}

@JvmOverloads
fun createEmptyTexture(width: Int, height: Int, internalFormat: Int = GL_RGBA, format: Int = GL_RGBA, type: Int = GL_UNSIGNED_BYTE): GLTexture {
    val textureID = glGenTextures()

    Logging.log("resource.texture.create") { "Creating ($width x $height) empty texture (ID: $textureID)" }

    glBindTexture(GL_TEXTURE_2D, textureID)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, null as ByteBuffer?)
    glBindTexture(GL_TEXTURE_2D, 0)

    return GLTexture(textureID, width, height, "texture_$textureID")
}

fun loadTextureFromInputStream(stream: InputStream, resID: String): GLTexture {
    val byteArray = stream.readAllBytes()
    val byteBuffer = BufferUtils.createByteBuffer(byteArray.size)
    byteBuffer.put(byteArray)
    byteBuffer.flip()
    return loadTexture(byteBuffer, resID)
}

fun loadTexture(filePath: String): GLTexture {
    val file = RandomAccessFile(File(filePath), "r")
    return loadTexture(file.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.channel.size()), filePath)
}

private fun loadTexture(buffer: ByteBuffer, resID: String): GLTexture {
    val textureID: Int = glGenTextures()
    val width: Int
    val height: Int
    val data: ByteBuffer
    val w = BufferUtils.createIntBuffer(1)
    val h = BufferUtils.createIntBuffer(1)
    val channels = BufferUtils.createIntBuffer(1)

    Logging.log("resource.texture.load") { "Loading texture '$resID' (ID: $textureID)" }

    data = STBImage.stbi_load_from_memory(buffer, w, h, channels, STBImage.STBI_rgb_alpha)
            ?: throw RuntimeException(STBImage.stbi_failure_reason())
    width = w.get()
    height = h.get()

    glBindTexture(GL_TEXTURE_2D, textureID)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data)
    glBindTexture(GL_TEXTURE_2D, 0)

    STBImage.stbi_image_free(data)

    return GLTexture(textureID, width, height, resID)
}
