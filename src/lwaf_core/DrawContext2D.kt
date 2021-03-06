package lwaf_core

import org.lwjgl.opengl.GL11.*

@Suppress("unused")
class DrawContext2D(private val view: GLView) {
    private var colour = vec3(1f, 1f, 1f)

    fun setColour(r: Float, g: Float = r, b: Float = g) {
        colour = vec3(r, g, b)
    }

    fun drawRectangle(position: vec2, size: vec2) {
        val displaySize = view.size
        val transform = mat4_identity
                .scaleBy(vec3(1f, -1f, 1f))
                .translateBy(vec3(-1f, -1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .translateBy(vec3(position.x, position.y, 0f))
                .scaleBy(size.vec3(1f))

        drawTexturedVAO(null, rectangleVAO, transform)
    }

    fun write(text: String, font: Font, position: vec2 = vec2(0f, 0f)) {
        write(font.getTextObject(text), position)
    }

    fun write(text: FontText, position: vec2 = vec2(0f, 0f)) {
        val displaySize = view.size
        val transform   = mat4_identity
                .translateBy(vec3(-1f, 1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .scaleBy(vec3(1f, -1f, 0f))
                .translateBy(vec3(position.x, position.y, 0f))

        drawTexturedVAO(text.font.texture, text.vao, transform)
    }

    fun drawImage(texture: GLTexture, position: vec2 = vec2(0f, 0f), scale: vec2 = vec2(1f, 1f)) {
        val displaySize = view.size
        val transform   = mat4_identity
                .scaleBy(vec3(1f, -1f, 1f))
                .translateBy(vec3(-1f, -1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .translateBy(vec3(position.x, position.y, 0f))
                .scaleBy(vec3(texture.width.toFloat(), texture.height.toFloat(), 1f))
                .scaleBy(vec3(scale.x, scale.y, 1f))

        drawTexturedVAO(texture, rectangleVAO, transform)
    }

    fun drawTexture(texture: GLTexture, position: vec2 = vec2(0f, 0f), scale: vec2 = vec2(1f, 1f)) {
        val displaySize = view.size
        val transform   = mat4_identity
                .scaleBy(vec3(1f, -1f, 1f))
                .translateBy(vec3(-1f, -1f, 0f))
                .scaleBy(vec3(2 / displaySize.x, 2 / displaySize.y, 1f))
                .translateBy(vec3(position.x, position.y, 0f))
                .scaleBy(vec3(texture.width.toFloat(), texture.height.toFloat(), 1f))
                .scaleBy(vec3(scale.x, scale.y, 1f))
                .translateBy(vec3(0f, 1f, 0f))
                .scaleBy(vec3(1f, -1f, 1f))

        drawTexturedVAO(texture, rectangleVAO, transform)
    }

    private fun drawTexturedVAO(texture: GLTexture?, vao: GLVAO, transform: mat4) {
        view.setViewport()
        texture?.bind()
        shaderProgram2D.setUniform("transform", transform)
        shaderProgram2D.setUniform("colour", colour)
        shaderProgram2D.setUniform("useTexture", texture != null)
        shaderProgram2D.start()
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        vao.load()
        glDrawElements(GL_TRIANGLES, vao.vertexCount, GL_UNSIGNED_INT, 0)
        vao.unload()
        shaderProgram2D.stop()
        texture?.unbind()
    }

    private var rectangleVAO: GLVAO = object : GLVAO() {
        init {
            vertexCount = 6

            genVertexBuffer(floatArrayOf(0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f))
            genNormalBuffer(floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f))
            genColourBuffer(4)
            genElementBuffer(intArrayOf(2, 1, 0, 3, 2, 0))
            genUVBuffer(floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 1f, 1f))
        }
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


    private fun mat4.translateBy(translation: vec3): mat4 = this * mat4_translate(translation)
    private fun mat4.scaleBy(scale: vec3): mat4 = this * mat3_scale(scale).mat4()
}
