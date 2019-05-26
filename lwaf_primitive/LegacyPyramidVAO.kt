package lwaf_primitive

import lwaf_core.GLVAO
import lwaf_core.normalise
import lwaf_core.vec3

class LegacyPyramidVAO(sides: Int) : GLVAO() {
    init {
        if (sides < 3)
            throw IllegalArgumentException("Too few sides for pyramid construction ($sides)")

        val vertices = FloatArray((sides * 4 + 1) * 3)
        val normals = Array(sides * 4 + 1) { vec3(0f) }
        val elements = IntArray(sides * 6)

        for (i in 0 until sides) {
            val theta = (i.toFloat() / sides).toDouble() * 2.0 * Math.PI
            val sin = Math.sin(theta).toFloat()
            val cos = Math.cos(theta).toFloat()
            val theta2 = ((i + 0.5f) / sides).toDouble() * 2.0 * Math.PI
            val sin2 = Math.sin(theta2).toFloat()
            val cos2 = Math.cos(theta2).toFloat()
            val theta3 = ((i - 0.5f) / sides).toDouble() * 2.0 * Math.PI
            val sin3 = Math.sin(theta3).toFloat()
            val cos3 = Math.cos(theta3).toFloat()

            // left of face
            vertices[i * 3] = sin / 2
            vertices[i * 3 + 1] = -0.5f
            vertices[i * 3 + 2] = cos / 2

            // right of face
            vertices[sides * 3 + i * 3] = sin / 2
            vertices[sides * 3 + i * 3 + 1] = -0.5f
            vertices[sides * 3 + i * 3 + 2] = cos / 2

            // top of face
            vertices[sides * 6 + i * 3] = 0f
            vertices[sides * 6 + i * 3 + 1] = 0.5f
            vertices[sides * 6 + i * 3 + 2] = 0f

            // bottom face
            vertices[sides * 9 + i * 3] = sin / 2
            vertices[sides * 9 + i * 3 + 1] = -0.5f
            vertices[sides * 9 + i * 3 + 2] = cos / 2

            normals[i] = vec3(sin2, 0.5f, cos2).normalise()
            normals[sides + i] = vec3(sin3, 0.5f, cos3).normalise()
            normals[sides * 2 + i] = vec3(sin2, 0.5f, cos2).normalise()
            normals[sides * 3 + i] = vec3(0f, -1f, 0f)
        }

        vertices[sides * 12] = 0f
        vertices[sides * 12 + 1] = -0.5f
        vertices[sides * 12 + 2] = 0f

        normals[sides * 4] = vec3(0f, -1f, 0f)

        for (i in 0 until sides) {
            elements[i * 3] = i + sides * 2
            elements[i * 3 + 1] = i
            elements[i * 3 + 2] = (i + 1) % sides + sides

            elements[sides * 3 + i * 3] = sides * 4
            elements[sides * 3 + i * 3 + 2] = sides * 3 + i
            elements[sides * 3 + i * 3 + 1] = sides * 3 + (i + 1) % sides
        }

        vertexCount = elements.size
        genVertexBuffer(vertices)
        genNormalBuffer(vec3fToFloatArray(normals.toList()))
        genColourBuffer(vertices.size / 3)
        genElementBuffer(elements)
    }
}
