package lwaf;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("unused")
public abstract class Application {
    private final Display display;
    private boolean running = true;
    private float time = 0;
    private Set<String> keysHeld = new HashSet<>();

    private static Application active;

    protected Application(Display display) {
        this.display = display;
    }

    protected abstract boolean load();
    protected abstract void draw();
    protected abstract void update(float dt);
    protected abstract void unload();

    protected abstract void onKeyDown(String key, int modifiers);
    protected abstract void onTextInput(String text);
    protected void onKeyUp(String key, int modifiers) {}
    protected void onPaste() {}
    protected void onCopy() {}
    protected void onCut() {}

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

        glfwSetKeyCallback(app.getDisplay().windowID, (window, key, scancode, action, mods) -> {
            String keyName = glfwGetKeyName(key, scancode);

            switch (key) {
                case GLFW_KEY_SPACE:
                    keyName = "space";
                    break;
                case GLFW_KEY_BACKSPACE:
                    keyName = "backspace";
                    break;
                case GLFW_KEY_ENTER:
                    keyName = "enter";
                    break;
                case GLFW_KEY_UP:
                    keyName = "up";
                    break;
                case GLFW_KEY_DOWN:
                    keyName = "down";
                    break;
                case GLFW_KEY_LEFT:
                    keyName = "left";
                    break;
                case GLFW_KEY_RIGHT:
                    keyName = "right";
                    break;
                case GLFW_KEY_TAB:
                    keyName = "tab";
                    break;
                case GLFW_KEY_ESCAPE:
                    keyName = "escape";
                    break;
                case GLFW_KEY_DELETE:
                    keyName = "delete";
                    break;
            }

            if (keyName != null) {
                if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                    if (key == GLFW_KEY_V && (mods & GLFW_MOD_CONTROL) != 0) {
                        app.onPaste();
                        return;
                    }
                    else if (key == GLFW_KEY_C && (mods & GLFW_MOD_CONTROL) != 0) {
                        app.onCopy();
                        return;
                    }
                    else if (key == GLFW_KEY_X && (mods & GLFW_MOD_CONTROL) != 0) {
                        app.onCut();
                        return;
                    }

                    app.keysHeld.add(keyName);
                    app.onKeyDown(keyName, mods);
                }
                else if (action == GLFW_RELEASE) {
                    app.keysHeld.remove(keyName);
                    app.onKeyUp(keyName, mods);
                }
            }
        });

        glfwSetCharCallback(app.getDisplay().windowID, (window, unicode) -> {
            app.onTextInput(new String(Character.toChars(unicode)));
        });

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
