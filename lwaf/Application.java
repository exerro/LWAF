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

    public void run() throws Display.WindowCreationError {
        long nanos, deltaNanos, lastNanos;

        display.setup();

        lastNanos = System.nanoTime();

        do {
            nanos = System.nanoTime();
            deltaNanos = nanos - lastNanos;
            lastNanos = nanos;

            update(deltaNanos / 1000000000f);
            display.beginRenderFrame();
            draw();
            display.finishRenderFrame();
            display.pollEvents();
        } while (!display.windowShouldClose());

        display.destroy();
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

}
