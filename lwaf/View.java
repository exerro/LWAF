package lwaf;

import static org.lwjgl.opengl.GL11.*;

public class View {

    private final FBO fbo;
    private Renderer renderer;
    private vec3f colour = new vec3f(0, 0, 0);

    public View(int width, int height) {
        fbo = new FBO(width, height);
    }

    public void render() {
        if (renderer == null) return;

        fbo.bind();
        glClearColor(colour.x, colour.y, colour.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderer.draw(fbo);
        fbo.unbind();
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public Texture getTexture() {
        return fbo.getTexture();
    }

}
