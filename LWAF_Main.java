
import lwaf.*;
import lwaf_primitive.CubeVAO;
import lwaf_primitive.SphereVAO;
import lwaf_primitive.UVSphereVAO;
import lwaf_model.Model;
import lwaf_model.ModelRenderer;
import lwaf_primitive.ConeVAO;
import lwaf_primitive.CylinderVAO;
import lwaf_primitive.PyramidVAO;

import java.io.IOException;

class CustomRenderer extends ModelRenderer {
    private Camera camera;
    private vec3f lightPosition = new vec3f(0, -1, 3);
    private Model<SphereVAO> lightModel;

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

        var dark_texture = Texture.load("lwaf/img/no-texture-light.png");

        for (int i = 0; i < 5; ++i) {
            add(new Model<>(new SphereVAO(i + 1)))
                    .setTranslation(i * 2, -3, -2);
        }

        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                add(new Model<>(new UVSphereVAO(j + 1, i + 3)))
                        .setTranslation(i * 2, 3 + j * 2, -2)
                        .setTexture(dark_texture);
            }
        }

        add(new Model<>(new SphereVAO(1)))
                .setColour(0, 1, 1)
                .setTranslation(0, 0, 0);

        add(new Model<>(new CubeVAO()))
                .setTexture(Texture.load("lwaf/img/no-texture-light.png"))
                .setTranslation(2, 0, 0);

        add(new Model<>(new UVSphereVAO(40, 80)))
                .setTexture(Texture.load("lwaf/img/no-texture-dark.png"))
                .setTranslation(4, 0, 0);

        add(new Model<>(new SphereVAO(7)))
                .setColour(1, 1, 0)
                .setTranslation(6, 0, 0);

        add(new Model<>(new ConeVAO(360)))
                .setTranslation(-2, 0, 0);

        add(new Model<>(new CylinderVAO(100)))
                .setTranslation(-2, 2, 0);

        add(new Model<>(new PyramidVAO(4)))
                .setTranslation(-2, 4, 0);

        lightModel = new Model<>(new SphereVAO(5))
                .setColour(0.9f, 0.9f, 0.3f)
                .setScale(0.1f);
    }

    public void setLightPosition(vec3f position) {
        lightPosition = position;
    }

    @Override
    public void setUniforms() {
        super.setUniforms();

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
        for (var model : getModels()) {
            model.setRotation(0, Application.getActive().getTime(), 0);
        }

        getShader().setUniform("lightMinimum", 0.3f);

        super.draw(framebuffer);

        lightModel.setTranslation(lightPosition);
        getShader().setUniform("lightMinimum", 1f);
        lightModel.draw(getShader());
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
        font = Font.safeLoad("lwaf/font/open-sans/OpenSans-Regular.fnt");
        text = new Text("!\"£$%^&*()_+-={}[]:@~;'#<>?,./`¬¦\\|", 100, font);
        view = new View(1200, 680);
        view.attachRenderer(renderer = new CustomRenderer());

        return true;
    }

    @Override
    protected void draw() {
        Draw.setColour(1, 1, 1);
        Draw.view(view, new vec2f(40, 20));

        Draw.setColour(0.5f, 0.5f, 0.5f);
        Draw.rectangle(0, 0, font.getWidth(text.getText()), font.getHeight());

        Draw.setColour(1, 1, 1);
        Draw.text(text, new vec2f(0, 0));

        Draw.setColour(1, 1, 1);
        Draw.text(new Text(
                String.valueOf(font.getWidth(text.getText())) + " |",
                1000,
                font.resizeTo(32)
        ), new vec2f(0, font.getHeight() + 5));
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

        text = new Text(getMousePosition().toString(), 700, font);

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
