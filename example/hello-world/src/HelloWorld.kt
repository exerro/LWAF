import lwaf_core.*

fun main() {
    // create a 1080x720 display with title "Display" and vsync enabled
    val display = Display(720, 540, "Hello World Display", true)
    // declare draw contexts for later use
    lateinit var context2D: DrawContext2D

    // attach a load callback, which will run when the display loads
    display.attachLoadCallback {
        // create the 2D context that we'll use for drawing later on
        context2D = DrawContext2D(GLView(display.getWindowSize()))
    }

    // attach a draw callback, which will run every frame
    display.attachDrawCallback {
        // text to draw
        val text = "Hello world"
        // font to use to draw text (the withHeight() method is used for scaling pre-loaded fonts)
        val font = Font.DEFAULT_FONT.withHeight(48f)
        // calculate central position for the text to draw
        val center = (display.getWindowSize() - vec2(font.getWidth(text), font.height)) / 2f
        // set a blueish draw colour
        context2D.setColour(0.3f, 0.6f, 1f)
        // draw a rectangle (behind the text)
        context2D.drawRectangle(center + vec2(-10f, -15f), vec2(font.getWidth(text), font.height) + vec2(20f))
        // set the colour back to white
        context2D.setColour(1f)
        // write the current FPS in the top left corner, using the default font but scaled down
        context2D.write("FPS: ${display.fps}", Font.DEFAULT_FONT.withHeight(28f), vec2(10f))
        // write the text at the central position
        context2D.write(text, font, center)
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
