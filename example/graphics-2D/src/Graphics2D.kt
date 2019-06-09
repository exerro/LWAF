import lwaf_core.*

fun main() {
    // create a 1080x720 display with title "Display" and vsync enabled
    val display = Display(1080, 720, "2D Graphics Display", true)
    // keep track of time
    var t = 0f
    // declare draw contexts for later use
    lateinit var context2D: DrawContext2D

    // attach a load callback, which will run when the display loads
    display.attachLoadCallback {
        // create the 2D context that we'll use for drawing later on
        context2D = DrawContext2D(GLView(display.getWindowSize()))
    }

    // attach a draw callback, which will run every frame
    display.attachDrawCallback {
        // push a new state
        context2D.push()

        context2D.lineWidth = 5f

        context2D.rotateAbout(vec2(70f), t)

        context2D.drawMode = DrawMode.Fill
        context2D.colour = vec3(0.9f, 0.6f, 0.3f)
        context2D.rectangle(vec2(20f), vec2(100f))

        context2D.drawMode = DrawMode.Line
        context2D.colour = vec3(0.3f, 0.6f, 0.9f)
        context2D.rectangle(vec2(20f), vec2(100f))
        // pop the state
        context2D.pop()

        context2D.circle(display.getMousePosition(), 50f + t * 5f)
    }

    display.attachUpdateCallback { dt ->
        t += dt
    }

    // attach a resize callback, which will run with the new width and height when the window is resized
    display.attachResizedCallback { _, _ ->
        // create new draw contexts
        context2D = DrawContext2D(GLView(display.getWindowSize()))
    }

    // run the display (and the whole application, in turn)
    // will create the window, set up OpenGL contexts, run the callbacks above etc
    display.run()
}
