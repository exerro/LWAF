
import kotlin.Unit;
import lwaf_3D.*;
import lwaf_core.*;
import lwaf_core.Display;

import static org.lwjgl.glfw.GLFW.*;

public class LWAF_Main {
    public static void main(String[] args) {
        var display = new lwaf_core.Display(1080, 720, "LWAF Demo");

        new LWAF_Main(display);

        display.run();
    }

    private LWAF_Main(lwaf_core.Display display) {
        this.display = display;

        display.setOnLoadCallback(() -> { load(); return Unit.INSTANCE; });
        display.setOnUnloadCallback(() -> { unload(); return Unit.INSTANCE; });
        display.setOnDrawCallback(() -> { draw(); return Unit.INSTANCE; });
        display.setOnUpdateCallback((dt) -> { update(dt); return Unit.INSTANCE; });
        display.setOnKeyPressedCallback((a, b) -> { onKeyDown(a, b); return Unit.INSTANCE; });
        display.setOnResizedCallback((w, h) -> {
            view.setSize(new vec2(w, h));
            return Unit.INSTANCE;
        });
    }

    private final Display display;
    private Scene scene;
    private GLTexture texture;
    private Text text;
    private Font font;
    private Models models;
    private GLShaderProgram shader;
    private Renderer renderer;
    private int textureToDraw = 0;
    private GLView view;
    private DrawContext2D context2D;

    protected boolean load() {
        font = Font.Companion.safeLoad("lwaf_res/font/open-sans/OpenSans-Regular.fnt");
        text = new Text("!\"£$%^&*()_+-={}[]:@~;'#<>?,./`¬¦\\|", font);
        texture = GLTextureKt.loadTexture("lwaf_res/img/no-texture-dark.png");
        view = new GLView(new vec2(0f, 0f), display.getWindowSize());
        context2D = new DrawContext2D(view);

        shader = GBuffer.safeLoadGeometryShader(
                "lwaf_3D/shader/vertex-3D.glsl",
                false
        );

        models = new Models(new DrawContext3D(new GLView(new vec2(0, 0), display.getWindowSize())));

        scene = new Scene() {
            {
                addLight(new Light.AmbientLight(0.05f));
                addLight(new Light.DirectionalLight(0.4f, new vec3(-1, -1, -1), new vec3(0, 1, 0)));
                addLight(new Light.DirectionalLight(0.4f, new vec3(0, 1, 0), new vec3(1, 0, 0)));
                addLight(new Light.DirectionalLight(0.4f, new vec3(0, -1, 0)));
                addLight(new Light.PointLight(10, new vec3(0, 4f, 10), Light.PointLight.attenuation(25), new vec3(1, 1, 0)));
//
//                addLight(new Light.SpotLight(
//                        1,
//                        new vec3f(0, -10 + 5, 11),
//                        new vec3f(0, -1, 0),
//                        new vec3f(1, 0.09f, 0.032f),
//                        Light.SpotLight.lightSpread(0.6f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(20, -4, 0),
//                        new vec3f(5, -1, 0),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.12f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(40, 10, 40),
//                        new vec3f(2, -2, -5),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.1f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(50, 20, -30),
//                        new vec3f(-2, -6, 5),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.1f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(10, -6, 0),
//                        new vec3f(-8, -5, -5),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.2f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(0, -4, -20),
//                        new vec3f(-5, -5, 8),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.2f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(-30, -4, -15),
//                        new vec3f(8, -5, 5),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.2f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(-30, -4, 30),
//                        new vec3f(5, -5, -8),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.2f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.SpotLight(
//                        1000,
//                        new vec3f(0, 0, 10),
//                        new vec3f(1, 1, -1),
//                        new vec3f(1000, 0.09f, 1f),
//                        Light.SpotLight.lightSpread(0.2f),
//                        vec3f.one
//                ));
//
//                addLight(new Light.PointLight(
//                        3,
//                        new vec3f(40, 5, 5)
//                ));
            }

            @Override
            protected void drawObjects(mat4 viewMatrix, mat4 projectionMatrix) {
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

    protected void unload() {
        renderer.destroy();
        texture.destroy();
        shader.destroy();
    }

    protected void draw() {
        renderer.draw(scene);

        // Draw2D.view(view, new vec2f(40, 20));
        // Draw2D.buffer(renderer.getGBuffer(), new vec2f(40, 20), vec2f.one);

//        Draw.setColour(0, 0, 0);
//        Draw.text(text, vec2f.zero);

        GLTexture texture = null;

        switch (textureToDraw) {
            case 0: texture = renderer.getTexture(); break;
            case 1: texture = renderer.getGBuffer().getColourTexture(); break;
            case 2: texture = renderer.getGBuffer().getNormalTexture(); break;
            case 3: texture = renderer.getGBuffer().getPositionTexture(); break;
            case 4: texture = renderer.getGBuffer().getLightingTexture(); break;
        }

        context2D.setColour(1f, 1f, 1f);
        context2D.drawTexture(texture, new vec2(0f, 0f), new vec2(1f, 1f));
        context2D.write(text, new vec2(0, 0));
    }

    protected void update(float dt) {
        var translation = scene.getCamera().getTranslation();
        var rotation = scene.getCamera().getRotation();
        var forward = scene.getCamera().getFlatForward();
        var right = scene.getCamera().getFlatRight();
        var speed = dt * 5;
        var rspeed = dt * (float) Math.PI / 2;

        text = new Text(display.getMousePosition().toString(), font);

        if (display.isKeyDown(GLFW_KEY_A)) {
            translation = translation.sub(right.mul(speed));
        }
        if (display.isKeyDown(GLFW_KEY_D)) {
            translation = translation.add(right.mul(speed));
        }

        if (display.isKeyDown(GLFW_KEY_W)) {
            translation = translation.add(forward.mul(speed));
        }
        if (display.isKeyDown(GLFW_KEY_S)) {
            translation = translation.sub(forward.mul(speed));
        }

        if (display.isKeyDown(GLFW_KEY_SPACE)) {
            translation = translation.add(new vec3(0, speed, 0));
        }
        if (display.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            translation = translation.sub(new vec3(0, speed, 0));
        }

        if (display.isKeyDown(GLFW_KEY_LEFT)) {
            rotation = rotation.add(new vec3(0, rspeed, 0));
        }
        if (display.isKeyDown(GLFW_KEY_RIGHT)) {
            rotation = rotation.sub(new vec3(0, rspeed, 0));
        }

        if (display.isKeyDown(GLFW_KEY_UP)) {
            rotation = rotation.add(new vec3(rspeed, 0, 0));
        }
        if (display.isKeyDown(GLFW_KEY_DOWN)) {
            rotation = rotation.sub(new vec3(rspeed, 0, 0));
        }

        scene.getCamera().setTranslation(translation);
        scene.getCamera().setRotation(rotation);
    }

    protected void onKeyDown(int key, int modifier) {
        if (key == GLFW_KEY_TAB) {
            if ((modifier & GLFW_MOD_CONTROL) != 0) {
                textureToDraw = (textureToDraw + 1) % 5;
            }
            else {
                textureToDraw = (textureToDraw - 1) % 5;
            }
        }

        if (textureToDraw < 0) textureToDraw += 5;
    }
}
