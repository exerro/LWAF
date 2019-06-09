import lwaf_core.GLVAO

class LegacyCylinderVAO(detail: Int) : GLVAO() {
    init {
        if (detail < 3)
            throw IllegalArgumentException("Detail is too low for cylinder construction ($detail)")

        val vertices = FloatArray(detail * 12 + 6)
        val normals = FloatArray(detail * 12 + 6)
        val elements = IntArray(detail * 12)

        for (i in 0 until detail) {
            val theta = (i.toFloat() / detail).toDouble() * 2.0 * Math.PI
            val sin = Math.sin(theta).toFloat()
            val cos = Math.cos(theta).toFloat()

            vertices[i * 3] = sin / 2
            vertices[i * 3 + 1] = 0.5f
            vertices[i * 3 + 2] = cos / 2

            vertices[detail * 3 + i * 3] = sin / 2
            vertices[detail * 3 + i * 3 + 1] = -0.5f
            vertices[detail * 3 + i * 3 + 2] = cos / 2

            vertices[detail * 6 + i * 3] = sin / 2
            vertices[detail * 6 + i * 3 + 1] = 0.5f
            vertices[detail * 6 + i * 3 + 2] = cos / 2

            vertices[detail * 9 + i * 3] = sin / 2
            vertices[detail * 9 + i * 3 + 1] = -0.5f
            vertices[detail * 9 + i * 3 + 2] = cos / 2

            normals[i * 3] = sin
            normals[i * 3 + 1] = 0f
            normals[i * 3 + 2] = cos

            normals[detail * 3 + i * 3] = sin
            normals[detail * 3 + i * 3 + 1] = 0f
            normals[detail * 3 + i * 3 + 2] = cos

            normals[detail * 6 + i * 3] = 0f
            normals[detail * 6 + i * 3 + 1] = 1f
            normals[detail * 6 + i * 3 + 2] = 0f

            normals[detail * 9 + i * 3] = 0f
            normals[detail * 9 + i * 3 + 1] = -1f
            normals[detail * 9 + i * 3 + 2] = 0f
        }

        vertices[detail * 12] = 0f
        vertices[detail * 12 + 1] = 0.5f
        vertices[detail * 12 + 2] = 0f
        vertices[detail * 12 + 3] = 0f
        vertices[detail * 12 + 4] = -0.5f
        vertices[detail * 12 + 5] = 0f

        normals[detail * 12] = 0f
        normals[detail * 12 + 1] = 1f
        normals[detail * 12 + 2] = 0f
        normals[detail * 12 + 3] = 0f
        normals[detail * 12 + 4] = -1f
        normals[detail * 12 + 5] = 0f

        for (i in 0 until detail) {
            elements[i * 3] = i
            elements[i * 3 + 1] = i + detail
            elements[i * 3 + 2] = (i + 1) % detail + detail

            elements[detail * 3 + i * 3] = i
            elements[detail * 3 + i * 3 + 1] = (i + 1) % detail + detail
            elements[detail * 3 + i * 3 + 2] = (i + 1) % detail

            elements[detail * 6 + i * 3] = detail * 4
            elements[detail * 6 + i * 3 + 1] = i + detail * 2
            elements[detail * 6 + i * 3 + 2] = (i + 1) % detail + detail * 2

            elements[detail * 9 + i * 3] = detail * 4 + 1
            elements[detail * 9 + i * 3 + 1] = (i + 1) % detail + detail * 3
            elements[detail * 9 + i * 3 + 2] = i + detail * 3
        }

        vertexCount = elements.size
        genVertexBuffer(vertices)
        genNormalBuffer(normals)
        genColourBuffer(vertices.size / 3)
        genElementBuffer(elements)
    }
}
