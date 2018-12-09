package lwaf;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class Application {

    private final Display display;
    private List<UI> uiComponents = new ArrayList<>();

    protected Application(Display display) {
        this.display = display;
    }

    protected void draw() {
        for (UI component : uiComponents) {
            component.draw();
        }
    }

    protected void update(float dt) {
        for (UI component : uiComponents) {
            component.update(dt);
        }
    }

    public <T extends UI> T addUI(T component) {
        uiComponents.add(component);
        return component;
    }

    public <T extends UI> T removeUI(T component) {
        uiComponents.remove(component);
        return component;
    }

    public void clearUI() {
        uiComponents.clear();
    }

    public Display getDisplay() {
        return display;
    }

    public static void run(Application app) throws Display.WindowCreationError {
        long nanos, deltaNanos, lastNanos;

        app.display.setup();

        Rect2D.init();

        lastNanos = System.nanoTime();

        do {
            nanos = System.nanoTime();
            deltaNanos = nanos - lastNanos;
            lastNanos = nanos;

            app.update(deltaNanos / 1000000000f);
            app.display.beginRenderFrame();
            app.draw();
            app.display.finishRenderFrame();
            app.display.pollEvents();
        } while (!app.display.windowShouldClose());

        app.display.destroy();
    }
}
