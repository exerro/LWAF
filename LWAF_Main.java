
import lwaf.*;

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
    protected void load() {
        view = new View(128, 128);
        view.attachRenderer(new Renderer() {
            @Override
            protected void draw(FBO framebuffer) {
                Draw.setColour(0, 0, 1);
                Draw.rectangle(new vec2f(10, 10), new vec2f(64, 64));
                Draw.setColour(1, 1, 1);
            }
        });
    }

    @Override
    protected void draw() {
        Draw.view(view);
    }

    @Override
    protected void update(float dt) {

    }

    @Override
    protected void unload() {
        view.destroy();
    }
}
