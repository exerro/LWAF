package lwaf;

import java.io.IOException;

@SuppressWarnings("unused")
public abstract class Application {

    private final Display display;
    private boolean running = true;
    private float time = 0;

    private static Application active;

    protected Application(Display display) {
        this.display = display;
    }

    protected abstract boolean load();
    protected abstract void draw();
    protected abstract void update(float dt);
    protected abstract void unload();

    public void stop() {
        running = false;
    }

    public float getTime() {
        return time;
    }

    public Display getDisplay() {
        return display;
    }

    public static Application getActive() {
        return active;
    }

    public static void run(Application app) throws Display.WindowCreationError, ShaderLoader.ProgramLoadException, IOException, ShaderLoader.ShaderLoadException {
        long nanos, deltaNanos, lastNanos;
        float dt;

        app.display.setup();
        app.time = 0;

        Draw.init();

        if (!app.load()) {
            app.display.destroy();
            Draw.destroy();
            return;
        }

        active = app;

        lastNanos = System.nanoTime();

        do {
            nanos = System.nanoTime();
            deltaNanos = nanos - lastNanos;
            lastNanos = nanos;
            dt = deltaNanos / 1000000000f;
            app.time += dt;

            app.update(dt);
            app.display.beginRenderFrame();
            app.draw();
            app.display.finishRenderFrame();
            app.display.pollEvents();
        } while (app.running && !app.display.windowShouldClose());

        app.unload();
        app.display.destroy();
        Draw.destroy();

        active = null;
    }
}
