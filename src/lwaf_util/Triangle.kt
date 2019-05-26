package lwaf_util

import lwaf_core.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

class TriangleSet(
        val triangles: Set<Triangle>,
        vertexTriangleMapSource: Map<vec3, Set<Triangle>>? = null
) {
    private val vertexTriangleMapDelegate: Map<vec3, Set<Triangle>> by lazy {
        val map = HashMap<vec3, MutableSet<Triangle>>()

        triangles.flatMap { it.vertices().map { v -> Pair(v, it) } } .forEach { (v, tri) ->
            map.computeIfAbsent(v) { HashSet() } .add(tri)
        }

        map
    }
    private val vertexTriangleMap = vertexTriangleMapSource ?: vertexTriangleMapDelegate

    val vertices: Set<vec3> by lazy {
        triangles.flatMap { it.vertices() } .toSet()
    }

    fun getTrianglesAttachedTo(vertex: vec3): Set<Triangle> {
        return vertexTriangleMap[vertex] ?: setOf()
    }

    fun mapTriangles(transforms: Boolean = false, fn: (Triangle) -> Triangle): TriangleSet
            = TriangleSet(triangles.parallelStream().map(fn).collect(Collectors.toSet()), if (transforms) null else vertexTriangleMap)

    fun mapTriangleVertices(transforms: Boolean = false, fn: (TriangleVertex) -> TriangleVertex): TriangleSet
            = TriangleSet(triangles.parallelStream().map { Triangle(fn(it.v0), fn(it.v1), fn(it.v2)) } .collect(Collectors.toSet()), if (transforms) null else vertexTriangleMap)
}

data class Triangle(
        val v0: TriangleVertex,
        val v1: TriangleVertex,
        val v2: TriangleVertex
) {
    val computedNormal: vec3 by lazy {
        (v1.position - v0.position).cross(v2.position - v0.position)
    }

    fun vertices(): Set<vec3>
            = setOf(v0.position, v1.position, v2.position)

    fun setNormals(replace: Boolean = true): Triangle = Triangle(
            v0.copy(normal = v0.normal.nullIf(replace) ?: computedNormal),
            v1.copy(normal = v1.normal.nullIf(replace) ?: computedNormal),
            v2.copy(normal = v2.normal.nullIf(replace) ?: computedNormal)
    )
}

data class TriangleVertex(
        val position: vec3,
        val normal: vec3? = null,
        val uv: vec2? = null,
        val colour: vec3? = null
)

data class VAOBufferGenerationOptions(
        val normals: VAOBufferGenerationOption = VAOBufferGenerationOption.ON,
        val textures: VAOBufferGenerationOption = VAOBufferGenerationOption.DETECT,
        val colours: VAOBufferGenerationOption = VAOBufferGenerationOption.DETECT
)

enum class VAOBufferGenerationOption {
    ON,
    OFF,
    OFF_BUT_BLANK,
    DETECT,
    DETECT_BUT_BLANK
}

private fun <T> T?.nullIf(cond: Boolean): T? = if (cond) null else this

fun TriangleSet.generateVAO(optimise: Boolean, options: VAOBufferGenerationOptions = VAOBufferGenerationOptions()): GLVAO {
    val fixedOptions = fixOptions(triangles, options)
    val vao = GLVAO()
    var vertices: MutableList<TriangleVertex> = ArrayList()
    var elements: IntArray

    fun prepareVertex(vertex: TriangleVertex): TriangleVertex = when {
        fixedOptions.normals == VAOBufferGenerationOption.ON && vertex.normal == null
        -> prepareVertex(vertex.copy(normal = vec3(0f, 1f, 0f)))
        fixedOptions.textures == VAOBufferGenerationOption.ON && vertex.uv == null
        -> prepareVertex(vertex.copy(uv = vec2(0f)))
        fixedOptions.colours == VAOBufferGenerationOption.ON && vertex.colour == null
        -> prepareVertex(vertex.copy(colour = vec3(1f)))
        else -> vertex
    }

    triangles.forEach { triangle ->
        vertices.add(prepareVertex(triangle.v0))
        vertices.add(prepareVertex(triangle.v1))
        vertices.add(prepareVertex(triangle.v2))
    }

    if (optimise) {
        optimiseVertices(preformatVertices(vertices, fixedOptions)).let {
            vertices = it.first.toMutableList()
            elements = it.second
        }
    }
    else {
        elements = (0 until vertices.size).toList().toIntArray()
    }

    vao.vertexCount = elements.size
    vao.genVertexBuffer(GLVAO.vec3fToFloatArray(vertices.map { it.position }))
    vao.genElementBuffer(elements)

    if (fixedOptions.normals == VAOBufferGenerationOption.ON)
        vao.genNormalBuffer(GLVAO.vec3fToFloatArray(vertices.map { it.normal!! }))
    else if (fixedOptions.normals == VAOBufferGenerationOption.OFF_BUT_BLANK)
        vao.genNormalBuffer(Array(vertices.size) { vec3(0f, 1f, 0f) } .flatMap { it.unpack().toList() } .toFloatArray())

    if (fixedOptions.textures == VAOBufferGenerationOption.ON)
        vao.genUVBuffer(GLVAO.vec2fToFloatArray(vertices.map { it.uv!! }))

    if (fixedOptions.colours== VAOBufferGenerationOption.ON)
        vao.genColourBuffer(GLVAO.vec3fToFloatArray(vertices.map { it.colour!! }))
    else if (fixedOptions.colours == VAOBufferGenerationOption.OFF_BUT_BLANK)
        vao.genColourBuffer(vertices.size)

    return vao
}

fun TriangleSet.computeNormals(smooth: Boolean = false, replaceCurrent: Boolean = false): TriangleSet {
    if (!smooth) {
        return mapTriangles { it.setNormals(!replaceCurrent) }
    }

    fun normalOfVertex(vertex: vec3): vec3 = getTrianglesAttachedTo(vertex)
            .map { it.computedNormal }
            .reduce { a, b -> a + b }
            .normalise()

    return mapTriangleVertices { it.copy(normal = (if (it.normal == null || replaceCurrent) normalOfVertex(it.position) else null) ?: it.normal) }
}

private fun fixOptions(inputs: Set<Triangle>, options: VAOBufferGenerationOptions): VAOBufferGenerationOptions {
    var finalOptions = options

    if (options.normals == VAOBufferGenerationOption.DETECT)
        finalOptions = finalOptions.copy(normals = inputs.anyVertexToOption { it.normal != null })

    if (options.textures == VAOBufferGenerationOption.DETECT)
        finalOptions = finalOptions.copy(textures = inputs.anyVertexToOption { it.uv != null })

    if (options.colours == VAOBufferGenerationOption.DETECT)
        finalOptions = finalOptions.copy(colours = inputs.anyVertexToOption { it.colour != null })

    if (options.normals == VAOBufferGenerationOption.DETECT_BUT_BLANK)
        finalOptions = finalOptions.copy(normals = inputs.anyVertexToOption(VAOBufferGenerationOption.OFF_BUT_BLANK) { it.normal != null })

    if (options.textures == VAOBufferGenerationOption.DETECT_BUT_BLANK)
        finalOptions = finalOptions.copy(textures = inputs.anyVertexToOption(VAOBufferGenerationOption.OFF_BUT_BLANK) { it.uv != null })

    if (options.colours == VAOBufferGenerationOption.DETECT_BUT_BLANK)
        finalOptions = finalOptions.copy(colours = inputs.anyVertexToOption(VAOBufferGenerationOption.OFF_BUT_BLANK) { it.colour != null })

    return finalOptions
}

private fun preformatVertices(inputs: List<TriangleVertex>, options: VAOBufferGenerationOptions): List<TriangleVertex> {
    var outputs = inputs

    if (options.normals == VAOBufferGenerationOption.OFF)
        outputs = outputs.map { it.copy(normal = null) }

    if (options.textures == VAOBufferGenerationOption.OFF)
        outputs = outputs.map { it.copy(uv = null) }

    if (options.colours == VAOBufferGenerationOption.OFF)
        outputs = outputs.map { it.copy(colour = null) }

    return outputs
}

private fun optimiseVertices(inputs: List<TriangleVertex>): Pair<List<TriangleVertex>, IntArray> {
    val cache = ConcurrentHashMap<TriangleVertex, Int>()
    val output = ArrayList<TriangleVertex>()
    val elements = IntArray(inputs.size)

    (0 until inputs.size).toList().zip(inputs).parallelStream().forEach { (i, vertex) ->
        elements[i] = cache.computeIfAbsent(vertex) {
            synchronized(output) {
                output.add(vertex)
                output.size - 1
            }
        }
    }

    return Pair(output, elements)
}

private fun Set<Triangle>.anyVertex(cond: (TriangleVertex) -> Boolean): Boolean
        = flatMap { listOf(it.v0, it.v1, it.v2) } .any(cond)

private fun Set<Triangle>.anyVertexToOption(off: VAOBufferGenerationOption = VAOBufferGenerationOption.OFF, cond: (TriangleVertex) -> Boolean): VAOBufferGenerationOption
        = if (anyVertex(cond)) VAOBufferGenerationOption.ON else off
