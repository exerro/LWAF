package lwaf_core

import org.lwjgl.opengl.GL11.*

object Draw2D: GLResource{
    private lateinit var rectangleVAO: GLVAO
    private lateinit var shaderProgram2D: GLShaderProgram
    private var colour = vec3(1f, 1f, 1f)
    private val viewportSizes = ArrayList<vec2>()
    private val viewportSize: vec2
        get() = viewportSizes[viewportSizes.size - 1]

    internal fun setViewport(size: vec2) {
        viewportSizes.clear()
        viewportSizes.add(size)
    }

    internal fun pushViewport(size: vec2) {
        viewportSizes.add(size)
        glViewport(0, 0, size.x.toInt(), size.y.toInt())
    }

    internal fun popViewport() {
        viewportSizes.removeAt(viewportSizes.size - 1)
        glViewport(0, 0, viewportSize.x.toInt(), viewportSize.y.toInt())
    }

    fun setColour(r: Float, g: Float, b: Float) {
        colour = vec3(r, g, b)
    }

    fun rectangle(position: vec2, size: vec2) {
        val displaySize = viewportSize
        val transform = mat4_identity
                .scaleBy(vec3(1f, -1f, 1f))
                .translateBy(vec3(-1f, -1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .translateBy(vec3(position.x, position.y, 0f))
                .scaleBy(size.vec3(1f))

        draw2D(null, rectangleVAO, transform)
    }

    @JvmOverloads
    fun texture(texture: GLTexture, offset: vec2 = vec2(0f, 0f), scale: vec2 = vec2(1f, 1f)) {
        val displaySize = viewportSize
        val transform = mat4_identity
                .scaleBy(vec3(1f, -1f, 1f))
                .translateBy(vec3(-1f, -1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .translateBy(vec3(offset.x, offset.y, 0f))
                .scaleBy(vec3(texture.width.toFloat(), texture.height.toFloat(), 1f))
                .scaleBy(vec3(scale.x, scale.y, 1f))
                .translateBy(vec3(0f, 1f, 0f))
                .scaleBy(vec3(1f, -1f, 1f))

        draw2D(texture, rectangleVAO, transform)
    }

    @JvmOverloads
    fun image(texture: GLTexture, offset: vec2 = vec2(0f, 0f), scale: vec2 = vec2(1f, 1f)) {
        val displaySize = viewportSize
        val transform = mat4_identity
                .scaleBy(vec3(1f, -1f, 1f))
                .translateBy(vec3(-1f, -1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .translateBy(vec3(offset.x, offset.y, 0f))
                .scaleBy(vec3(texture.width.toFloat(), texture.height.toFloat(), 1f))
                .scaleBy(vec3(scale.x, scale.y, 1f))

        draw2D(texture, rectangleVAO, transform)
    }

        fun text(text: Text, position: vec2) {
            val displaySize = viewportSize
            val transform   = mat4_identity
                    .translateBy(vec3(-1f, 1f, 0f))
                    .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                    .scaleBy(vec3(1f, -1f, 0f))
                    .translateBy(vec3(position.x, position.y, 0f))

            draw2D(text.font.texture, text.vao, transform)
        }

    private fun draw2D(texture: GLTexture?, vao: GLVAO, transform: mat4) {
        texture?.bind()

        shaderProgram2D.setUniform("transform", transform)
        shaderProgram2D.setUniform("colour", colour)
        shaderProgram2D.setUniform("useTexture", texture != null)
        shaderProgram2D.start()
        drawIndexedVAO(vao)
        shaderProgram2D.stop()

        texture?.unbind()
    }

    fun drawIndexedVAO(vao: GLVAO) {
        vao.load()
        glDrawElements(GL_TRIANGLES, vao.vertexCount, GL_UNSIGNED_INT, 0)
        vao.unload()
    }

    fun init() {
        shaderProgram2D = loadShaderProgram("#version 400 core\n" +
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

        rectangleVAO = object : GLVAO() {
            init {
                vertexCount = 6

                genVertexBuffer(floatArrayOf(0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f))
                genNormalBuffer(floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f))
                genColourBuffer(4)
                genElementBuffer(intArrayOf(2, 1, 0, 3, 2, 0))
                genUVBuffer(floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f))
            }
        }
    }

    override fun destroy() {
        rectangleVAO.destroy()
        shaderProgram2D.destroy()
    }
}