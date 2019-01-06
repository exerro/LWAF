package lwaf;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL30.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class FBO {

    private final int width, height;
    private final int frameBufferID;
    private final Texture depthTexture;

    public FBO(int width, int height) {
        this.width = width;
        this.height = height;

        frameBufferID = glGenFramebuffers();
        depthTexture = attachTexture(
                Texture.create(width, height, GL_DEPTH_COMPONENT32, GL_DEPTH_COMPONENT, GL_FLOAT),
                GL_DEPTH_ATTACHMENT
        );
    }

    public Texture attachTexture(Texture texture, int attachment) {
        bind();
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, texture.getTextureID(), 0);
        unbind();

        return texture;
    }

    public void setDrawBuffers(int... attachments) {
        bind();
        glDrawBuffers(attachments);
        unbind();
    }

    public Texture getDepthTexture() {
        return depthTexture;
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);
    }

    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void destroy() {
        glDeleteFramebuffers(frameBufferID);
        depthTexture.destroy();
    }

}
