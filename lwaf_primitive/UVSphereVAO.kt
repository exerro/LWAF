package lwaf_primitive

import lwaf_core.normalise
import lwaf_core.vec3

class UVSphereVAO(val verticalPoints: Int, val horizontalPoints: Int) : GenericSmoothSpheroidVAO() {

    init {

        val elements = genElements(horizontalPoints, verticalPoints)
        val vertices = genVertices(horizontalPoints, verticalPoints)

        vertexCount = elements.size
        genSpheroidBuffers(vertices)
        genColourBuffer(vertices.size)
        genUVBuffer(genUVs(horizontalPoints, verticalPoints))
        genElementBuffer(elements)
    }

    private fun genVertices(w: Int, h: Int): Array<vec3> {
        val vs = Array((w + 1) * h + 2 * w) { vec3(0f) }
        var v = 0

        for (yt in 0 until h) {
            for (xt in 0..w) {
                val yTheta = (yt + 1).toDouble() / (h + 1) * Math.PI
                val xTheta = xt.toDouble() / w * Math.PI * 2.0
                val y = Math.cos(yTheta)
                val rl = Math.sin(yTheta)
                val x = rl * Math.sin(xTheta)
                val z = rl * Math.cos(xTheta)

                vs[v++] = vec3(x.toFloat(), y.toFloat(), z.toFloat()).normalise()
            }
        }

        for (i in 0 until w) {
            vs[v++] = vec3(0f, 1f, 0f)
            vs[v++] = vec3(0f, -1f, 0f)
        }

        return vs
    }

    private fun genUVs(w: Int, h: Int): FloatArray {
        val uvs = FloatArray((w + 1) * h * 2 + 4 * w)
        var uv = 0

        for (yt in 0 until h) {
            for (xt in 0..w) {
                uvs[uv++] = xt.toFloat() / w
                uvs[uv++] = (yt + 1).toFloat() / (h + 1)
            }
        }

        for (i in 0 until w) {
            uvs[uv++] = i.toFloat() / w
            uvs[uv++] = 0f
            uvs[uv++] = i.toFloat() / w
            uvs[uv++] = 1f
        }

        return uvs
    }

    private fun genElements(w: Int, h: Int): IntArray {
        val elements = IntArray(6 * h * (w + 1))
        var i = 0
        val top = (w + 1) * h
        val bottom = top + 1

        for (y in 0 until h - 1) {
            for (x in 0..w) {
                val a = y * (w + 1) + x
                val b = y * (w + 1) + (x + 1)
                val c = (y + 1) * (w + 1) + x
                val d = (y + 1) * (w + 1) + (x + 1)

                elements[i++] = c
                elements[i++] = b
                elements[i++] = a
                elements[i++] = b
                elements[i++] = c
                elements[i++] = d
            }
        }

        for (x in 0..w) {
            val a = x % (w + 1)
            val b = (x + 1) % (w + 1)

            elements[i++] = top + x * 2
            elements[i++] = a
            elements[i++] = b
            elements[i++] = bottom + x * 2
            elements[i++] = (w + 1) * (h - 1) + b
            elements[i++] = (w + 1) * (h - 1) + a
        }

        return elements
    }
}
