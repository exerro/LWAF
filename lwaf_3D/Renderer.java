package lwaf_3D;

import lwaf.FBO;
import lwaf.Texture;

import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;

public class Renderer {
    private final GBuffer buffer;
    private final FBO framebuffer;
    private final Texture texture;

    public Renderer(int width, int height) {
        buffer = new GBuffer(width, height);
        framebuffer = new FBO(width, height);
        texture = Texture.create(width, height);

        framebuffer.attachTexture(texture, GL_COLOR_ATTACHMENT0);
        framebuffer.setDrawBuffers(GL_COLOR_ATTACHMENT0);
    }

    public GBuffer getGBuffer() {
        return buffer;
    }

    public FBO getFramebuffer() {
        return framebuffer;
    }

    public Texture getTexture() {
        return texture;
    }

    public void draw(Scene scene) {
        int[] currentViewport = new int[4];
        glViewport(0, 0, texture.getWidth(), texture.getHeight());
        scene.draw(framebuffer, buffer);
        glGetIntegerv(GL_VIEWPORT, currentViewport);
        glViewport(currentViewport[0], currentViewport[1], currentViewport[2], currentViewport[3]);
    }

    public void destroy() {
        buffer.destroy();
        framebuffer.destroy();
        texture.destroy();
    }
}
