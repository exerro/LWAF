import lwaf_core.GLVAO
import lwaf_core.*
import lwaf_core.vec2
import lwaf_core.vec3
import lwaf_util.*

import java.util.*
import java.util.stream.Collectors

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

    fun getTriangulatedVAO(smooth: Boolean, strategy: EvaluationStrategy): GLVAO {
        val cache = generateCache()
        val triangles = strategy.generateTriangles(this).parallelStream().map { tri ->
            val ps = tri.points.toList().map {
                val pos = evalCached(it, cache)
                val colour = colouring(pos)
                TriangleVertex(pos, null, null, colour)
            }
            Triangle(ps[0], ps[1], ps[2])
        } .collect(Collectors.toSet())

        return TriangleSet(triangles).computeNormals(smooth).generateVAO(false)
    }

    private fun eval(v: vec2): vec3 {
        val pos = bounds_min + (bounds_max - bounds_min) * (v + vec2(0.5f))
        return vec3(pos.x, function(pos), pos.y)
    }

    private fun evalCached(v: vec2, cache: MutableMap<vec2, vec3>): vec3 {
        synchronized(cache) {
            return cache.computeIfAbsent(v) { this.eval(it) }
        }
    }

    private fun generateCache(): MutableMap<vec2, vec3> {
        return HashMap()
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
