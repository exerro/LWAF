import lwaf_core.*
import lwaf_util.AABB
import org.lwjgl.opengl.GL11.*
import kotlin.math.sin

fun main() {
    // create a 1080x720 display with title "Display" and vsync enabled
    val display = Display(1080, 720, "2D Graphics Display", true)
    val controls = mutableListOf<vec2>()
    // keep track of time
    var t = 0f
    // declare draw contexts for later use
    lateinit var context2D: DrawContext2D
    lateinit var view: GLView

    // attach a load callback, which will run when the display loads
    display.attachLoadCallback {
        // create the 2D context that we'll use for drawing later on
        view = GLView(display.getWindowSize())
        context2D = DrawContext2D(view)

        glHint(GL_POLYGON_SMOOTH, GL_NICEST)
    }

    display.attachMouseDownCallback { pos, _ ->
        controls.add(pos)
    }

    // attach a draw callback, which will run every frame
    display.attachDrawCallback {
        context2D.draw {
            listOf(
                    Colour.red, Colour.pink, Colour.purple,
                    Colour.deepPurple, Colour.indigo, Colour.blue,
                    Colour.lightBlue, Colour.cyan, Colour.teal,
                    Colour.green, Colour.lightGreen, Colour.lime,
                    Colour.yellow, Colour.amber, Colour.orange,
                    Colour.deepOrange, Colour.brown, Colour.blueGrey,
                    Colour.lightGrey, Colour.grey, Colour.darkGrey
            ).mapIndexed { i, c ->
                colour = c
                rectangle(vec2(i * 20f, 0f), vec2(20f, 80f))
            }

            colour = Colour.white
            circle(display.getMousePosition(), (sin(t * 4) + 2) * 10)
        }
    }

    display.attachUpdateCallback { dt ->
        t += dt
    }

    // attach a resize callback, which will run with the new width and height when the window is resized
    display.attachResizedCallback { _, _ ->
        // create a new draw context
        view = GLView(display.getWindowSize())
        context2D = DrawContext2D(view)
    }

    // run the display (and the whole application, in turn)
    // will create the window, set up OpenGL contexts, run the callbacks above etc
    display.run()
}
