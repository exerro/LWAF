
import lwaf.*;

import java.io.IOException;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        Display display = new Display("LWAF Demo");

        var app = new LWAF_Main(display);

        Application.run(app);
    }

    UI rect = new Rect2D(new vec2f(), new vec2f(100, 100), new vec3f(1, 1, 0));

    private LWAF_Main(Display display) {
        super(display);
        addUI(rect);
    }

    @Override
    protected void draw() {
        super.draw();
    }

    @Override
    protected void update(float dt) {
        super.update(dt);
        rect.moveBy(new vec2f(dt * 10, dt * 2));
        System.out.println(dt);
    }
}
