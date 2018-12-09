
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
    private Texture texture;
    private Font font;
    private Text text1, text2, text3, text4;

    private LWAF_Main(Display display) {
        super(display);
    }

    @Override
    protected boolean load() {
        try {
            font = new Font("lwaf/font/open-sans/OpenSans-Regular.fnt");
            text1 = new Text("Hello world!", 0, font, 128);
            text2 = new Text("Hello world!", 0, font, 64);
            text3 = new Text("Hello world!", 0, font, 32);
            text4 = new Text("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz", 0, font, 32);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        texture = new Texture("im.png");

        view = new View(128, 128);
        view.attachRenderer(new Renderer() {
            @Override
            protected void draw(FBO framebuffer) {
                Draw.setColour(0, 0, 1);
                Draw.rectangle(10, 10, 64, 64);
                Draw.setColour(1, 1, 1);
            }
        });

        return true;
    }

    @Override
    protected void draw() {
        Draw.setColour(1, 1, 1);
        Draw.image(texture, new vec2f(0, 0), new vec2f(0.3f, 0.3f));
        Draw.view(view, new vec2f(0, 400));
        Draw.text(text4, new vec2f(200, 0));
        Draw.setColour(0, 0, 0);
        Draw.rectangle(200, 200, 1000, 128);
        Draw.rectangle(200, 400, 1000, 64);
        Draw.rectangle(200, 600, 1000, 32);
        Draw.setColour(1, 1, 1);
        Draw.text(text1, new vec2f(200, 200));
        Draw.text(text2, new vec2f(200, 400));
        Draw.text(text3, new vec2f(200, 600));
    }

    @Override
    protected void update(float dt) {

    }

    @Override
    protected void unload() {
        view.destroy();
    }
}
