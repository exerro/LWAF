package lwaf_core

class Text(val text: String, val font: Font) {
    val vao: GLVAO = GLVAO()

    init {
        initialiseVAO()
    }

    private fun initialiseVAO() {
        val vertices = FloatArray(text.length * 12)
        val uvs = FloatArray(text.length * 8)
        val elements = IntArray(text.length * 6)

        val base = font.getBase()
        val lineHeight = font.getLineHeight()
        var ei = 0
        var x = 0
        val y = lineHeight - base

        for (i in 0 until text.length) {
            val c = text[i]
            val vi = i * 12
            val uvi = i * 8

            val width = font.getCharAdvance(c)
            val size = font.getCharSize(c)
            val offset = font.getCharOffset(c)
            val cuvs = font.getCharUVPositions(c)

            uvs[uvi] = cuvs[0].x
            uvs[uvi + 1] = cuvs[0].y
            uvs[uvi + 2] = cuvs[1].x
            uvs[uvi + 3] = cuvs[1].y
            uvs[uvi + 4] = cuvs[2].x
            uvs[uvi + 5] = cuvs[2].y
            uvs[uvi + 6] = cuvs[3].x
            uvs[uvi + 7] = cuvs[3].y

            vertices[vi] = x + offset.x
            vertices[vi + 1] = y - offset.y
            vertices[vi + 2] = 0f
            vertices[vi + 3] = x + offset.x
            vertices[vi + 4] = y - offset.y + size.y
            vertices[vi + 5] = 0f
            vertices[vi + 6] = x.toFloat() + offset.x + size.x
            vertices[vi + 7] = y - offset.y + size.y
            vertices[vi + 8] = 0f
            vertices[vi + 9] = x.toFloat() + offset.x + size.x
            vertices[vi + 10] = y - offset.y
            vertices[vi + 11] = 0f

            x += width.toInt()
        }

        var i = 0
        while (i < text.length) {
            elements[ei] = 4 * i
            elements[ei + 1] = 4 * i + 1
            elements[ei + 2] = 4 * i + 2
            elements[ei + 3] = 4 * i
            elements[ei + 4] = 4 * i + 2
            elements[ei + 5] = 4 * i + 3
            ++i
            ei += 6
        }

        vao.vertexCount = elements.size
        vao.genVertexBuffer(vertices)
        vao.genColourBuffer(vertices.size / 3)
        vao.genUVBuffer(uvs)
        vao.genElementBuffer(elements)
    }

}
