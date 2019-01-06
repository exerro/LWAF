package lwaf_3D;

import lwaf.FBO;
import lwaf.Texture;

import static org.lwjgl.opengl.GL30.*;

public class GBuffer {
    private final FBO fbo;
    private final Texture colourTexture, positionTexture, normalTexture, lightingTexture;

    public GBuffer(int width, int height) {
        fbo = new FBO(width, height);

        colourTexture = fbo.attachTexture(Texture.create(width, height), GL_COLOR_ATTACHMENT0);
        positionTexture = fbo.attachTexture(Texture.create(width, height, GL_RGB32F, GL_RGB, GL_FLOAT), GL_COLOR_ATTACHMENT1);
        normalTexture = fbo.attachTexture(Texture.create(width, height, GL_RGB32F, GL_RGB, GL_FLOAT), GL_COLOR_ATTACHMENT2);
        lightingTexture = fbo.attachTexture(Texture.create(width, height, GL_RGB32F, GL_RGB, GL_FLOAT), GL_COLOR_ATTACHMENT3);

        fbo.setDrawBuffers(GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3);
    }

    public void bind() {
        fbo.bind();
    }

    public void unbind() {
        fbo.unbind();
    }

    public void bindReading() {
        for (int i = 0 ; i < 4; i++) {
            glActiveTexture(GL_TEXTURE0); glBindTexture(GL_TEXTURE_2D, colourTexture.getTextureID());
            glActiveTexture(GL_TEXTURE1); glBindTexture(GL_TEXTURE_2D, positionTexture.getTextureID());
            glActiveTexture(GL_TEXTURE2); glBindTexture(GL_TEXTURE_2D, normalTexture.getTextureID());
            glActiveTexture(GL_TEXTURE3); glBindTexture(GL_TEXTURE_2D, lightingTexture.getTextureID());
        }
    }

    public void unbindReading() {
        for (int i = 0 ; i < 4; i++) {
            glActiveTexture(GL_TEXTURE0); glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE1); glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE2); glBindTexture(GL_TEXTURE_2D, 0);
            glActiveTexture(GL_TEXTURE3); glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    public Texture getColourTexture() {
        return colourTexture;
    }

    public Texture getPositionTexture() {
        return positionTexture;
    }

    public Texture getNormalTexture() {
        return normalTexture;
    }

    public Texture getLightingTexture() {
        return lightingTexture;
    }

    public Texture getDepthTexture() {
        return fbo.getDepthTexture();
    }

    public void destroy() {
        fbo.destroy();
        colourTexture.destroy();
        positionTexture.destroy();
        normalTexture.destroy();
        lightingTexture.destroy();
    }

}
