
import lwaf.*;
import lwaf_3D.*;
import lwaf_model.Model;
import lwaf_primitive.ConeVAO;

import java.io.IOException;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        Display display = new Display("LWAF Demo");

        var app = new LWAF_Main(display);
        app.getDisplay().setBackgroundColour(1, 1, 1);

        Application.run(app);
    }

    private LWAF_Main(Display display) {
        super(display);
    }

    private Scene scene;
    private Texture texture;
    private Text text;
    private Font font;
    private Models models;
    private ShaderLoader.Program shader;
    private Renderer renderer;
    private int textureToDraw = 0;

    @Override
    protected boolean load() {
        font = Font.safeLoad("lwaf/font/open-sans/OpenSans-Regular.fnt");
        text = new Text("!\"£$%^&*()_+-={}[]:@~;'#<>?,./`¬¦\\|", font);
        texture = Texture.load("lwaf/img/no-texture-dark.png");

        shader = GBuffer.safeLoadGeometryShader(
                "lwaf_3D/shader",
                "vertex-3D.glsl",
                false
        );

        models = new Models();

        scene = new Scene() {
            {
                addLight(new Light.AmbientLight(0.2f));
//                addLight(new Light.DirectionalLight(0.4f, vec3f.one.unm(), vec3f.y_axis));
//                addLight(new Light.DirectionalLight(0.4f, vec3f.y_axis, vec3f.x_axis));
//                addLight(new Light.DirectionalLight(0.4f, vec3f.y_axis.unm()));
                addLight(new Light.PointLight(10, new vec3f(0, 4f, 10), Light.PointLight.attenuation(25), new vec3f(1, 1, 0)));

                addLight(new Light.SpotLight(
                        1,
                        new vec3f(0, -10 + 5, 11),
                        new vec3f(0, -1, 0),
                        new vec3f(1, 0.09f, 0.032f),
                        Light.SpotLight.lightSpread(0.6f),
                        vec3f.one
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(20, -4, 0),
                        new vec3f(5, -1, 0),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.12f),
                        vec3f.one
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(40, 10, 40),
                        new vec3f(2, -2, -5),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.1f),
                        vec3f.one
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(50, 20, -30),
                        new vec3f(-2, -6, 5),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.1f),
                        vec3f.one
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(10, -6, 0),
                        new vec3f(-8, -5, -5),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.3f),
                        vec3f.x_axis
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(0, -4, -20),
                        new vec3f(-5, -5, 8),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.3f),
                        vec3f.y_axis
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(-30, -4, -15),
                        new vec3f(8, -5, 5),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.3f),
                        vec3f.z_axis
                ));

                addLight(new Light.SpotLight(
                        1000,
                        new vec3f(0, 0, 10),
                        new vec3f(1, 1, -1),
                        new vec3f(1000, 0.09f, 1f),
                        Light.SpotLight.lightSpread(0.2f),
                        vec3f.one
                ));
            }

            @Override
            protected void drawObjects(mat4f viewMatrix, mat4f projectionMatrix) {
                shader.setUniform("viewTransform", viewMatrix);
                shader.setUniform("projectionTransform", projectionMatrix);
                shader.start();
                models.draw(shader);
                shader.stop();
            }
        };

        renderer = new Renderer(1200, 680);

        scene.getCamera().setPerspectiveProjection(
                renderer.getAspectRatio(),
                Camera.PerspectiveProjection.DEFAULT_FOV,
                Camera.PerspectiveProjection.DEFAULT_NEAR,
                Camera.PerspectiveProjection.DEFAULT_FAR
        );

        return true;
    }

    @Override
    protected void unload() {
        renderer.destroy();
        texture.destroy();
        shader.destroy();
    }

    @Override
    protected void draw() {
        renderer.draw(scene);

        // Draw.view(view, new vec2f(40, 20));
        // Draw.buffer(renderer.getGBuffer(), new vec2f(40, 20), vec2f.one);

        Draw.setColour(0, 0, 0);
        Draw.text(text, vec2f.zero);

        Texture texture = null;

        switch (textureToDraw) {
            case 0: texture = renderer.getTexture(); break;
            case 1: texture = renderer.getGBuffer().getColourTexture(); break;
            case 2: texture = renderer.getGBuffer().getNormalTexture(); break;
            case 3: texture = renderer.getGBuffer().getPositionTexture(); break;
            case 4: texture = renderer.getGBuffer().getLightingTexture(); break;
        }

        Draw.setColour(1, 1, 1);
        Draw.texture(texture);
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
    protected void onKeyDown(String key, int modifier) {
        switch (MOD(key, modifier)) {
            case "tab": textureToDraw = (textureToDraw + 1) % 5; break;
            case "ctrl-tab": textureToDraw = (textureToDraw - 1) % 5; break;
        }

        if (textureToDraw < 0) textureToDraw += 5;
    }

    @Override
    protected void onTextInput(String text) {

    }
}
