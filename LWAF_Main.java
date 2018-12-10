
import lwaf.*;
import lwaf.util.CubeVAO;

import java.io.IOException;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        Display display = new Display("LWAF Demo");

        var app = new LWAF_Main(display);
        app.getDisplay().setBackgroundColour(0.5f, 0.5f, 0.5f);

        Application.run(app);
    }

    private View view;

    private LWAF_Main(Display display) {
        super(display);
    }

    @Override
    protected boolean load() {

        view = new View(1200, 680);
        view.attachRenderer(new Renderer.VAORenderer3D() {
            {
                setShader(ShaderLoader.safeLoad(
                        "lwaf/shader",
                        "vertex-3D.glsl",
                        "fragment-3D.glsl",
                        false
                ));

                setVAO(new CubeVAO());
            }

            @Override
            public void setUniforms() {

                getShader().setUniform("projectionTransform", mat4f.projection(
                        70,
                        Draw.getViewportSize(),
                        0.1f,
                        1000f
                ));
                getShader().setUniform("viewTransform", mat4f.translation(0, -1, -4));
                getShader().setUniform("transform", mat4f.rotation(new vec3f(0, 1, 0), Application.getActive().getTime()));

                getShader().setUniform("useTexture", false);
                getShader().setUniform("colour", new vec3f(1, 0.5f, 1));

                getShader().setUniform("lightMinimum", 0.5f);
                getShader().setUniform("lightColour", new vec3f(1, 1, 1));
                getShader().setUniform("lightPosition", new vec3f(0, -1, 3));

            }
        });

        return true;
    }

    @Override
    protected void draw() {
        Draw.setColour(1, 1, 1);
        Draw.view(view, new vec2f(40, 20));
    }

    @Override
    protected void update(float dt) {

    }

    @Override
    protected void unload() {
        view.destroy();
    }
}
