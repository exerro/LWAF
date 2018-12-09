
import lwaf.*;

import java.io.IOException;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        Display display = new Display("LWAF Demo");

        var app = new LWAF_Main(display);
        app.getDisplay().setBackgroundColour(0.5f, 0.5f, 0.5f);

        Application.run(app);
    }

    private float x = 0;
    Texture texture;

    private LWAF_Main(Display display) {
        super(display);
    }

    @Override
    protected void load() {
        texture = new Texture("im.png");
    }

    @Override
    protected void draw() {
        super.draw();
        Draw.setColour(new vec3f(255, 255, 0));
        Draw.rectangle(new vec2f(x, 0), new vec2f(100, 100));
        Draw.image(new vec2f(1216, 0), texture);
    }

    @Override
    protected void update(float dt) {
        super.update(dt);
        x += dt * 10;
    }
}
