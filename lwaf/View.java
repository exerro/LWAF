package lwaf;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class View {

    private final FBO fbo;
    private List<Renderer> renderers = new ArrayList<>();
    private vec3f colour = new vec3f(0, 0, 0);

    public View(int width, int height) {
        fbo = new FBO(width, height);
    }

    void render() {
        fbo.bind();
        glClearColor(colour.x, colour.y, colour.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        for (var renderer : renderers) {
            renderer.preDraw(fbo);
            renderer.draw(fbo);
            renderer.postDraw(fbo);
        }

        fbo.unbind();
    }

    public <T extends Renderer> T attachRenderer(T renderer) {
        renderer.load();
        renderers.add(renderer);
        return renderer;
    }

    public <T extends Renderer> T detachRenderer(T renderer) {
        renderer.unload();
        renderers.remove(renderer);
        return renderer;
    }

    public Texture getTexture() {
        return fbo.getTexture();
    }

    public void destroy() {
        fbo.destroy();

        for (var renderer : renderers) {
            renderer.unload();
        }
    }

}
