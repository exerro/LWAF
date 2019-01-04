package lwaf;

import java.io.IOException;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Application {
    private final Display display;
    private boolean running = true;
    private float time = 0;
    private Set<String> keysHeld = new HashSet<>();
    private boolean mouseLocked = false;

    private static Application active;

    protected Application(Display display) {
        this.display = display;
    }

    protected abstract boolean load();
    protected abstract void draw();
    protected abstract void update(float dt);
    protected abstract void unload();

    protected void onKeyDown(String key, int modifiers) {}
    protected void onTextInput(String text) {}
    protected void onKeyUp(String key, int modifiers) {}
    protected void onMouseEvent(MouseEvent event) {}
    protected void onMouseMove(vec2f position, boolean free) {} // free is true if no mouse buttons are currently held
    protected void onPaste() {}
    protected void onCopy() {}
    protected void onCut() {}

    public vec2f getMousePosition() {
        double[] x = new double[1], y = new double[1];

        glfwGetCursorPos(getDisplay().windowID, x, y);

        return new vec2f((float) x[0], (float) y[0]);
    }

    public boolean isKeyDown(String key) {
        return keysHeld.contains(key);
    }

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
        Map<Integer, MouseEvent> mouseEvents = new HashMap<>();
        Map<Integer, Boolean> heldKeys;
        List<String> keyModifiers;
        float dt;

        app.display.setup();
        app.time = 0;

        Draw.init();

        active = app;

        if (!app.load()) {
            app.display.destroy();
            Draw.destroy();
            active = null;
            return;
        }

        glfwSetCursorPosCallback(app.getDisplay().windowID, (window, x, y) -> {
            vec2f position = new vec2f((float) x, (float) y);

            app.onMouseMove(position, mouseEvents.isEmpty());

            for (MouseEvent event : mouseEvents.values()) {
                event.moved = true;
                event.move(position);
            }
        });

        glfwSetMouseButtonCallback(app.getDisplay().windowID, (window, button, action, mods) -> {
            vec2f position = app.getMousePosition();

            if (action == GLFW_PRESS) {
                if (mouseEvents.containsKey(button)) mouseEvents.get(button).up(position);
                MouseEvent event = new MouseEvent(position, button, mods);
                mouseEvents.put(button, event);
                app.onMouseEvent(event);
            }
            else {
                if (mouseEvents.containsKey(button)) {
                    mouseEvents.get(button).up(position);
                    mouseEvents.remove(button);
                }
            }
        });

        glfwSetKeyCallback(app.getDisplay().windowID, (window, key, scancode, action, mods) -> {
            String keyName = glfwGetKeyName(key, scancode);

            switch (key) {
                case GLFW_KEY_SPACE: keyName = "space"; break;
                case GLFW_KEY_BACKSPACE: keyName = "backspace"; break;
                case GLFW_KEY_ENTER: keyName = "enter"; break;
                case GLFW_KEY_UP: keyName = "up"; break;
                case GLFW_KEY_DOWN: keyName = "down"; break;
                case GLFW_KEY_LEFT: keyName = "left"; break;
                case GLFW_KEY_RIGHT: keyName = "right"; break;
                case GLFW_KEY_TAB: keyName = "tab"; break;
                case GLFW_KEY_ESCAPE: keyName = "escape"; break;
                case GLFW_KEY_DELETE: keyName = "delete"; break;
            }

            if (keyName != null) {
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    if      (key == GLFW_KEY_V && CTRL(mods)) app.onPaste();
                    else if (key == GLFW_KEY_C && CTRL(mods)) app.onCopy();
                    else if (key == GLFW_KEY_X && CTRL(mods)) app.onCut();
                    else {
                        app.keysHeld.add(keyName);
                        app.onKeyDown(keyName, mods);
                    }
                }
                else if (action == GLFW_RELEASE) {
                    app.keysHeld.remove(keyName);
                    app.onKeyUp(keyName, mods);
                }
            }
        });

        glfwSetCharCallback(app.getDisplay().windowID, (window, unicode) -> app.onTextInput(new String(Character.toChars(unicode))));

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

        glfwSetCharCallback(app.getDisplay().windowID, null);
        glfwSetKeyCallback(app.getDisplay().windowID, null);

        active = null;
    }

    protected static boolean CTRL(int modifier) {
        return (modifier & GLFW_MOD_CONTROL) != 0;
    }

    protected static boolean ALT(int modifier) {
        return (modifier & GLFW_MOD_ALT) != 0;
    }

    protected static boolean SHIFT(int modifier) {
        return (modifier & GLFW_MOD_SHIFT) != 0;
    }

    protected static boolean CTRL() {
        return glfwGetKey(getActive().getDisplay().windowID, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS || glfwGetKey(getActive().getDisplay().windowID, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS;
    }

    protected static boolean ALT() {
        return glfwGetKey(getActive().getDisplay().windowID, GLFW_KEY_LEFT_ALT) == GLFW_PRESS || glfwGetKey(getActive().getDisplay().windowID, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS;
    }

    protected static boolean SHIFT() {
        return glfwGetKey(getActive().getDisplay().windowID, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS || glfwGetKey(getActive().getDisplay().windowID, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS;
    }

    protected static String MOD(String key, int modifier) {
        if (ALT(modifier)) key = "alt-" + key;
        if (SHIFT(modifier)) key = "shift-" + key;
        if (CTRL(modifier)) key = "ctrl-" + key;
        return key;
    }
}
