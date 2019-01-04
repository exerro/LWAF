
import lwaf.*;
import lwaf.util.CubeVAO;
import lwaf.util.SphereVAO;
import lwaf.util.UVSphereVAO;

import java.io.IOException;

class CustomRenderer extends Renderer.CameraRenderer3D {
    private Camera camera;
    private VAO[] vaos = new VAO[] {
            new SphereVAO(1),
            new CubeVAO(),
            new UVSphereVAO(40, 80),
            new SphereVAO(7),

    };
    private vec3f[] vao_colours = new vec3f[] {
            new vec3f(0, 1, 1),
            new vec3f(1, 1, 1),
            new vec3f(1, 1, 1),
            new vec3f(1, 1, 0),
    };
    private VAO vao4 = new UVSphereVAO(4, 8);
    private vec3f lightPosition = new vec3f(0, -1, 3);
    private Texture texture = new Texture("lwaf/img/no-texture-light.png");

    CustomRenderer() {
        setShader(ShaderLoader.safeLoad(
                "lwaf/shader",
                "vertex-3D.glsl",
                "fragment-3D.glsl",
                false
        ));

        camera = new Camera(new vec3f(0, 1, 5));
        camera.rotateBy(new vec3f((float) Math.PI * -0.1f, 0, 0));
        camera.setPerspectiveProjection(
                Application.getActive().getDisplay().getAspectRatio(),
                Camera.PerspectiveProjection.DEFAULT_FOV,
                Camera.PerspectiveProjection.DEFAULT_NEAR,
                Camera.PerspectiveProjection.DEFAULT_FAR
        );
    }

    public void setLightPosition(vec3f position) {
        lightPosition = position;
    }

    @Override
    public void setUniforms() {
        super.setUniforms();

        // getShader().setUniform("projectionTransform", new Camera(vec3f.zero).setPerspectiveProjection(Display.getActive().getAspectRatio()).getProjectionMatrix());
        // .rotate(new vec3f(1, 0, 0), (float) (Math.PI * 0.3))
        // getShader().setUniform("viewTransform", mat4f.translation(0, -1f, -5));
        getShader().setUniform("transform", mat4f.rotation(vec3f.y_axis, Application.getActive().getTime()));

        getShader().setUniform("useTexture", false);

        getShader().setUniform("lightMinimum", 0.5f);
        getShader().setUniform("lightColour", new vec3f(1, 1, 1));
        getShader().setUniform("lightPosition", lightPosition);
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
        for (int i = 0; i < vaos.length; ++i) {
            getShader().setUniform("transform", mat4f.translation(i * 2, 0, 0)/*.rotate(vec3f.y_axis, Application.getActive().getTime())*/);
            getShader().setUniform("colour", vao_colours[i]);

            if (vaos[i] instanceof UVSphereVAO || vaos[i] instanceof CubeVAO) {
                getShader().setUniform("useTexture", true);
                texture.bind();
            }

            Renderer.drawElements(vaos[i]);

            if (vaos[i] instanceof UVSphereVAO || vaos[i] instanceof CubeVAO) {
                texture.unbind();
                getShader().setUniform("useTexture", false);
            }
        }

        getShader().setUniform("lightMinimum", 1f);
        getShader().setUniform("transform", mat4f.translation(lightPosition).scaleBy(0.1f));
        getShader().setUniform("colour", new vec3f(0.9f, 0.9f, 0.3f));
        Renderer.drawElements(vao4);

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
    private CustomRenderer renderer;

    @Override
    protected boolean load() {
        try {
            font = new Font("lwaf/font/open-sans/OpenSans-Regular.fnt");
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        text = new Text("Hello world", 100, font, 64);
        view = new View(1200, 680);
        view.attachRenderer(renderer = new CustomRenderer());

        return true;
    }

    @Override
    protected void draw() {
        Draw.setColour(1, 1, 1);
        Draw.view(view, new vec2f(40, 20));
        Draw.text(text, new vec2f(100, 100));
    }

    @Override
    protected void onMouseEvent(MouseEvent event) {
        System.out.println(event.position);

        event.onDrag((position) -> System.out.println("Dragged to " + position.toString()));
    }

    @Override
    protected void update(float dt) {
        var translation = renderer.getCamera().getTranslation();
        var rotation = renderer.getCamera().getRotation();
        var forward = renderer.getCamera().getFlatForward();
        var right = renderer.getCamera().getFlatRight();
        var speed = dt * 5;
        var rspeed = dt * (float) Math.PI / 2;

        text = new Text(getMousePosition().toString(), 700, font, 32);

        if (isKeyDown("a")) {
            translation = translation.sub(right.mul(speed));
        }
        if (isKeyDown("d")) {
            translation = translation.add(right.mul(speed));
        }

        if (isKeyDown("w")) {
            translation = translation.add(forward.mul(speed));
        }
        if (isKeyDown("s")) {
            translation = translation.sub(forward.mul(speed));
        }

        if (isKeyDown("space")) {
            translation = translation.add(vec3f.y_axis.mul(speed));
        }
        if (SHIFT()) {
            translation = translation.sub(vec3f.y_axis.mul(speed));
        }

        if (isKeyDown("left")) {
            rotation = rotation.add(vec3f.y_axis.mul(rspeed));
        }
        if (isKeyDown("right")) {
            rotation = rotation.sub(vec3f.y_axis.mul(rspeed));
        }

        if (isKeyDown("up")) {
            rotation = rotation.add(vec3f.x_axis.mul(rspeed));
        }
        if (isKeyDown("down")) {
            rotation = rotation.sub(vec3f.x_axis.mul(rspeed));
        }

        renderer.setLightPosition(
                vec3f.y_axis.mul((float) Math.sin(Application.getActive().getTime()))
           .add(vec3f.x_axis.mul((float) Math.cos(1.5 * Application.getActive().getTime())))
           .add(vec3f.z_axis.mul(1 + (float) Math.sin(0.3 * Application.getActive().getTime())))
           .add(vec3f.x_axis.mul(6))
        );

        renderer.getCamera().setTranslation(translation);
        renderer.getCamera().setRotation(rotation);
    }

    @Override
    protected void unload() {
        view.destroy();
    }

    @Override
    protected void onKeyDown(String key, int modifier) {
        switch (MOD(key, modifier)) {

        }
    }

    @Override
    protected void onTextInput(String text) {

    }
}
