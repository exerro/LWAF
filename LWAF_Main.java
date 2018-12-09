
import lwaf.*;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError {
        Display display = new Display("LWAF Demo");

        var app = new LWAF_Main(display);

        app.addUI(new Rect2D(new vec2f(), new vec2f(), new vec3f()));

        Application.run(app);
    }

    private LWAF_Main(Display display) {
        super(display);
    }

    @Override
    protected void draw() {
        super.draw();
    }

    @Override
    protected void update(float dt) {
        super.update(dt);
        System.out.println(dt);
    }
}
