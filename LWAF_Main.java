
import lwaf.Application;
import lwaf.Display;

public class LWAF_Main extends Application {
    public static void main(String[] args) throws Display.WindowCreationError {
        Display display = new Display("LWAF Demo");
        new LWAF_Main(display).run();
    }

    private LWAF_Main(Display display) {
        super(display);
    }

    @Override
    protected void draw() {

    }

    @Override
    protected void update(float dt) {
        System.out.println(dt);
    }
}
