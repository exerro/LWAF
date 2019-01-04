package lwaf;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL30.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class FBO {

    private final int width, height;
    private final int frameBufferID;
    private final int depthTextureID;
    private final int depthBufferID;
    private final Texture texture;

    public FBO(int width, int height) {
        this.width = width;
        this.height = height;

        frameBufferID = glGenFramebuffers();
        texture = Texture.create(width, height);
        depthTextureID = glGenTextures();
        depthBufferID = glGenRenderbuffers();

        // bind framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);

        // bind texture
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.getTextureID(), 0);
//
//        // generate and bind depth texture
//        glBindTexture(GL_TEXTURE_2D, depthTextureID);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTextureID, 0);
//        glBindTexture(GL_TEXTURE_2D, 0);

        // bind depth buffer
        glBindRenderbuffer(GL_RENDERBUFFER, depthBufferID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBufferID);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);
        Draw.viewport(new vec2f(texture.getWidth(), texture.getHeight()));
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        Draw.viewport();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture getTexture() {
        return texture;
    }

    public void destroy() {
        glDeleteFramebuffers(frameBufferID);
        glDeleteTextures(depthTextureID);
        glDeleteRenderbuffers(depthBufferID);
        texture.destroy();
    }

}
