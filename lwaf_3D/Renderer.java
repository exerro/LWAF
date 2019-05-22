package lwaf_3D;

import lwaf_core.GLFramebuffer;
import lwaf_core.GLTexture;
import lwaf_core.vec2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;

public class Renderer {
    private final GBuffer buffer;
    private final GLFramebuffer framebuffer;
    private final GLTexture texture;

    public Renderer(int width, int height) {
        buffer = new GBuffer(width, height);
        framebuffer = new GLFramebuffer(width, height);
        texture = lwaf_core.GLTextureKt.createEmptyTexture(width, height);

        framebuffer.attachTexture(texture, GL_COLOR_ATTACHMENT0);
        framebuffer.setDrawBuffers(GL_COLOR_ATTACHMENT0);
    }

    public int getWidth() {
        return texture.getWidth();
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public float getAspectRatio() {
        return (float) getWidth() / getHeight();
    }

    public GBuffer getGBuffer() {
        return buffer;
    }

    public GLFramebuffer getFramebuffer() {
        return framebuffer;
    }

    public GLTexture getTexture() {
        return texture;
    }

    public void draw(Scene scene) {
        var currentViewport = new int[4];
        var viewMatrix = scene.getCamera().getViewMatrix();
        var projectionMatrix = scene.getCamera().getProjectionMatrix();

        buffer.bind();

        glGetIntegerv(GL_VIEWPORT, currentViewport);
        glViewport(0, 0, getWidth(), getHeight());

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDepthMask(true);
        glClearColor(0, 0, 0, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        scene.drawObjects(viewMatrix, projectionMatrix);

        glDepthMask(false);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        buffer.unbind();
        framebuffer.bind();

        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        buffer.bindReading();

        for (var lightType : scene.getLightTypes()) {
            var shader = Light.Companion.getShader(lightType);

            shader.start();
            shader.setUniform("screenSize", new vec2(getWidth(), getHeight()));
            shader.setUniform("viewTransform", viewMatrix);
            shader.setUniform("projectionTransform", projectionMatrix);

            for (var light : scene.getLightsOfType(lightType)) {
                light.setUniforms(viewMatrix, projectionMatrix);
                light.render(buffer);
            }
        }

        framebuffer.unbind();
        buffer.unbindReading();

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glViewport(currentViewport[0], currentViewport[1], currentViewport[2], currentViewport[3]);
    }

    public void destroy() {
        buffer.destroy();
        framebuffer.destroy();
        texture.destroy();
    }
}