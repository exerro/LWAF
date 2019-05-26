package lwaf_3D.poly

import lwaf_3D.DrawContext3D
import lwaf_3D.Material
import lwaf_3D.MutableObject3D
import lwaf_core.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

class Model internal constructor(val objects: MutableMap<String, Pair<GLVAO, Material>>, val resID: String): Resource, GLResource, MutableObject3D {
    override fun getResourceID(): String = resID
    override fun free() = destroy()

    override fun destroy() {
        objects.values.map { (vao, _) -> vao.destroy() }
    }

    override var translation: vec3 = vec3(0f)
    override var rotation: vec3 = vec3(0f)
    override var scale: vec3 = vec3(1f)

    override fun draw(context: DrawContext3D, shader: GLShaderProgram) {
        shader.setUniform("transform", mat4_translate(translation) * (rotation.toRotationMatrix() * mat3_scale(scale)).mat4())

        objects.values.map { (vao, material) ->
            shader.setUniform("colour", material.colour)
            shader.setUniform("useTexture", material.texture != null)
            shader.setUniform("diffuseLightingIntensity", material.diffuseLightingIntensity)
            shader.setUniform("specularLightingIntensity", material.specularLightingIntensity)
            shader.setUniform("specularLightingPower", material.specularLightingPower)

            material.texture?.bind()
            context.drawIndexedVAO(vao)
            material.texture?.unbind()
        }
    }
}

fun loadOBJModel(path: String): Model
        = loadOBJModelContent(Files.readAllLines(Paths.get(path)), path)

private fun loadOBJModelContent(lines: List<String>, resID: String): Model {
    val (vertexData, rest) = splitVertexDataLines(trimEmptyLines(lines))
    val (vertices, uvs, normals) = parseVertexDataLines(vertexData)
    val objects = parseObjects(rest)
            .map { (k, v) -> k to v.copy(second = v.second.filter { it.second.isNotEmpty() } .toMutableList()) }
            .filter { (_, v) -> v.second.isNotEmpty() }

    println("#objects: ${objects.size}")

    return Model(objects.map { (name, ob) ->
        ob.second.mapIndexed { i, group ->
            val vao = loadObjectIntoVAO(group.first, group.second, vertices, uvs, normals)
            val material = if (ob.first == null) Material() else loadMaterial(ob.first!!)

            "$name[$i]" to Pair(vao, material)
        }
    } .flatten() .toMap() .toMutableMap(), resID)
}

// TODO: major optimisation with vertex sharing
private fun loadObjectIntoVAO(smoothBlend: Boolean, faces: List<Face>, sourceVertices: List<vec3>, sourceUVs: List<vec2>, sourceNormals: List<vec3>): GLVAO {
    val outputVertices = mutableListOf<vec3>()
    val outputUVS = mutableListOf<vec2>()
    val outputNormals = mutableListOf<vec3>()

    // TODO: use smoothBlend

    faces.forEach { (a, b, c) ->
        val av = sourceVertices[a.first - 1]
        val bv = sourceVertices[b.first - 1]
        val cv = sourceVertices[c.first - 1]
        val normal = if (a.third == null || b.third == null || c.third == null) {
            (bv - av).cross(cv - av)
        }
        else {
            sourceNormals[a.third!! - 1]
        }
        val an = if (a.third == null) normal else sourceNormals[a.third!! - 1]
        val bn = if (b.third == null) normal else sourceNormals[b.third!! - 1]
        val cn = if (c.third == null) normal else sourceNormals[c.third!! - 1]
        val auv = if (a.second == null) vec2(0f) else sourceUVs[a.second!! - 1]
        val buv = if (b.second == null) vec2(0f) else sourceUVs[b.second!! - 1]
        val cuv = if (c.second == null) vec2(0f) else sourceUVs[c.second!! - 1]

        outputVertices.add(av)
        outputVertices.add(bv)
        outputVertices.add(cv)
        outputUVS.add(auv * vec2(1f, -1f) + vec2(0f, 1f))
        outputUVS.add(buv * vec2(1f, -1f) + vec2(0f, 1f))
        outputUVS.add(cuv * vec2(1f, -1f) + vec2(0f, 1f))
        outputNormals.add(an.normalise())
        outputNormals.add(bn.normalise())
        outputNormals.add(cn.normalise())
    }

    return generateStandardVAO(outputVertices, outputNormals, (0 until faces.size * 3) .toList() .toIntArray(), null, outputUVS)
}

private fun loadMaterial(path: String): Material {
    return Material() // TODO
}

private fun trimEmptyLines(lines: List<String>): List<String>
        = lines
        .parallelStream()
        .map { if (it.contains("#")) it.substring(0, it.indexOf("#")) else it }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .collect(Collectors.toList())

private fun splitVertexDataLines(lines: List<String>): Pair<List<String>, List<String>>
        = lines.partition { it.startsWith("v ") || it.startsWith("vt ") || it.startsWith("vn ") }

private fun parseVertexDataLines(lines: List<String>): Triple<List<vec3>, List<vec2>, List<vec3>>
        = lines.partition { it.startsWith("v ") } .let { (vertexLines, rest) ->
            val vertices = vertexLines.map { parse3DVector(it.substring(2)) }
            val (uvs, normals) = rest.partition { it.startsWith("vt ") } .let { (uvLines, normalLines) ->
                Pair(
                        uvLines.map { parse2DVector(it.substring(3)) },
                        normalLines.map { parse3DVector(it.substring(3)) }
                )
            }
            Triple(vertices, uvs, normals)
        }

private fun parse3DVector(data: String): vec3 {
    val tokenizer = StringTokenizer(data)
    val x = tokenizer.nextToken().toFloat()
    val y = tokenizer.nextToken().toFloat()
    val z = tokenizer.nextToken().toFloat()
    return vec3(x, y, z)
}

private fun parse2DVector(data: String): vec2 {
    val tokenizer = StringTokenizer(data)
    val x = tokenizer.nextToken().toFloat()
    val y = if (tokenizer.hasMoreTokens()) tokenizer.nextToken().toFloat() else 0f
    return vec2(x, y)
}

private fun parseObjects(lines: List<String>): Map<String, Object> {
    var objectName = "default"
    val groups = HashMap<String, Object>()
    var smoothEnabled = false

    lines.forEach { line ->
        groups.computeIfAbsent(objectName) { Pair(null, mutableListOf(Pair(smoothEnabled, mutableListOf()))) }

        when {
            line.startsWith("f ") -> {
                val faces = groups[objectName]!!.second.last().second
                faces.addAll(parseFace(line.substring(2)))
            }
            line.startsWith("s ") -> {
                smoothEnabled = line == "s on"
                groups[objectName]!!.second.add(Pair(smoothEnabled, mutableListOf()))
            }
            line.startsWith("usemtl ") -> {
                groups[objectName] = Pair(line.substring(7), groups[objectName]!!.second)
            }
            line.startsWith("o ") -> {
                objectName = line.substring(2)
                smoothEnabled = false
            }
        }
    }

    return groups
}

private fun parseFace(data: String): List<Face> {
    val parts = data.split(" ")
    return parts.zip(parts.drop(1)).zip(parts.drop(2)).map { (a, c) -> Triple(
            parseFaceVertex(a.first),
            parseFaceVertex(a.second),
            parseFaceVertex(c)
    ) }
}

private fun parseFaceVertex(data: String): FaceVertex {
    val match = Regex("(\\d+)(?:/(\\d+)?(?:/(\\d+))?)?").find(data)!!
    return Triple(
           match.groupValues[1].toInt(),
           match.groupValues.getOrNull(2)?. let { it -> if (it.isEmpty()) null else it } ?.toInt(),
           match.groupValues.getOrNull(3)?. let { it -> if (it.isEmpty()) null else it } ?.toInt()
    )
}

typealias Object = Pair<String?, MutableList<Pair<Boolean, MutableList<Face>>>>
typealias Face = Triple<FaceVertex, FaceVertex, FaceVertex>
typealias FaceVertex = Triple<Int, Int?, Int?>
