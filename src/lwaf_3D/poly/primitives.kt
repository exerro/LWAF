package lwaf_3D.poly

import lwaf_3D.Box
import lwaf_3D.Material
import lwaf_3D.Sphere
import lwaf_core.*

fun Box.toVAOObject3D(material: Material = Material()): VAOObject3D
        = VAOObject3D(getVAO(), material)

fun Sphere.toVAOObject3D(resolution: Int, material: Material = Material()): VAOObject3D
        = VAOObject3D(getIcoVAO(resolution), material)

fun Sphere.toVAOObject3D(material: Material = Material()): VAOObject3D
        = VAOObject3D(getIcoVAO(), material)

fun Sphere.toUVVAOObject3D(w: Int, h: Int, material: Material = Material()): VAOObject3D
        = VAOObject3D(getUVVAO(w, h), material)

fun Sphere.toUVVAOObject3D(material: Material = Material()): VAOObject3D
        = VAOObject3D(getUVVAO(), material)

fun Box.getVAO(): GLVAO
        = generateStandardVAO(boxVertices.map { it * size } .toTypedArray(), boxNormals, boxElements, boxVertices.size, boxUVs)

fun Sphere.getIcoVAO(resolution: Int = 4): GLVAO {
    var faceCount = icosahedron_faces.size
    var faces = icosahedron_faces
    var vertices = icosahedron_vertices

    for (res in 1 until resolution) {
        val vertices_out = Array(vertices.size + 3 * faces.size) { vec3(0f) }
        val faces_out = Array(faces.size * 4) { IntArray(0) }

        var f = 0
        var v = vertices.size

        System.arraycopy(vertices, 0, vertices_out, 0, vertices.size)

        for (face in faces) {
            val v0 = vertices[face[0]]
            val v1 = vertices[face[1]]
            val v2 = vertices[face[2]]
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

    return generateStandardVAO(vertices.map { it * radius }, vertices.toList(), elements, vertices.size)
}

fun Sphere.getUVVAO(w: Int = 30, h: Int = 20): GLVAO {
    val elements = IntArray(6 * h * (w + 1))
    val vertices = Array((w + 1) * h + 2 * w) { vec3(0f) }
    val uvs = Array((w + 1) * h + 2 * w) { vec2(0f) }
    var uv = 0
    var v = 0
    var el = 0
    val top = (w + 1) * h
    val bottom = top + 1

    for (y in 0 until h - 1) {
        for (x in 0..w) {
            val a = y * (w + 1) + x
            val b = y * (w + 1) + (x + 1)
            val c = (y + 1) * (w + 1) + x
            val d = (y + 1) * (w + 1) + (x + 1)

            elements[el++] = c
            elements[el++] = b
            elements[el++] = a
            elements[el++] = b
            elements[el++] = c
            elements[el++] = d
        }
    }

    for (x in 0..w) {
        val a = x % (w + 1)
        val b = (x + 1) % (w + 1)

        elements[el++] = top + x * 2
        elements[el++] = a
        elements[el++] = b
        elements[el++] = bottom + x * 2
        elements[el++] = (w + 1) * (h - 1) + b
        elements[el++] = (w + 1) * (h - 1) + a
    }

    for (yt in 0 until h) {
        for (xt in 0..w) {
            uvs[uv++] = vec2(xt.toFloat() / w, (yt + 1).toFloat() / (h + 1))
        }
    }

    for (i in 0 until w) {
        uvs[uv++] = vec2(i.toFloat() / w, 0f)
        uvs[uv++] = vec2(i.toFloat() / w, 0f)
    }

    for (yt in 0 until h) {
        for (xt in 0..w) {
            val yTheta = (yt + 1).toDouble() / (h + 1) * Math.PI
            val xTheta = xt.toDouble() / w * Math.PI * 2.0
            val y = Math.cos(yTheta)
            val rl = Math.sin(yTheta)
            val x = rl * Math.sin(xTheta)
            val z = rl * Math.cos(xTheta)

            vertices[v++] = vec3(x.toFloat(), y.toFloat(), z.toFloat()).normalise()
        }
    }

    for (i in 0 until w) {
        vertices[v++] = vec3(0f, 1f, 0f)
        vertices[v++] = vec3(0f, -1f, 0f)
    }

    return generateStandardVAO(vertices.map { it * radius }, vertices.toList(), elements, vertices.size, uvs.toList())
}

private val boxVertices = arrayOf(
        // front face
        vec3(-1f, 1f, 1f),
        vec3(-1f, -1f, 1f),
        vec3(1f, -1f, 1f),
        vec3(1f, 1f, 1f),

        // back face
        vec3(-1f, 1f, -1f),
        vec3(-1f, -1f, -1f),
        vec3(1f, -1f, -1f),
        vec3(1f, 1f, -1f),

        // left face
        vec3(-1f, 1f, -1f),
        vec3(-1f, -1f, -1f),
        vec3(-1f, -1f, 1f),
        vec3(-1f, 1f, 1f),

        // right face
        vec3(1f, 1f, 1f),
        vec3(1f, -1f, 1f),
        vec3(1f, -1f, -1f),
        vec3(1f, 1f, -1f),

        // top face
        vec3(1f, 1f, 1f),
        vec3(1f, 1f, -1f),
        vec3(-1f, 1f, -1f),
        vec3(-1f, 1f, 1f),

        // bottom face
        vec3(-1f, -1f, 1f),
        vec3(-1f, -1f, -1f),
        vec3(1f, -1f, -1f),
        vec3(1f, -1f, 1f)
)

private val boxUVs = arrayOf(
        // front face
        vec2(0.25f, 0.3333f),
        vec2(0.25f, 0.6667f),
        vec2(0.50f, 0.6667f),
        vec2(0.50f, 0.3333f),

        // back face
        vec2(1.00f, 0.3333f),
        vec2(1.00f, 0.6667f),
        vec2(0.75f, 0.6667f),
        vec2(0.75f, 0.3333f),

        // left face
        vec2(0.00f, 0.3333f),
        vec2(0.00f, 0.6667f),
        vec2(0.25f, 0.6667f),
        vec2(0.25f, 0.3333f),

        // right face
        vec2(0.50f, 0.3333f),
        vec2(0.50f, 0.6667f),
        vec2(0.75f, 0.6667f),
        vec2(0.75f, 0.3333f),

        // top face
        vec2(0.50f, 0.3333f),
        vec2(0.50f, 0.0000f),
        vec2(0.25f, 0.0000f),
        vec2(0.25f, 0.3333f),

        // bottom face
        vec2(0.25f, 0.6667f),
        vec2(0.25f, 1.0000f),
        vec2(0.50f, 1.0000f),
        vec2(0.50f, 0.6667f)
)

private val boxNormals = arrayOf(
        // front face
        vec3(0f, 0f, 1f),
        vec3(0f, 0f, 1f),
        vec3(0f, 0f, 1f),
        vec3(0f, 0f, 1f),

        // back face
        vec3(0f, 0f, -1f),
        vec3(0f, 0f, -1f),
        vec3(0f, 0f, -1f),
        vec3(0f, 0f, -1f),

        // left face
        vec3(-1f, 0f, 0f),
        vec3(-1f, 0f, 0f),
        vec3(-1f, 0f, 0f),
        vec3(-1f, 0f, 0f),

        // right face
        vec3(1f, 0f, 0f),
        vec3(1f, 0f, 0f),
        vec3(1f, 0f, 0f),
        vec3(1f, 0f, 0f),

        // top face
        vec3(0f, 1f, 0f),
        vec3(0f, 1f, 0f),
        vec3(0f, 1f, 0f),
        vec3(0f, 1f, 0f),

        // bottom face
        vec3(0f, -1f, 0f),
        vec3(0f, -1f, 0f),
        vec3(0f, -1f, 0f),
        vec3(0f, -1f, 0f)
)

private val boxElements = intArrayOf(
        // front face
        0, 1, 2, 0, 2, 3,

        // back face
        6, 5, 4, 7, 6, 4,

        // left face
        8, 9, 10, 8, 10, 11,

        // right face
        12, 13, 14, 12, 14, 15,

        // top face
        16, 17, 18, 16, 18, 19,

        // bottom face
        20, 21, 22, 20, 22, 23
)

private val icosahedronT = Math.sqrt(5.0).toFloat() / 2 + 1

private val icosahedron_vertices = arrayOf(
        vec3(-1f, icosahedronT, 0f).normalise(),
        vec3(1f, icosahedronT, 0f).normalise(),
        vec3(-1f, -icosahedronT, 0f).normalise(),
        vec3(1f, -icosahedronT, 0f).normalise(),
        vec3(0f, -1f, icosahedronT).normalise(),
        vec3(0f, 1f, icosahedronT).normalise(),
        vec3(0f, -1f, -icosahedronT).normalise(),
        vec3(0f, 1f, -icosahedronT).normalise(),
        vec3(icosahedronT, 0f, -1f).normalise(),
        vec3(icosahedronT, 0f, 1f).normalise(),
        vec3(-icosahedronT, 0f, -1f).normalise(),
        vec3(-icosahedronT, 0f, 1f).normalise()
)

private val icosahedron_faces = arrayOf(
        intArrayOf(0, 11, 5),
        intArrayOf(0, 5, 1),
        intArrayOf(0, 1, 7),
        intArrayOf(0, 7, 10),
        intArrayOf(0, 10, 11),
        intArrayOf(1, 5, 9),
        intArrayOf(5, 11, 4),
        intArrayOf(11, 10, 2),
        intArrayOf(10, 7, 6),
        intArrayOf(7, 1, 8),
        intArrayOf(3, 9, 4),
        intArrayOf(3, 4, 2),
        intArrayOf(3, 2, 6),
        intArrayOf(3, 6, 8),
        intArrayOf(3, 8, 9),
        intArrayOf(4, 9, 5),
        intArrayOf(2, 4, 11),
        intArrayOf(6, 2, 10),
        intArrayOf(8, 6, 7),
        intArrayOf(9, 8, 1)
)
