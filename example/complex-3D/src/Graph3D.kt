package lwaf_demo

import lwaf_core.GLVAO
import lwaf_core.*
import lwaf_core.vec2
import lwaf_core.vec3

import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

class Graph3D(private val function: (vec2) -> Float) {
    private var colouring: (vec3) -> vec3 = { _ -> vec3(1f, 1f, 1f) }
    private var bounds_min = vec2(-1f, -1f)
    private var bounds_max = vec2(1f, 1f)

    fun setBounds(min: vec2, max: vec2): Graph3D {
        this.bounds_min = min
        this.bounds_max = max
        return this
    }

    fun setColouring(colouring: (vec3) -> vec3): Graph3D {
        this.colouring = colouring
        return this
    }

    fun getTriangulatedVAO(strategy: EvaluationStrategy): GLVAO {
        val triangles = strategy.generateTriangles(this)
        val vertices = Array(triangles.size * 3) { vec3(0f) }
        val normals = Array(triangles.size * 3) { vec3(0f) }
        val colours = Array(triangles.size * 3) { vec3(0f) }
        val uvs = Array(triangles.size * 3) { vec2(0f) }
        val elements = IntArray(triangles.size * 3)
        val lookup = buildVertexLookup(triangles)
        var vertex = 0

        for (tri in triangles) {
            val p0 = lookup[tri.points[0]]!!
            val p1 = lookup[tri.points[1]]!!
            val p2 = lookup[tri.points[2]]!!
            val n = (p1 - p0).cross(p2 - p0).normalise()

            vertices[vertex] = p0
            vertices[vertex + 1] = p1
            vertices[vertex + 2] = p2

            normals[vertex] = n
            normals[vertex + 1] = n
            normals[vertex + 2] = n

            uvs[vertex] = vec2(0.5f + p0.x, 0.5f + p0.z)
            uvs[vertex + 1] = vec2(0.5f + p1.x, 0.5f + p1.z)
            uvs[vertex + 2] = vec2(0.5f + p2.x, 0.5f + p2.z)

            vertex += 3
        }

        for (i in colours.indices) {
            colours[i] = colouring(vertices[i])
        }

        for (i in elements.indices) {
            elements[i] = i
        }

        return generateStandardVAO(vertices, normals, elements, colours, uvs)
    }

    fun getSmoothVAO(strategy: EvaluationStrategy): GLVAO {
        val triangles = strategy.generateTriangles(this)
        val lookup = buildVertexLookup(triangles)
        val elements = IntArray(triangles.size * 3)
        var el = 0

        val vertex_list: List<vec2>
        var accumulated_normals: MutableList<vec3>
        val vertex_indexes: Map<vec2, Int>
        val vertices: Array<vec3>
        val normals: Array<vec3>
        val colours: Array<vec3>
        val uvs: Array<vec2>

        vertex_list = triangles
                .stream()
                .flatMap { tri -> Stream.of(*tri.points) }
                .distinct()
                .collect(Collectors.toList())

        vertex_indexes = IntStream.range(0, vertex_list.size)
                .parallel()
                .boxed()
                .collect(Collectors.toMap(
                        { vertex_list[it] },
                        { it }
                ))

        accumulated_normals = ArrayList(Collections.nCopies(vertex_list.size, vec3(0f, 0f, 0f)))

        for (triangle in triangles) {
            val p0 = triangle.points[0]
            val p1 = triangle.points[1]
            val p2 = triangle.points[2]
            val p0v = lookup[p0]!!
            val p1v = lookup[p1]!!
            val p2v = lookup[p2]!!
            val p0i = vertex_indexes[p0]
            val p1i = vertex_indexes[p1]
            val p2i = vertex_indexes[p2]
            var p0n = accumulated_normals[p0i!!]
            var p1n = accumulated_normals[p1i!!]
            var p2n = accumulated_normals[p2i!!]
            val n = (p1v - p0v).cross(p2v - p0v)

            p0n += n
            p1n += n
            p2n += n
            accumulated_normals[p0i] = p0n
            accumulated_normals[p1i] = p1n
            accumulated_normals[p2i] = p2n
        }

        accumulated_normals = accumulated_normals
                .parallelStream()
                .map { v -> v.normalise() }
                .collect(Collectors.toList())

        vertices = {
            val s = vertex_list
                .parallelStream()
                .map { lookup[it] }
                .collect(Collectors.toList())
            val a = ArrayList(s)
            val out = Array(s.size) { vec3(0f) }
            a.toArray(out)
            out
        }()

        normals = accumulated_normals.toTypedArray()

        colours = Array(vertices.size) { vec3(0f) }
        for (i in colours.indices) {
            colours[i] = colouring(vertices[i])
        }

        uvs = Array(vertices.size) { vec2(0f) }
        for (i in uvs.indices) {
            uvs[i] = vec2(vertices[i].x + 0.5f, vertices[i].z + 0.5f)
        }

        for (triangle in triangles) {
            elements[el] = vertex_indexes[triangle.points[0]]!!
            elements[el + 1] = vertex_indexes[triangle.points[1]]!!
            elements[el + 2] = vertex_indexes[triangle.points[2]]!!
            el += 3
        }

        return generateStandardVAO(vertices, normals, elements, colours, uvs)
    }

    fun buildVertexLookup(triangles: List<Tri>): Map<vec2, vec3> {
        val bounds_diff = bounds_max - bounds_min
        val half = vec2(1f, 1f) * 0.5f

        return triangles
                .parallelStream()
                .flatMap { tri -> Stream.of(*tri.points) }
                .distinct()
                .collect(Collectors.toMap(
                        { it },
                        { v ->
                            val pos = bounds_min + bounds_diff * (v + half)
                            val `val` = function(pos)
                            vec3(v.x, `val`, v.y)
                        }
                ))
    }

    fun eval(v: vec2): vec3 {
        val pos = bounds_min + (bounds_max - bounds_min) * (v + vec2(0.5f, 0.5f))
        val `val` = function(pos)
        return vec3(pos.x, `val`, pos.y)
    }

    fun evalCached(v: vec2, cache: MutableMap<vec2, vec3>): vec3 {
        cache.computeIfAbsent(v) { this.eval(it) }
        return cache[v]!!
    }

    fun generateCache(): MutableMap<vec2, vec3> {
        return HashMap()
    }

    @FunctionalInterface
    interface SurfaceMap {
        fun apply(location: vec2): Float
    }

    class Tri internal constructor(vararg val points: vec2)

    abstract class EvaluationStrategy {
        abstract fun generateTriangles(graph: Graph3D): List<Tri>
    }

    open class UniformGridStrategy(internal val resolution: Int) : EvaluationStrategy() {

        protected fun generateVertexArray(): Array<Array<vec2>> {
            val result = Array(resolution + 1) { Array(resolution + 1) { vec2(0f) } }

            for (xi in 0..resolution) {
                val x = xi.toFloat() / resolution - 0.5f

                for (zi in 0..resolution) {
                    val z = zi.toFloat() / resolution - 0.5f

                    result[xi][zi] = vec2(x, z)
                }
            }

            return result
        }

        protected fun generateTrianglesFromVertexArray(vertices: Array<Array<vec2>>): List<Tri> {
            val result = ArrayList<Tri>(resolution * resolution * 2)

            for (xi in 0 until resolution) {
                for (zi in 0 until resolution) {
                    val a = vertices[xi][zi]
                    val b = vertices[xi][zi + 1]
                    val c = vertices[xi + 1][zi + 1]
                    val d = vertices[xi + 1][zi]

                    result.add(Tri(a, b, c))
                    result.add(Tri(a, c, d))
                }
            }

            return result
        }

        override fun generateTriangles(graph: Graph3D): List<Tri> {
            return generateTrianglesFromVertexArray(generateVertexArray())
        }
    }

    class GradientPullStrategy(resolution: Int) : UniformGridStrategy(resolution) {

        override fun generateTriangles(graph: Graph3D): List<Tri> {
            val points = generateVertexArray()
            val points_out = Array(resolution + 1) { Array(resolution + 1) { vec2(0f) } }
            val cache = graph.generateCache()
            val max_pull = 0.4f / resolution

            for (xi in 0..resolution) {
                for (zi in 0..resolution) {
                    var grad_x = 0f
                    var grad_z = 0f
                    val k = 2f / resolution

                    if (xi > 0 && xi < resolution) {
                        val y0x = graph.evalCached(points[xi - 1][zi], cache).y
                        val y1x = graph.evalCached(points[xi + 1][zi], cache).y

                        grad_x = (y1x - y0x) * k
                        if (Math.abs(grad_x) > max_pull) grad_x = if (grad_x > 0) max_pull else -max_pull
                    }

                    if (zi > 0 && zi < resolution) {
                        val y0z = graph.evalCached(points[xi][zi - 1], cache).y
                        val y1z = graph.evalCached(points[xi][zi + 1], cache).y

                        grad_z = (y1z - y0z) * k
                        if (Math.abs(grad_z) > max_pull) grad_z = if (grad_z > 0) max_pull else -max_pull
                    }

                    points_out[xi][zi] = vec2(points[xi][zi].x + grad_x, points[xi][zi].y + grad_z)
                }
            }

            return generateTrianglesFromVertexArray(points_out)
        }
    }
}
