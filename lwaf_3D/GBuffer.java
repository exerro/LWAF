package lwaf_3D;

import lwaf.FBO;
import lwaf.Texture;

import static org.lwjgl.opengl.GL30.*;

public class GBuffer {
    private final FBO fbo;
    private final Texture colourTexture, positionTexture, normalTexture;

    public GBuffer(int width, int height) {
        fbo = new FBO(width, height);

        colourTexture = fbo.attachColorAttachment(GL_COLOR_ATTACHMENT0);
        positionTexture = fbo.attachColorAttachment(GL_COLOR_ATTACHMENT1);
        normalTexture = fbo.attachColorAttachment(GL_COLOR_ATTACHMENT2);
    }

    public void bind() {
        fbo.bind();
    }

    public void unbind() {
        fbo.unbind();
    }

    public Texture getColourTexture() {
        return colourTexture;
    }

    public void destroy() {
        fbo.destroy();
        colourTexture.destroy();
        positionTexture.destroy();
        normalTexture.destroy();
    }

}
