package lwaf_primitive

import lwaf_core.normalise
import lwaf_core.plus
import lwaf_core.vec3

// FIXME: non-optimal number of vertices are created by the upscaling method, this should be fixed
// TODO: add texturing support with calculated UV coords
class IcoSphereVAO(val resolution: Int) : GenericSmoothSpheroidVAO() {

    init {

        var faceCount = icosahedron_faces.size
        var faces = icosahedron_faces
        var vertices = icosahedron_vertices

        for (res in 1 until resolution) {
            val vertices_out = Array(vertices.size + 3 * faces.size) { vec3(0f) }
            val faces_out = Array(faces.size * 4) { IntArray(0) }

            upscaleVertices(vertices, vertices_out, faces, faces_out)

            vertices = vertices_out
            faces = faces_out
            faceCount *= 4
        }

        val elements = IntArray(faceCount * 3)

        for (i in faces.indices) {
            elements[i * 3] = faces[i][0]
            elements[i * 3 + 1] = faces[i][1]
            elements[i * 3 + 2] = faces[i][2]
        }

        vertexCount = faceCount * 3
        genSpheroidBuffers(vertices)
        genColourBuffer(vertices.size)
        genElementBuffer(elements)
    }

    companion object {

        private fun upscaleVertices(vertices_in: Array<vec3>, vertices_out: Array<vec3>, faces_in: Array<IntArray>, faces_out: Array<IntArray>) {
            var f = 0
            var v = vertices_in.size

            System.arraycopy(vertices_in, 0, vertices_out, 0, vertices_in.size)

            for (face in faces_in) {
                val v0 = vertices_in[face[0]]
                val v1 = vertices_in[face[1]]
                val v2 = vertices_in[face[2]]
                val v3 = (v0 + v1).normalise()
                val v4 = (v0 + v2).normalise()
                val v5 = (v1 + v2).normalise()

                faces_out[f++] = intArrayOf(face[0], v, v + 1)
                faces_out[f++] = intArrayOf(face[2], v + 1, v + 2)
                faces_out[f++] = intArrayOf(v, face[1], v + 2)

                // faces_out[f++] = new int[] {v + 1, face[2], v + 2};
                faces_out[f++] = intArrayOf(v + 2, v + 1, v)

                vertices_out[v++] = v3
                vertices_out[v++] = v4
                vertices_out[v++] = v5
            }
        }

        private val t = Math.sqrt(5.0).toFloat() / 2 + 1

        private val icosahedron_vertices = arrayOf<vec3>(vec3(-1f, t, 0f).normalise(), vec3(1f, t, 0f).normalise(), vec3(-1f, -t, 0f).normalise(), vec3(1f, -t, 0f).normalise(), vec3(0f, -1f, t).normalise(), vec3(0f, 1f, t).normalise(), vec3(0f, -1f, -t).normalise(), vec3(0f, 1f, -t).normalise(), vec3(t, 0f, -1f).normalise(), vec3(t, 0f, 1f).normalise(), vec3(-t, 0f, -1f).normalise(), vec3(-t, 0f, 1f).normalise())

        private val icosahedron_faces = arrayOf(intArrayOf(0, 11, 5), intArrayOf(0, 5, 1), intArrayOf(0, 1, 7), intArrayOf(0, 7, 10), intArrayOf(0, 10, 11), intArrayOf(1, 5, 9), intArrayOf(5, 11, 4), intArrayOf(11, 10, 2), intArrayOf(10, 7, 6), intArrayOf(7, 1, 8), intArrayOf(3, 9, 4), intArrayOf(3, 4, 2), intArrayOf(3, 2, 6), intArrayOf(3, 6, 8), intArrayOf(3, 8, 9), intArrayOf(4, 9, 5), intArrayOf(2, 4, 11), intArrayOf(6, 2, 10), intArrayOf(8, 6, 7), intArrayOf(9, 8, 1))
    }
}
