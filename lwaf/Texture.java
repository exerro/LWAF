package lwaf;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Texture {

    private final int textureID;
    private final int width, height;

    private Texture(int textureID, int width, int height) {
        this.textureID = textureID;
        this.width = width;
        this.height = height;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getTextureID() {
        return textureID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void destroy() {
        glDeleteTextures(textureID);
    }

    public static Texture load(String filePath) {
        int textureID, width, height;

        ByteBuffer data;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            data = STBImage.stbi_load(filePath, w, h, channels, STBImage.STBI_rgb_alpha);

            if (data == null)
                throw new RuntimeException(STBImage.stbi_failure_reason());

            width = w.get();
            height = h.get();
        }

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glBindTexture(GL_TEXTURE_2D, 0);

        STBImage.stbi_image_free(data);

        return new Texture(textureID, width, height);
    }

    public static Texture safeLoad(String filePath) {
        return load(filePath);
    }

    public static Texture create(int width, int height) {
        int textureID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_2D, 0);

        return new Texture(textureID, width, height);
    }

}
