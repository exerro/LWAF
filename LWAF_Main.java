
import lwaf.*;
import lwaf.util.CubeVAO;

import java.awt.datatransfer.Clipboard;
import java.io.IOException;

class CustomRenderer extends Renderer.CameraRenderer3D {
    private Camera camera;
    private VAO vao = new CubeVAO();

    CustomRenderer() {
        setShader(ShaderLoader.safeLoad(
                "lwaf/shader",
                "vertex-3D.glsl",
                "fragment-3D.glsl",
                false
        ));

        camera = new Camera(new vec3f(0, 1, 5));
        camera.rotateBy(new vec3f((float) Math.PI * -0.3f, 0, 0));
        camera.setPerspectiveProjection(
                Application.getActive().getDisplay().getAspectRatio(),
                Camera.PerspectiveProjection.DEFAULT_FOV,
                Camera.PerspectiveProjection.DEFAULT_NEAR,
                Camera.PerspectiveProjection.DEFAULT_FAR
        );
    }

    @Override
    public void setUniforms() {
        super.setUniforms();

        // getShader().setUniform("projectionTransform", new Camera(vec3f.zero).setPerspectiveProjection(Display.getActive().getAspectRatio()).getProjectionMatrix());
        // .rotate(new vec3f(1, 0, 0), (float) (Math.PI * 0.3))
        // getShader().setUniform("viewTransform", mat4f.translation(0, -1f, -5));
        getShader().setUniform("transform", mat4f.rotation(vec3f.y_axis, Application.getActive().getTime()));

        getShader().setUniform("useTexture", false);
        getShader().setUniform("colour", new vec3f(0.3f, 0.6f, 0.9f));

        getShader().setUniform("lightMinimum", 0.5f);
        getShader().setUniform("lightColour", new vec3f(1, 1, 1));
        getShader().setUniform("lightPosition", new vec3f(0, -1, 3));
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    protected void draw(FBO framebuffer) {
        Renderer.drawElements(vao);
    }
}

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

    private Text text;
    private Font font;

    @Override
    protected boolean load() {
        try {
            font = new Font("lwaf/font/open-sans/OpenSans-Regular.fnt");
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        text = new Text("Hello world", 100, font, 512);
        view = new View(1200, 680);
        view.attachRenderer(new CustomRenderer());

        return true;
    }

    @Override
    protected void draw() {
        Draw.setColour(1, 1, 1);
        Draw.view(view, new vec2f(40, 20));
        Draw.text(text, new vec2f(100, 100));
    }

    @Override
    protected void update(float dt) {

    }

    @Override
    protected void unload() {
        view.destroy();
    }

    @Override
    protected void onKeyDown(String key, int modifier) {
        System.out.println("Key pressed: " + MOD(key, modifier));
    }

    @Override
    protected void onTextInput(String text) {
        System.out.println(text);
    }
}
