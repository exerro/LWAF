
import lwaf.*;
import lwaf_3D.Camera;
import lwaf_3D.GBuffer;
import lwaf_3D.Scene;

import java.io.IOException;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        Display display = new Display("LWAF Demo");

        var app = new LWAF_Main(display);
        app.getDisplay().setBackgroundColour(1, 1, 1);

        Application.run(app);
    }

    private Scene scene;
    private GBuffer gBuffer;
    private Texture texture;
    private Text text;
    private Font font;
    private Renderer renderer;
    private ShaderLoader.Program shader;

    private LWAF_Main(Display display) {
        super(display);
    }

    @Override
    protected boolean load() {
        font = Font.safeLoad("lwaf/font/open-sans/OpenSans-Regular.fnt");
        text = new Text("!\"£$%^&*()_+-={}[]:@~;'#<>?,./`¬¦\\|", font);
        texture = Texture.load("lwaf/img/no-texture-dark.png");
        shader = Scene.safeLoadGeometryShader(
                "lwaf/shader",
                "vertex-3D.glsl",
                false
        );
        renderer = new Renderer();
        scene = new Scene() {
            @Override
            protected void drawObjects(mat4f viewMatrix, mat4f projectionMatrix) {
                shader.setUniform("viewTransform", viewMatrix);
                shader.setUniform("projectionTransform", projectionMatrix);
                shader.start();
                renderer.draw(shader);
                shader.stop();
            }
        };
        gBuffer = new GBuffer(1200, 680);
        scene.getCamera().setPerspectiveProjection(
                Application.getActive().getDisplay().getAspectRatio(),
                Camera.PerspectiveProjection.DEFAULT_FOV,
                Camera.PerspectiveProjection.DEFAULT_NEAR,
                Camera.PerspectiveProjection.DEFAULT_FAR
        );

        return true;
    }

    @Override
    protected void draw() {
        scene.draw(gBuffer);

        Draw.setColour(1, 1, 1);
        // Draw.view(view, new vec2f(40, 20));
        Draw.buffer(gBuffer, new vec2f(40, 20), vec2f.one);
    }

    @Override
    protected void onMouseEvent(MouseEvent event) {
        System.out.println(event.position);

        event.onDrag((position) -> System.out.println("Dragged to " + position.toString()));
    }

    @Override
    protected void update(float dt) {
        var translation = scene.getCamera().getTranslation();
        var rotation = scene.getCamera().getRotation();
        var forward = scene.getCamera().getFlatForward();
        var right = scene.getCamera().getFlatRight();
        var speed = dt * 5;
        var rspeed = dt * (float) Math.PI / 2;

        text = new Text(getMousePosition().toString(), font);

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

        scene.getCamera().setTranslation(translation);
        scene.getCamera().setRotation(rotation);
    }

    @Override
    protected void unload() {
        gBuffer.destroy();
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
