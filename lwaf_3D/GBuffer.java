package lwaf_3D;

import lwaf.ShaderLoader;
import lwaf_core.GLFBO;
import lwaf_core.GLShaderProgram;
import lwaf_core.GLTexture;
import lwaf_core.GLTextureKt;

import java.io.IOException;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL30.*;

public class GBuffer {
    private final GLFBO fbo;
    private final GLTexture colourTexture, positionTexture, normalTexture, lightingTexture;

    public GBuffer(int width, int height) {
        fbo = new GLFBO(width, height);

        colourTexture = fbo.attachTexture(GLTextureKt.createEmptyTexture(width, height), GL_COLOR_ATTACHMENT0);
        positionTexture = fbo.attachTexture(GLTextureKt.createEmptyTexture(width, height, GL_RGB32F, GL_RGB, GL_FLOAT), GL_COLOR_ATTACHMENT1);
        normalTexture = fbo.attachTexture(GLTextureKt.createEmptyTexture(width, height, GL_RGB32F, GL_RGB, GL_FLOAT), GL_COLOR_ATTACHMENT2);
        lightingTexture = fbo.attachTexture(GLTextureKt.createEmptyTexture(width, height, GL_RGB32F, GL_RGB, GL_FLOAT), GL_COLOR_ATTACHMENT3);

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

    public GLTexture getColourTexture() {
        return colourTexture;
    }

    public GLTexture getPositionTexture() {
        return positionTexture;
    }

    public GLTexture getNormalTexture() {
        return normalTexture;
    }

    public GLTexture getLightingTexture() {
        return lightingTexture;
    }

    public GLTexture getDepthTexture() {
        return fbo.getDepthTexture();
    }

    public void destroy() {
        fbo.destroy();
        colourTexture.destroy();
        positionTexture.destroy();
        normalTexture.destroy();
        lightingTexture.destroy();
    }

    public static String FRAGMENT_SHADER_PATH = "lwaf_3D/shader/gbuffer-render.fragment-3D.glsl";

    public static GLShaderProgram safeLoadGeometryShader(String basePath, String vertexShader, boolean instanced) {
        return ShaderLoader.safeLoad(
                "",
                Paths.get(basePath, vertexShader).toString(),
                FRAGMENT_SHADER_PATH,
                instanced
        );
    }

}
