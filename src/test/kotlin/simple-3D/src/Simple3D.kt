import lwaf_3D.Box
import lwaf_3D.DrawContext3D
import lwaf_3D.Sphere
import lwaf_3D.poly.toVAOObject3D
import lwaf_3D.property.rotateBy
import lwaf_3D.property.translateBy
import lwaf_core.*
import org.lwjgl.glfw.GLFW

fun main() {
    // create a 1080x720 display with title "Display" and vsync enabled
    val display = Display(1080, 720, "Display", true)
    // declare draw contexts for later use
    lateinit var context3D: DrawContext3D
    lateinit var context2D: DrawContext2D

    // attach a load callback, which will run when the display loads
    display.attachLoadCallback {
        // create the 2D and 3D contexts that we'll use for drawing later on
        context3D = DrawContext3D(GLView(display.getWindowSize()))
        context2D = DrawContext2D(GLView(display.getWindowSize()))
        // set the camera's projection matrix to a default perspective projection matrix
        context3D.camera.setPerspectiveProjection(display.getWindowAspectRatio())
    }

    // attach a draw callback, which will run every frame
    display.attachDrawCallback {
        // begin 3D rendering
        context3D.begin()
        // draw some shapes to the GBuffer (intermediate render target)
        context3D.drawToGBuffer(context3D.DEFAULT_SHADER,
                Sphere(1f).toVAOObject3D(5).translateBy(vec3(0f, 0f, -3f)),
                Box(vec3(0.8f)).toVAOObject3D().translateBy(vec3(0.9f, 0f, -3f))
        )
        // render the GBuffer to a texture using some lighting information given below
        context3D.directionalLight(vec3(-1f, -3f, -2f).normalise())
        context3D.ambientLight(0.1f, vec3(0f, 0f, 1f))
        // draw the rendered texture to the screen
        context2D.drawTexture(context3D.texture)
        // write the current FPS in the top left corner, using the default font but scaled down
        context2D.write("FPS: ${display.fps}", Font.DEFAULT_FONT.withHeight(30f), vec2(10f))
    }

    // attach a resize callback, which will run with the new width and height when the window is resized
    display.attachResizedCallback { _, _ ->
        // destroy the current 3D context (note, the 2D one need not be destroyed)
        context3D.destroy()
        // create new draw contexts, preserving the current context's camera
        context3D = DrawContext3D(GLView(display.getWindowSize()), context3D.camera)
        context2D = DrawContext2D(GLView(display.getWindowSize()))
        // update the camera's projection matrix to use the new aspect ratio
        context3D.camera.setPerspectiveProjection(display.getWindowAspectRatio())
    }

    // attach a mouse down callback
    display.attachMouseDownCallback { _, _ ->
        // lock the mouse cursor to within the window and disable it
        display.setMouseLocked(true)
    }

    // attach a mouse up callback
    display.attachMouseUpCallback { _, _ ->
        // unlock the mouse cursor from the window and enable it
        if (!display.isMouseDown()) display.setMouseLocked(false)
    }

    // attach a mouse drag callback
    display.attachMouseDragCallback { pos, last, _, _ ->
        // compute the difference in position (mouse movement)
        val d = pos - last
        // rotate the camera in the Y and X axis based on the movement
        context3D.camera.rotateBy(vec3(0f, -d.x / display.getWindowSize().x * 0.5f, 0f))
        context3D.camera.rotateBy(vec3(-d.y / display.getWindowSize().y * 0.5f, 0f, 0f))
    }

    // attach an update callback, which will run once a frame
    display.attachUpdateCallback { dt ->
        // create an initial translation vector that we'll change later on
        var translation = vec3(0f)
        // compute the forward/right direction vectors for the camera ("flat" keeps it on the same Y plane)
        val forward = context3D.camera.flatForward
        val right = context3D.camera.flatRight
        val speed = dt * 4

        // check for key presses and update the desired translation
        if (display.isKeyDown(GLFW.GLFW_KEY_W)) translation += forward
        if (display.isKeyDown(GLFW.GLFW_KEY_S)) translation -= forward
        if (display.isKeyDown(GLFW.GLFW_KEY_A)) translation -= right
        if (display.isKeyDown(GLFW.GLFW_KEY_D)) translation += right
        if (display.isKeyDown(GLFW.GLFW_KEY_SPACE)) translation += vec3(0f, 1f, 0f)
        if (display.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) translation -= vec3(0f, 1f, 0f)

        // move the camera by the translation * speed
        context3D.camera.translateBy(translation * speed)
    }

    // run the display (and the whole application, in turn)
    // will create the window, set up OpenGL contexts, run the callbacks above etc
    display.run()
}
