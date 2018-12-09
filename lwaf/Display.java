package lwaf;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

// handles creating and rendering to a window
@SuppressWarnings({"unused", "WeakerAccess"})
public class Display {

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;
    public static final String DEFAULT_TITLE = "Unnamed LWAF Window";

    private static Display active;

    long windowID;

    private int width, height;
    private String title;
    private int cr = 0, cg = 0, cb = 0;
    private boolean isSetup = false;

    public Display(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;

        windowID = 0;
    }

    public Display(int width, int height) {
        this(width, height, DEFAULT_TITLE);
    }

    public Display(String title) {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, title);
    }

    public Display() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_TITLE);
    }

    // sets up the display
    // must be called for window to become visible
    public void setup() throws WindowCreationError {
        if (isSetup) return;

        setupGLFWWindow();
        setupOpenGL();
        isSetup = true;
    }

    // sets up the GLFW window
    private void setupGLFWWindow() throws WindowCreationError {
        // causes gl errors to be printed to stderr
        GLFWErrorCallback.createPrint(java.lang.System.err).set();

        // check that GLFW can be initialised
        if (!glfwInit()) {
            throw new WindowCreationError("Unable to initialise GLFW");
        }

        // set window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // create window
        windowID = glfwCreateWindow(width, height, title, NULL, NULL);

        // if a window wasn't created, throw a runtime exception
        if (windowID == NULL) {
            throw new WindowCreationError("Failed to create GLFW window");
        }

        // think this makes the window centered?
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowID, pWidth, pHeight);

            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (videoMode != null) {
                glfwSetWindowPos(windowID,
                        (videoMode.width() - pWidth.get(0)) / 2,
                        (videoMode.height() - pHeight.get(0)) / 2
                );
            }
            // otherwise, do nothing, centering isn't that important...
        }

        // don't really know what these are for but I found them online so fuck it
        glfwMakeContextCurrent(windowID);
        glfwSwapInterval(1);
        // think this has something to do with the centering
        glfwShowWindow(windowID);
    }

    private void setupOpenGL() {
        GL.createCapabilities();

        glClearColor(cr, cg, cb, 1.0f);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
        glEnable(GL_DEPTH_TEST);
    }

    // returns true if the window should close (e.g. close button pressed)
    public boolean windowShouldClose() {
        if (!isSetup) {
            throw new IllegalStateException("Display is not yet setup, call setup() first");
        }

        return glfwWindowShouldClose(windowID);
    }

    // begins a render frame, clearing the screen
    public void beginRenderFrame() {
        if (!isSetup) {
            throw new IllegalStateException("Display is not yet setup, call setup() first");
        }

        glClearColor(cr, cg, cb, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        active = this;
    }

    // finishes a render frame, swapping buffers
    public void finishRenderFrame() {
        if (!isSetup) {
            throw new IllegalStateException("Display is not yet setup, call setup() first");
        }

        glfwSwapBuffers(windowID);
    }

    // polls events
    // don't forget to call this...
    public void pollEvents() {
        if (!isSetup) {
            throw new IllegalStateException("Display is not yet setup, call setup() first");
        }

        glfwPollEvents();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public float getAspectRatio() {
        return (float) width / height;
    }

    public void setWidth(int width) {
        this.width = width;

        if (isSetup) {
            glfwSetWindowSize(windowID, width, height);
        }
    }

    public void setHeight(int height) {
        this.height = height;

        if (isSetup) {
            glfwSetWindowSize(windowID, width, height);
        }
    }

    public void setTitle(String title) {
        this.title = title;

        if (isSetup) {
            glfwSetWindowTitle(windowID, title);
        }
    }

    // sets the colour of the background of each frame prior to any rendering
    public void setBackgroundColour(int r, int g, int b) {
        cr = r;
        cg = g;
        cb = b;
    }

    // destroys the window, calling respective gl destroy/free functions
    // don't forget to do this
    // like seriously
    // don't...
    // ...forget
    // don't forget!
    public void destroy() {
        glfwFreeCallbacks(windowID);
        glfwDestroyWindow(windowID);
        glfwTerminate();
        // TODO: what does this do?
        GLFWErrorCallback x=glfwSetErrorCallback(null);if(x!=null)x.free();
    }

    public static Display getActive() {
        return active;
    }

    // thrown in setup() when window creation fails
    public static class WindowCreationError extends Exception {
        private WindowCreationError(String error) {
            super(error);
        }
    }
}
