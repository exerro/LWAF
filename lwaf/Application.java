package lwaf;

@SuppressWarnings("unused")
public abstract class Application {

    private final Display display;

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

    protected abstract void draw();
    protected abstract void update(float dt);

    public Display getDisplay() {
        return display;
    }

}
