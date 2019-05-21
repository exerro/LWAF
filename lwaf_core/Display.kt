package lwaf_core

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL

const val DISPLAY_FPS_READINGS = 15
const val DISPLAY_DEFAULT_WIDTH = 1080
const val DISPLAY_DEFAULT_HEIGHT = 720
const val DISPLAY_DEFAULT_TITLE = "Display"

class Display(
        private var width: Int = DISPLAY_DEFAULT_WIDTH,
        private var height: Int = DISPLAY_DEFAULT_HEIGHT,
        private val title: String = DISPLAY_DEFAULT_TITLE
) : GLResource {
    private var running = false
    private val heldMouseButtons = HashSet<Int>()
    private var lastMousePosition = vec2(0f, 0f)
    private var firstMousePosition = vec2(0f, 0f)
    private var onSetup: MutableList<() -> Unit> = ArrayList()
    private var setup = false
    private var window: Long = 0
    private var internalFPS = 0

    private val onResizedCallbacks: MutableList<((Int, Int) -> Unit)> = ArrayList()
    private val onMouseDownCallbacks: MutableList<((vec2, Int) -> Unit)> = ArrayList()
    private val onMouseUpCallbacks: MutableList<((vec2, Int) -> Unit)> = ArrayList()
    private val onMouseMoveCallbacks: MutableList<((vec2, vec2) -> Unit)> = ArrayList()
    private val onMouseDragCallbacks: MutableList<((vec2, vec2, vec2, Set<Int>) -> Unit)> = ArrayList()
    private val onKeyPressedCallbacks: MutableList<((Int, Int) -> Unit)> = ArrayList()
    private val onKeyReleasedCallbacks: MutableList<((Int, Int) -> Unit)> = ArrayList()
    private val onTextInputCallbacks: MutableList<((String) -> Unit)> = ArrayList()
    private val onUpdateCallbacks: MutableList<((Float) -> Unit)> = ArrayList()
    private val onDrawCallbacks: MutableList<(() -> Unit)> = ArrayList()
    private val onLoadCallbacks: MutableList<(() -> Unit)> = ArrayList()
    private val onUnloadCallbacks: MutableList<(() -> Unit)> = ArrayList()

    fun attachResizedCallback(callback: (Int, Int) -> Unit) {
        onResizedCallbacks.add(callback)
    }

    fun attachMouseDownCallback(callback: (vec2, Int) -> Unit) {
        onMouseDownCallbacks.add(callback)
    }

    fun attachMouseUpCallback(callback: (vec2, Int) -> Unit) {
        onMouseUpCallbacks.add(callback)
    }

    fun attachMouseMoveCallback(callback: (vec2, vec2) -> Unit) {
        onMouseMoveCallbacks.add(callback)
    }

    fun attachMouseDragCallback(callback: (vec2, vec2, vec2, Set<Int>) -> Unit) {
        onMouseDragCallbacks.add(callback)
    }

    fun attachKeyPressedCallback(callback: (Int, Int) -> Unit) {
        onKeyPressedCallbacks.add(callback)
    }

    fun attachKeyReleasedCallback(callback: (Int, Int) -> Unit) {
        onKeyReleasedCallbacks.add(callback)
    }

    fun attachTextInputCallback(callback: (String) -> Unit) {
        onTextInputCallbacks.add(callback)
    }

    fun attachUpdateCallback(callback: (Float) -> Unit) {
        onUpdateCallbacks.add(callback)
    }

    fun attachDrawCallback(callback: () -> Unit) {
        onDrawCallbacks.add(callback)
    }

    fun attachLoadCallback(callback: () -> Unit) {
        onLoadCallbacks.add(callback)
    }

    fun attachUnloadCallback(callback: () -> Unit) {
        onUnloadCallbacks.add(callback)
    }

    fun detachResizedCallback(callback: (Int, Int) -> Unit) {
        onResizedCallbacks.remove(callback)
    }

    fun detachMouseDownCallback(callback: (vec2, Int) -> Unit) {
        onMouseDownCallbacks.remove(callback)
    }

    fun detachMouseUpCallback(callback: (vec2, Int) -> Unit) {
        onMouseUpCallbacks.remove(callback)
    }

    fun detachMouseMoveCallback(callback: (vec2, vec2) -> Unit) {
        onMouseMoveCallbacks.remove(callback)
    }

    fun detachMouseDragCallback(callback: (vec2, vec2, vec2, Set<Int>) -> Unit) {
        onMouseDragCallbacks.remove(callback)
    }

    fun detachKeyPressedCallback(callback: (Int, Int) -> Unit) {
        onKeyPressedCallbacks.remove(callback)
    }

    fun detachKeyReleasedCallback(callback: (Int, Int) -> Unit) {
        onKeyReleasedCallbacks.remove(callback)
    }

    fun detachTextInputCallback(callback: (String) -> Unit) {
        onTextInputCallbacks.remove(callback)
    }

    fun detachUpdateCallback(callback: (Float) -> Unit) {
        onUpdateCallbacks.remove(callback)
    }

    fun detachDrawCallback(callback: () -> Unit) {
        onDrawCallbacks.remove(callback)
    }

    fun detachLoadCallback(callback: () -> Unit) {
        onLoadCallbacks.remove(callback)
    }

    fun detachUnloadCallback(callback: () -> Unit) {
        onUnloadCallbacks.remove(callback)
    }

    val FPS get() = internalFPS

    fun setMouseLocked(locked: Boolean) {
        whenSetup {
            glfwSetInputMode(window, GLFW_CURSOR, if (locked) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL)
        }
    }

    /**
     * Return true if the key given is currently held
     */
    fun isKeyDown(key: Int): Boolean = glfwGetKey(window, key) == GLFW_PRESS

    /**
     * Return true if the mouse button given is currently held
     */
    fun isMouseDown(button: Int): Boolean = glfwGetMouseButton(window, button) == GLFW_PRESS

    /**
     * Return true if any mouse button is currently held
     */
    fun isMouseDown(): Boolean = heldMouseButtons.isNotEmpty()

    /**
     * Gets the mouse position
     */
    fun getMousePosition(): vec2 {
        val w = DoubleArray(1)
        val h = DoubleArray(1)
        glfwGetCursorPos(window, w, h)
        return vec2(w[0].toFloat(), h[0].toFloat())
    }

    /**
     * Gets the size of the window
     */
    fun getWindowSize(): vec2 {
        val w = IntArray(1)
        val h = IntArray(1)
        glfwGetWindowSize(window, w, h)
        return vec2(w[0].toFloat(), h[0].toFloat())
    }

    /**
     * Closes the display
     */
    fun close() {
        running = false
    }

    fun run() {
        var lastUpdate = System.currentTimeMillis()
        var fpsReadings: List<Float> = ArrayList()

        setup()
        running = true

        // run the rendering loop until the user has attempted to close the window or has pressed the ESCAPE key
        while (running && !glfwWindowShouldClose(window)) {
            updateGLViewport()
            // set the clear colour to black and clear the framebuffer
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            val t = System.currentTimeMillis()
            val dt = (t - lastUpdate).toFloat()
            lastUpdate = t

            // calculate the FPS based on past FPS readings
            fpsReadings = (fpsReadings + listOf(1000/dt)).take(DISPLAY_FPS_READINGS)
            internalFPS = (fpsReadings.fold(dt) { acc, it -> acc + it } / (fpsReadings.size + 1)).toInt()

            // call the update render callbacks
            onUpdateCallbacks.map { it(dt / 1000f) }
            onDrawCallbacks.map { it() }

            // swap the color buffers to present the content to the screen
            glfwSwapBuffers(window)
            glFlush()

            // poll for window events
            // the key callback above will only be invoked during this call
            glfwPollEvents()
        }

        onUnloadCallbacks.map { it() }

        destroy()
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free()
    }

    override fun destroy() {
        // free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)
    }

    /**
     * Creates a new display, using the standard GLFW init code found at https://www.lwjgl.org/guide
     */
    private fun setup() {
        GLFWErrorCallback.createPrint(System.err).set()

        // initialise GLFW
        if(!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_TRUE) // the window will focus when shown
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
        glfwWindowHint(GLFW_CENTER_CURSOR , GLFW_TRUE) // the window will have the cursor centred

        window = glfwCreateWindow(width, height, title, NULL, NULL) // create the window
        if (window == NULL) throw RuntimeException("Failed to create the GLFW window")

        val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        if (videoMode != null)
            glfwSetWindowPos(
                    window,
                    (videoMode.width() - getWindowSize().x.toInt()) / 2,
                    (videoMode.height() - getWindowSize().y.toInt()) / 2
            )

        glfwMakeContextCurrent(window) // make the OpenGL context current
//        glfwSwapInterval(1) // enable v-sync
        glfwSwapInterval(0) // disable v-sync
        glfwShowWindow(window) // make the window visible

        GL.createCapabilities() // makes OpenGL bindings available to use from LWJGL

        setCallbacks() // sets the GLFW callbacks to use the custom callbacks above
        updateGLViewport() // update the GL viewport to set it up initially

        onLoadCallbacks.map { it() } // call the loaded callbacks
        onSetup.map { it() }
        setup = true
    }

    private fun setCallbacks() {
        // on window resize, update the GL viewport and call a resized callback, if set
        glfwSetWindowSizeCallback(window) { _, _, _ ->
            updateGLViewport()
            onResizedCallbacks.map { it(width, height) }
        }

        glfwSetKeyCallback(window) { _, key, _, action, mods ->
            if (action == GLFW_PRESS) onKeyPressedCallbacks.map { it(key, mods) }
            if (action == GLFW_RELEASE) onKeyReleasedCallbacks.map { it(key, mods) }
        }

        glfwSetCharCallback(window) { _, codepoint ->
            onTextInputCallbacks.map { it(Character.toChars(codepoint).toString()) }
        }

        glfwSetCursorPosCallback(window) { _, _, _ ->
            val pos = getMousePosition()
            if (heldMouseButtons.isEmpty()) onMouseMoveCallbacks.map { it(pos, lastMousePosition) }
            else onMouseDragCallbacks.map { it(pos, lastMousePosition, firstMousePosition, heldMouseButtons) }
            lastMousePosition = pos
        }

        glfwSetMouseButtonCallback(window) { _, button, action, mods ->
            val pos = getMousePosition()

            if (action == GLFW_PRESS) {
                if (heldMouseButtons.isEmpty()) firstMousePosition = pos
                heldMouseButtons.add(button)
                onMouseDownCallbacks.map { it(pos, mods) }
            }
            else if (action == GLFW_RELEASE) {
                heldMouseButtons.remove(button)
                onMouseUpCallbacks.map { it(pos, mods) }
            }
        }
    }

    private fun updateGLViewport() {
        val width = IntArray(1)
        val height = IntArray(1)
        glfwGetFramebufferSize(window, width, height)
        this.width = width[0]
        this.height = height[0]
    }

    private fun whenSetup(func: () -> Unit) {
        if (setup) func()
        else onSetup.add(func)
    }
}