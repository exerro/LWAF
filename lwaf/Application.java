package lwaf;

import java.io.IOException;

@SuppressWarnings("unused")
public abstract class Application {

    private final Display display;
    private boolean running = true;

    protected Application(Display display) {
        this.display = display;
    }

    protected abstract void load();
    protected abstract void draw();
    protected abstract void update(float dt);
    protected abstract void unload();

    public void stop() {
        running = false;
    }

    public Display getDisplay() {
        return display;
    }

    public static void run(Application app) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        long nanos, deltaNanos, lastNanos;

        app.display.setup();

        Draw.init();

        app.load();

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
        } while (app.running && !app.display.windowShouldClose());

        app.unload();

        app.display.destroy();

        Draw.destroy();
    }
}
