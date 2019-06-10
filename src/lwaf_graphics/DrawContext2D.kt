import lwaf_core.*
import lwaf_util.*
import org.lwjgl.opengl.GL11.*
import kotlin.math.atan2
import kotlin.math.max

fun DrawContext2D.rectangle(position: vec2, size: vec2) {
    if (drawMode == DrawMode.Line) {
        vao(rectangleVAO, mat4_translate(position.vec3(0f)) *
                mat3_scale(vec3(size.x, lineWidth, 1f)).mat4())

        vao(rectangleVAO, mat4_translate(position.vec3(0f)) *
                mat3_scale(vec3(lineWidth, size.y, 1f)).mat4())

        vao(rectangleVAO, mat4_translate(vec3(position.x, position.y + size.y - lineWidth, 0f)) *
                mat3_scale(vec3(size.x, lineWidth, 1f)).mat4())

        vao(rectangleVAO, mat4_translate(vec3(position.x + size.x - lineWidth, position.y, 0f)) *
                mat3_scale(vec3(lineWidth, size.y, 1f)).mat4())
    }
    else {
        vao(rectangleVAO, mat4_translate(position.vec3(0f)) *
                mat3_scale(size.vec3(1f)).mat4())
    }
}

fun DrawContext2D.circle(position: vec2, radius: Float = 5f) {
    if (drawMode == DrawMode.Fill) {
        vao(circleVAO(calculateCirclePoints(radius)), mat4_translate(position.vec3(0f)) *
                mat3_scale(radius).mat4())
    }
    else {
        TODO("line-mode circle rendering not yet implemented")
    }
}

fun DrawContext2D.convexPolygon(vararg points: vec2) {
    if (drawMode == DrawMode.Fill) {
        ConvexPolygon2D(points.toList()).toFanGLVAOs().map {
            vao(it, mat4_identity, GL_TRIANGLE_FAN)
        }
    }
    else {
        lines(*points)
        line(points.last(), points[0])
    }
}

fun DrawContext2D.polygon(vararg points: vec2) {
    if (drawMode == DrawMode.Fill) {
        Polygon2D(points.toList()).toFanGLVAOs().map {
            vao(it, mat4_identity, GL_TRIANGLE_FAN)
        }
    }
    else {
        push()

        TODO("kill me")
    }
}

fun DrawContext2D.line(a: vec2, b: vec2) {
    // hacky af but oh well
    push()
    drawMode = DrawMode.Fill
    rotateAbout(a, -(b - a).let { (dx, dy) -> atan2(dy, dx) })
    rectangle(a - vec2(lineWidth / 2f), vec2((b - a).length(), 0f) + vec2(lineWidth))
    pop()
}

fun DrawContext2D.lines(vararg points: vec2) {
    // also hacky: shouldn't draw rectangles for lines because that's dumb
    points.zip(points.drop(1)).map { (a, b) -> line(a, b) }
}

fun DrawContext2D.path(start: vec2, init: Path.() -> Unit) {
    val points = Path(start, init).computePoints()
    polygon(*Path(start, init).computePoints().toTypedArray())
}

fun DrawContext2D.draw(draw: DrawContext2D.() -> Unit) {
    draw(this)
}

open class DrawContext2D(protected val view: GLView) {
    protected val state = DrawState()
    protected val states: MutableList<DrawState> = mutableListOf()
    protected val activeState: DrawState
        get() = if (states.isNotEmpty()) states.last() else state
    protected val transform: mat4
        get() = mat4_identity *
                mat3_scale(vec3(1f, -1f, 1f)).mat4() *
                mat4_translate(vec3(-1f, -1f, 0f)) *
                mat3_scale(vec3(2 / view.size.x, 2 / view.size.y, 1f)).mat4() *
                mat4_translate(-vec3(stencil?.min?.x ?: 0f, stencil?.min?.y ?: 0f, 0f)) *
                states.fold(mat4_identity) { a, b -> a * b.transform }

    var drawMode: DrawMode
        get() = activeState.drawMode
        set(mode) { activeState.drawMode = mode }

    var colour: vec3
        get() = activeState.colour
        set(colour) { activeState.colour = colour }

    var lineWidth: Float
        get() = activeState.lineWidth
        set(width) { activeState.lineWidth = width }

    var stencil: AABB?
        get() = activeState.computeStencil()
        set(stencil) { activeState.stencil = stencil?.intersection(AABB(view.offset, view.offset + view.size)) }

    fun vao(vao: GLVAO, customTransform: mat4 = mat4_identity, mode: Int = GL_TRIANGLES) {
        drawTexturedVAO(null, vao, transform * customTransform, mode)
    }

    fun push() {
        states.add(DrawState(activeState))
    }

    fun pop() {
        if (states.isNotEmpty()) states.removeAt(states.size - 1)
    }

    fun translate(translation: vec2) {
        activeState.transform *= mat4_translate(translation.vec3(0f))
    }

    fun rotate(theta: Float) {
        activeState.transform *= mat3_rotate(theta, vec3(0f, 0f, -1f)).mat4()
    }

    fun rotateAbout(position: vec2, theta: Float) {
        translate(position)
        rotate(theta)
        translate(-position)
    }

    fun scale(scale: vec2) {
        activeState.transform *= mat3_scale(scale.vec3(1f)).mat4()
    }

    fun scale(scale: Float) {
        activeState.transform *= mat3_scale(vec3(scale, scale, 1f)).mat4()
    }

    private fun drawTexturedVAO(texture: GLTexture?, vao: GLVAO, transform: mat4, mode: Int = GL_TRIANGLES) {
        val s = stencil
        (if (s != null) GLView(view.offset + vec2(s.min.x, view.size.y - s.max.y), s.max - s.min) else view).setViewport()
        texture?.bind()
        shaderProgram2D.setUniform("transform", transform)
        shaderProgram2D.setUniform("colour", activeState.colour)
        shaderProgram2D.setUniform("useTexture", texture != null)
        shaderProgram2D.start()
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        vao.load()
        glDrawElements(mode, vao.vertexCount, GL_UNSIGNED_INT, 0)
        vao.unload()
        shaderProgram2D.stop()
        texture?.unbind()
    }

    private var shaderProgram2D: GLShaderProgram = loadShaderProgram("#version 400 core\n" +
            "\n" +
            "// model attributes\n" +
            "layout (location=0) in vec3 vertex;\n" +
            "layout (location=1) in vec2 vertex_uv;\n" +
            "layout (location=3) in vec3 vertex_colour;\n" +
            "\n" +
            "out vec3 fragment_colour;\n" +
            "out vec2 fragment_uv;\n" +
            "\n" +
            "uniform mat4 transform;\n" +
            "\n" +
            "void main(void) {\n" +
            "\tgl_Position = transform * vec4(vertex, 1);\n" +
            "    fragment_colour = vertex_colour;\n" +
            "    fragment_uv = vertex_uv;\n" +
            "}", "#version 400 core\n" +
            "\n" +
            "in vec3 fragment_colour;\n" +
            "in vec2 fragment_uv;\n" +
            "\n" +
            "uniform sampler2D textureSampler;\n" +
            "uniform vec3 colour = vec3(1, 1, 1);\n" +
            "uniform bool useTexture = false;\n" +
            "\n" +
            "void main(void) {\n" +
            "    gl_FragColor = vec4(colour * fragment_colour, 1.0);\n" +
            "    if (useTexture) gl_FragColor *= texture(textureSampler, fragment_uv);\n" +
            "}", false)

    protected class DrawState(val parent: DrawState? = null) {
        var transform: mat4 = mat4_identity
        var colour: vec3 = parent?.colour ?: vec3(1f)
        var drawMode: DrawMode = parent?.drawMode ?: DrawMode.Fill
        var lineWidth: Float = parent?.lineWidth ?: 1f
        var stencil: AABB? = null

        fun computeStencil(): AABB?
            = if (stencil == null) parent?.computeStencil() else {
                val s = stencil!!
                val parentStencil = parent?.computeStencil()
                if (parentStencil == null) stencil else s intersection parentStencil
            }
    }
}

private val circleCache = LinkedHashMap<Int, GLVAO>()
private val MAX_CACHE_SIZE = 15

private fun calculateCirclePoints(radius: Float)
    = max((2 * radius).toInt(), 3)

private fun circleVAO(numPoints: Int): GLVAO {
    if (circleCache.size >= MAX_CACHE_SIZE) {
        for ((k, _) in circleCache) {
            circleCache.remove(k)
            break
        }
    }

    return circleCache.computeIfAbsent(numPoints) {
        generateStandardVAO(
                (listOf(vec3(0f)) + (0 until numPoints).map { i -> mat3_rotate(i / numPoints.toFloat() * Math.PI.toFloat() * 2, vec3(0f, 0f, -1f)) * vec3(1f, 0f, 0f) }) .toTypedArray(),
                Array(numPoints + 1) { vec3(0f, 0f, 1f) },
                (1 .. numPoints).flatMap { i -> listOf(0, i, i % numPoints + 1) } .toIntArray()
        )
    }
}

private var rectangleVAO: GLVAO = generateStandardVAO(
        arrayOf(
                vec3(0f, 1f, 0f),
                vec3(0f, 0f, 0f),
                vec3(1f, 0f, 0f),
                vec3(1f, 1f, 0f)
        ),
        Array(4) { vec3(0f, 0f, 1f) },
        intArrayOf(2, 1, 0, 3, 2, 0),
        Array(4) { vec3(1f) },
        arrayOf(
                vec2(0f, 1f),
                vec2(0f, 0f),
                vec2(1f, 0f),
                vec2(1f, 1f)
        )
)

sealed class DrawMode {
    object Fill : DrawMode()
    object Line : DrawMode()
}
