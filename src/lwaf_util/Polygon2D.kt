package lwaf_util

import lwaf_core.*

open class Polygon2D(
        val vertices: List<vec2>,
        val points: List<Int> = (0 until vertices.size).toList()
) {
    // TODO: broken
    open fun triangulate(): List<Triple<Int, Int, Int>> {
        val p = ArrayList(points)
        val result = mutableListOf<Triple<Int, Int, Int>>()

        while (p.size > 3) {
            val triple = (0 until p.size).map { i ->
                Triple(p[i], p[(i + 1) % p.size], p[(i + 2) % p.size])
            } .first { (a, b, c) ->
                val ab = vertices[b] - vertices[a]
                val ac = vertices[c] - vertices[a]
                val abR = ab.rotate90CCW()
                (abR dot ac) <= 0
            }

            result.add(Triple(triple.first, triple.second, triple.third))
            p.remove(triple.second)
        }

        return result
    }

    // TODO: broken
    open fun convexPolygonPoints(): List<List<Int>> {
        val p = ArrayList(points)
        val result = mutableListOf<List<Int>>()

        while (p.size > 3) {
            val triple = (0 until p.size).map { i ->
                Triple(p[i], p[(i + 1) % p.size], p[(i + 2) % p.size])
            } .first { (a, b, c) ->
                val ab = vertices[b] - vertices[a]
                val bc = vertices[c] - vertices[b]
                val abR = ab.rotate90CW()
                (abR dot bc) >= 0
            }

            result.add(listOf(triple.first, triple.second, triple.third))
            p.remove(triple.second)
        }

        result.add(p)

        return result
    }
//            = triangulate().map { it.toList() }

    fun convexPolygons(): List<ConvexPolygon2D>
            = convexPolygonPoints().map { ConvexPolygon2D(vertices, it) }
}

class ConvexPolygon2D(
        vertices: List<vec2>,
        points: List<Int> = (0 until vertices.size).toList()
): Polygon2D(vertices, points) {
    override fun triangulate(): List<Triple<Int, Int, Int>> = (2 .. points.size).map { i ->
        Triple(points[0], points[i - 1], points[i])
    }

    override fun convexPolygonPoints(): List<List<Int>> {
        return listOf(points)
    }
}

// TODO: unused vertices are given to the VAO (optimisation potential)
fun Polygon2D.toFanGLVAOs(): List<GLVAO> {
    return convexPolygons().map { poly ->
        generateStandardVAO(
                vertices.map { v -> v.vec3(0f) },
                vertices.map { vec3(0f, 0f, 1f) },
                poly.points.toIntArray()
        )
    }
}

// TODO: unused vertices are given to the VAO (optimisation potential)
fun Polygon2D.toTriangleGLVAOs(): List<GLVAO> {
    return convexPolygons().map { poly ->
        generateStandardVAO(
                poly.vertices.map { v -> v.vec3(0f) },
                poly.vertices.map { vec3(0f, 0f, 1f) },
                (2 to poly.points.size).toList().flatMap { i -> listOf(poly.points[0], poly.points[i - 1], poly.points[i]) } .toIntArray()
        )
    }
}
