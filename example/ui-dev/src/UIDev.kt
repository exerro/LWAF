import lwaf_core.Display
import lwaf_core.GLView
import lwaf_core.vec2

open class Button: UINode() {
    open var colour by property("colour", this, Colour.white)

    override fun draw(context: DrawContext2D, position: vec2, size: vec2) {
        val rect = this
        context.draw {
            colour = rect.colour
            rectangle(position, size)
        }

        super.draw(context, position, size)
    }
}

fun main() {
    val main = Button(); main.colour = Colour.yellow
    val layout = FlowLayout()
    val a = Button(); a.parent = main; a.colour = Colour.blue
    val b = Button(); b.parent = main; b.colour = Colour.red
    val c = Button(); c.parent = main; c.colour = Colour.green
    val display = Display(720, 540, "Hello world", true)
    lateinit var view: GLView
    lateinit var context: DrawContext2D

    a.width = 30f
    a.height = 50f
    b.width = 50f
    b.height = 30f
    c.width = 40f
    c.height = 20f

    main.layout = layout
    layout.horizontalSpacing = 10f
    layout.verticalSpacing = 5f

    positionNode(main, 100f, 100f)

    display.attachLoadCallback {
        view = GLView(display.getWindowSize())
        context = DrawContext2D(view)
    }

    display.attachDrawCallback {
        main.draw(context, vec2(0f), vec2(100f))
    }

    display.run()
}
