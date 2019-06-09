package lwaf_model

import lwaf_3D.Material
import lwaf_core.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

object OBJModelLoader {
    private val numberPattern = Pattern.compile("-?\\d*\\.?\\d+")
    private val faceSectionPattern = Pattern.compile("(\\d+)(?:/(\\d*)(?:/(\\d+))?)?")

    fun loadModel(data: List<Pair<String, ModelData>>): Model<GLVAO> {
        val model = Model<GLVAO>()

        for ((objectName, objectData) in data) {
            model.addObject(objectName, objectData.getVAO(), Material())
        }

        return model
    }

    @Throws(FileNotFoundException::class)
    fun preloadModel(file: String): ArrayList<Pair<String, ModelData>> {
        println("Loading model '$file'")

        val parsed = readObjectLines(file)

        val vertices = (parsed.dataLines)
                .getOrDefault("v", emptyList())
                .stream()
                .map(parseVecNf(3))
                .collect(Collectors.toList())
        val normals = (parsed.dataLines)
                .getOrDefault("vn", emptyList())
                .stream()
                .map(parseVecNf(3))
                .collect(Collectors.toList())
        val uvs = (parsed.dataLines)
                .getOrDefault("vt", emptyList())
                .stream()
                .map(parseVecNf(2))
                .map { (x, y) -> vec2(x, 1 - y) } // why does 1-v.y work???
                .collect(Collectors.toList())

        println(vertices.size.toString() + " vertices; " + normals.size + " normals; " + uvs.size + " uvs; " + parsed.objects.size + " objects")

        val vertexCache = HashMap<IntArray, Int>()
        val pairs = ArrayList<Pair<String, ModelData>>()

        for (objectName in parsed.objects.keys) {
            val vaoData = loadObjectVAO(parsed.objects[objectName] ?: error(""), vertices, normals, uvs, vertexCache)

            println("Object '" + objectName + "': " + parsed.objects[objectName]!!.getOrDefault("f", emptyList()).size + " faces")

            pairs.add(Pair(objectName, vaoData))
        }

        return pairs
    }

    fun safePreloadModel(file: String): ArrayList<Pair<String, ModelData>> {
        try {
            return preloadModel(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            System.exit(1)
            while (true);
        }
    }

    private fun loadObjectVAO(linesData: Map<String, List<String>>, vertices: List<vec3>, normals: List<vec3>, uvs: List<vec2>, vertexCache: HashMap<IntArray, Int>): ModelData {
        val faces = linesData
                .getOrDefault("f", emptyList())
                .stream()
                .flatMap { parseFace(it).toList().stream() }
                .collect(Collectors.toList())

        val newVertices = ArrayList<vec3>()
        val newNormals = ArrayList<vec3>()
        val newUVs = ArrayList<vec2>()
        val elements = IntArray(faces.size * 3)
        var ei = 0

        for (face in faces) {
            // note, here, that face[vertex] is { position, uv, normal }
            val hasUndefinedNormal = face[0][2] == -1 || face[1][2] == -1 || face[2][2] == -1
            val normal = if (hasUndefinedNormal) calculateNormal(face[0][0], face[1][0], face[2][0], vertices) else vec3(0f, 0f, 0f)

            for (v in 0..2) {
                var set = false

                if (face[v][1] == -1 || face[v][2] == -1) {
                    set = true
                    elements[ei++] = newVertices.size
                } else {
                    if (!vertexCache.containsKey(face[v])) {
                        set = true
                        vertexCache[face[v]] = newVertices.size
                    }

                    elements[ei++] = vertexCache[face[v]]!!
                }

                if (set) {
                    newVertices.add(vertices[face[v][0] - 1])
                    newUVs.add(if (face[v][1] == -1) vec2(0f, 0f) else uvs[face[v][1] - 1])
                    newNormals.add((if (face[v][2] == -1) normal else normals[face[v][2] - 1]).normalise())
                }
            }
        }

        return ModelData(
                newVertices.toTypedArray(),
                newNormals.toTypedArray(),
                newUVs.toTypedArray(),
                elements
        )
    }

    private fun parseVecNf(n: Int): (String) -> vec3 {
        return { s ->
            val numberMatcher = numberPattern.matcher(s)
            val ns = floatArrayOf(0f, 0f, 0f)

            var i = 0
            while (i < n && numberMatcher.find()) {
                ns[i] = java.lang.Float.parseFloat(numberMatcher.group(0))
                ++i
            }

            vec3(ns[0], ns[1], ns[2])
        }
    }

    private fun parseFace(s: String): Array<Array<IntArray>> {
        val sectionMatcher = faceSectionPattern.matcher(s)
        val sections = ArrayList<IntArray>()

        while (sectionMatcher.find()) {
            val sVertex = sectionMatcher.group(1)
            val sUV = sectionMatcher.group(2)
            val sNormal = sectionMatcher.group(3)

            sections.add(intArrayOf(Integer.parseInt(sVertex), if (sUV == null || sUV == "") -1 else Integer.parseInt(sUV), if (sNormal == null || sNormal == "") -1 else Integer.parseInt(sNormal)))
        }

        val faces = Array(sections.size - 2) { Array(0) { IntArray(0) } }

        for (i in 0 until sections.size - 2) {
            val s0 = sections[0]
            val s1 = sections[i + 1]
            val s2 = sections[i + 2]

            faces[i] = arrayOf(s0, s1, s2)
        }

        return faces
    }

    @Throws(FileNotFoundException::class)
    private fun readObjectLines(file: String): ObjectLines {
        val reader = BufferedReader(FileReader(file))
        val objects = HashMap<String, MutableMap<String, MutableList<String>>>()
        val lines = HashMap<String, MutableList<String>>()
        var objectName = Model.DEFAULT_OBJECT_NAME

        for (line in reader.lines().collect(Collectors.toList()).map { sanitiseLine(it) }) {
            if (line.isEmpty()) continue
            if (line[0] < 'a' || line[0] > 'z') continue

            val type = getLineType(line)
            val data = getLineData(line)

            if (type == "o") {
                objectName = data
            } else if (type == "v" || type == "vt" || type == "vn") {
                lines.computeIfAbsent(type) { ArrayList() }
                lines[type]!!.add(data)
            } else {
                objects.computeIfAbsent(objectName) { HashMap() }
                objects[objectName]!!.computeIfAbsent(type) { ArrayList() }
                objects[objectName]!![type]!!.add(data)
            }
        }

        return ObjectLines(lines, objects)
    }

    private fun getLineType(line: String): String {
        return if (line.contains(" ")) line.substring(0, line.indexOf(" ")) else line
    }

    private fun getLineData(line: String): String {
        return if (line.contains(" ")) line.substring(line.indexOf(" ") + 1) else ""
    }

    private fun sanitiseLine(line: String): String {
        var ln = line
        if (ln.contains("#")) {
            ln = ln.substring(0, ln.indexOf("#"))
        }
        return ln.toLowerCase()
    }

    private fun calculateNormal(v0n: Int, v1n: Int, v2n: Int, vertices: List<vec3>): vec3 {
        val v0 = vertices[v0n - 1]
        val v1 = vertices[v1n - 1]
        val v2 = vertices[v2n - 1]
        return (v1 - v0).cross(v2 - v0)
    }

    private class ObjectLines(internal val dataLines: Map<String, List<String>>, internal val objects: Map<String, Map<String, List<String>>>)

}

class ModelData(val vertices: Array<vec3>, val normals: Array<vec3>, val uvs: Array<vec2>, val elements: IntArray) {
    fun getVAO() = generateStandardVAO(vertices, normals, null, uvs, elements)

}
