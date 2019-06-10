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
        context2D.push()
            context2D.rotateAbout(vec2(70f), t)
            context2D.lineWidth = 5f
            context2D.drawMode = DrawMode.Fill
            context2D.colour = vec3(0.9f, 0.6f, 0.3f)
            context2D.rectangle(vec2(20f), vec2(100f))
            context2D.drawMode = DrawMode.Line
            context2D.colour = vec3(0.3f, 0.6f, 0.9f)
            context2D.rectangle(vec2(20f), vec2(100f))
        context2D.pop()

        context2D.push()
            listOf(
                    Colour.red, Colour.pink, Colour.purple,
                    Colour.deepPurple, Colour.indigo, Colour.blue,
                    Colour.lightBlue, Colour.cyan, Colour.teal,
                    Colour.green, Colour.lightGreen, Colour.lime,
                    Colour.yellow, Colour.amber, Colour.orange,
                    Colour.deepOrange, Colour.brown, Colour.blueGrey,
                    Colour.lightGrey, Colour.grey, Colour.darkGrey
            ).mapIndexed { i, c ->
                context2D.colour = c
                context2D.rectangle(vec2(i * 20f, 0f), vec2(20f, 100f))
            }
        context2D.pop()

        context2D.push()
            context2D.drawMode = DrawMode.Line
            context2D.lineWidth = 5f
            context2D.path(vec2(100f, 100f)) {
                lineTo(300f, 200f)
                curveTo(300f, 300f) {
                    controlPoint(500f, 100f)
                    controlPoint(400f, 200f)
                }
                close()
            }
        context2D.pop()

        context2D.push()
            context2D.drawMode = DrawMode.Line
            context2D.lineWidth = 3f
            context2D.path(vec2(500f, 500f)) {
                curveTo(600f, 600f) { controlPoint(575f, 525f) }
                curveTo(500f, 700f) { controlPoint(575f, 675f) }
                curveTo(400f, 600f) { controlPoint(425f, 675f) }
                curveTo(500f, 500f) { controlPoint(425f, 525f) }
                close()
            }
        context2D.pop()
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
