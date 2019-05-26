package lwaf_primitive

import lwaf_core.GLVAO
import lwaf_core.normalise
import lwaf_core.vec3

// TODO: improve this
//  due to the single normal at the top of the cone
//  there are very obvious vertical lines from lighting when rendering
class LegacyConeVAO(detail: Int) : GLVAO() {
    init {
        if (detail < 3)
            throw IllegalArgumentException("Detail is too low for cone construction ($detail)")

        val vertices = FloatArray(detail * 12)
        val normals = Array(detail * 4) { vec3(0f) }
        val elements = IntArray(detail * 6)

        for (i in 0 until detail) {
            val theta = (i.toFloat() / detail).toDouble() * 2.0 * Math.PI
            val sin = Math.sin(theta).toFloat()
            val cos = Math.cos(theta).toFloat()
            val theta2 = ((i + 0.5f) / detail).toDouble() * 2.0 * Math.PI
            val sin2 = Math.sin(theta2).toFloat()
            val cos2 = Math.cos(theta2).toFloat()

            vertices[i * 3 + detail * 3] = sin / 2
            vertices[i * 3] = vertices[i * 3 + detail * 3]
            vertices[i * 3 + detail * 3 + 1] = -0.5f
            vertices[i * 3 + 1] = vertices[i * 3 + detail * 3 + 1]
            vertices[i * 3 + detail * 3 + 2] = cos / 2
            vertices[i * 3 + 2] = vertices[i * 3 + detail * 3 + 2]

            vertices[i * 3 + detail * 6] = 0f
            vertices[i * 3 + detail * 6 + 1] = 0.5f
            vertices[i * 3 + detail * 6 + 2] = 0f

            vertices[i * 3 + detail * 9] = 0f
            vertices[i * 3 + detail * 9 + 1] = -0.5f
            vertices[i * 3 + detail * 9 + 2] = 0f

            normals[i] = vec3(sin, 0.5f, cos).normalise()
            normals[i + detail] = vec3(0f, -1f, 0f)
            normals[i + detail * 2] = vec3(sin2, 0.5f, cos2).normalise()
            normals[i + detail * 3] = vec3(0f, -1f, 0f)
        }

        for (i in 0 until detail) {
            elements[i * 3] = i + detail * 2
            elements[i * 3 + 1] = i
            elements[i * 3 + 2] = (i + 1) % detail
            elements[detail * 3 + i * 3] = i + detail * 3
            elements[detail * 3 + i * 3 + 2] = i + detail
            elements[detail * 3 + i * 3 + 1] = (i + 1) % detail + detail
        }

        vertexCount = elements.size
        genVertexBuffer(vertices)
        genNormalBuffer(vec3fToFloatArray(normals.toList()))
        genColourBuffer(vertices.size / 3)
        genElementBuffer(elements)
    }
}
