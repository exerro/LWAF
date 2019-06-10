import lwaf_core.*
import lwaf_util.ConvexPolygon2D
import lwaf_util.Polygon2D
import lwaf_util.toFanGLVAOs
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

        val poly = Polygon2D(points.toList())

        val p = ArrayList(poly.points)
        val cpoints = mutableListOf<List<Int>>()
        var n = 0

        while (p.size > 3) {
            val triple = (0 until p.size).map { i ->
                val a = i
                val b = (i + 1) % p.size
                val c = (i + 2) % p.size
                val d = (i + 3) % p.size
                listOf(a, b, c, d)
            } .filter { (a, b, c, _) ->
                val ab = poly.vertices[p[b]] - poly.vertices[p[a]]
                val bc = poly.vertices[p[c]] - poly.vertices[p[b]]
                val abR = ab.rotate90CW()
                (abR dot bc) >= 0
            } .filter { (a, b, c, d) ->
                val ca = poly.vertices[p[a]] - poly.vertices[p[c]]
                val bc = poly.vertices[p[c]] - poly.vertices[p[b]]
                val cd = poly.vertices[p[d]] - poly.vertices[p[c]]
                val caR = ca.rotate90CW()
                val bcR = bc.rotate90CW()

                caR dot cd < 0 || bcR dot cd < 0
            } .filter { (a, _, c, _) ->
                val sA = poly.vertices[p[a]]
                val sC = poly.vertices[p[c]]

                !(0 until p.size).map {
                    Pair(p[it], p[(it + 1) % p.size])
                } .filter { (aa, bb) ->
                    (aa != p[a] || bb != p[c]) && (aa != p[c] || bb != p[a])
                } .map { (aa, bb) ->
                    Pair(poly.vertices[aa], poly.vertices[bb])
                } .any { (aa, bb) ->
                    val dA = sA - aa
                    val dC = sC - aa
                    val nAA = (bb - aa).rotate90CCW()
                    val ndA = (dC - dA).rotate90CCW()
                    (dA dot nAA) * (dC dot nAA) < 0 && (aa dot ndA) * (bb dot ndA) < 0
                }
            } .first() .let { (a, b, c, _) ->
                Triple(p[a], p[b], p[c])
            }

            if (n == 0) {
                colour = Colour.white
                convexPolygon(*p.map { poly.vertices[it] }.toTypedArray())
            }

            cpoints.add(listOf(triple.first, triple.second, triple.third))
            if (n-- <= 0) {
                colour = Colour.green
                line(poly.vertices[triple.first], poly.vertices[triple.third])
                colour = Colour.red
                drawMode = DrawMode.Fill
                circle(poly.vertices[triple.second])
            }
            p.remove(triple.second)
        }

        cpoints.add(p)

        cpoints.map { points ->
//            convexPolygon(*points.map { poly.vertices[it] } .toTypedArray())
        }
        pop()
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

    if (drawMode == DrawMode.Line) {
        lines(*points.toTypedArray())
    }
    else {
        polygon(*points.toTypedArray())
    }
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
        view.setViewport()
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

    protected class DrawState(parent: DrawState? = null) {
        var transform: mat4 = mat4_identity
        var colour: vec3 = parent?.colour ?: vec3(1f)
        var drawMode: DrawMode = parent?.drawMode ?: DrawMode.Fill
        var lineWidth: Float = parent?.lineWidth ?: 1f
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
