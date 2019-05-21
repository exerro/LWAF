package lwaf_core

// credit for .fnt and .png file generation:
//   http://kvazars.com/littera/

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.HashMap
import java.util.regex.Pattern

// TODO: <kerning first="\d+" second="\d+" amount="[+-]\d+"/>
//  not really sure what kerning is or how it works...
//  something to do with nice spacing between characters

// supports single page fonts only (no idea what pages are tbh)
class Font(
        private val lineHeight: Int,
        private val base: Int,
        private val char_uvs: Array<Array<vec2>>,
        private val char_offsets: Array<vec2>,
        private val char_sizes: Array<vec2>,
        private val char_x_advance: FloatArray,
        private val kerning: MutableMap<Int, MutableMap<Int, Int>>,
        private val sizeScale: Float,
        val texture: GLTexture
) {
    val height: Float
        get() = lineHeight * sizeScale

    fun withHeight(size: Float): Font
            = Font(lineHeight, base, char_uvs, char_offsets, char_sizes, char_x_advance, kerning, size / lineHeight, texture)

    fun getWidth(text: String): Float {
        var total = 0f

        for (i in 0 until text.length - 1) {
            total += getCharAdvance(text[i])
            total += getKerning(text[i].toInt(), text[i + 1].toInt())
        }

        total += getCharAdvance(text[text.length - 1])

        return total * sizeScale
    }

    fun getTextObject(text: String): FontText
            = FontText(text, this)

    internal fun getLineHeight(): Float
            = lineHeight * sizeScale

    internal fun getBase(): Float
            = base * sizeScale

    internal fun getCharUVPositions(c: Char): Array<vec2>
            = char_uvs[c.toInt()]

    internal fun getCharSize(c: Char): vec2
            = char_sizes[c.toInt()].mul(sizeScale)

    internal fun getCharOffset(c: Char): vec2
            = char_offsets[c.toInt()].mul(sizeScale)

    internal fun getCharAdvance(c: Char): Float
            = char_x_advance[c.toInt()] * sizeScale

    internal fun getKerning(id1: Int, id2: Int): Float
            = (kerning[id1]?.get(id2) ?: 0).toFloat() * sizeScale
}

class FontText internal constructor(val text: String, val font: Font) {
    val vao: GLVAO = GLVAO()

    init {
        val vertices = FloatArray(text.length * 12)
        val uvs = FloatArray(text.length * 8)
        val elements = IntArray(text.length * 6)

        val base = font.getBase()
        val lineHeight = font.getLineHeight()
        var ei = 0
        var x = 0
        val y = lineHeight - base

        for (i in 0 until text.length) {
            val c = text[i]
            val vi = i * 12
            val uvi = i * 8

            val width = font.getCharAdvance(c)
            val size = font.getCharSize(c)
            val offset = font.getCharOffset(c)
            val cuvs = font.getCharUVPositions(c)

            uvs[uvi] = cuvs[0].x
            uvs[uvi + 1] = cuvs[0].y
            uvs[uvi + 2] = cuvs[1].x
            uvs[uvi + 3] = cuvs[1].y
            uvs[uvi + 4] = cuvs[2].x
            uvs[uvi + 5] = cuvs[2].y
            uvs[uvi + 6] = cuvs[3].x
            uvs[uvi + 7] = cuvs[3].y

            vertices[vi] = x + offset.x
            vertices[vi + 1] = y - offset.y
            vertices[vi + 2] = 0f
            vertices[vi + 3] = x + offset.x
            vertices[vi + 4] = y - offset.y + size.y
            vertices[vi + 5] = 0f
            vertices[vi + 6] = x.toFloat() + offset.x + size.x
            vertices[vi + 7] = y - offset.y + size.y
            vertices[vi + 8] = 0f
            vertices[vi + 9] = x.toFloat() + offset.x + size.x
            vertices[vi + 10] = y - offset.y
            vertices[vi + 11] = 0f

            x += width.toInt()
        }

        var i = 0
        while (i < text.length) {
            elements[ei] = 4 * i
            elements[ei + 1] = 4 * i + 1
            elements[ei + 2] = 4 * i + 2
            elements[ei + 3] = 4 * i
            elements[ei + 4] = 4 * i + 2
            elements[ei + 5] = 4 * i + 3
            ++i
            ei += 6
        }

        vao.vertexCount = elements.size
        vao.genVertexBuffer(vertices)
        vao.genColourBuffer(vertices.size / 3)
        vao.genUVBuffer(uvs)
        vao.genElementBuffer(elements)
    }
}

fun loadFont(filePath: String): Font {
    val lineHeight: Int
    val base: Int
    val texture: GLTexture
    val uvs: Array<Array<vec2>> = Array(256) { Array(4) { vec2(0f, 0f) } }
    val offsets: Array<vec2> = Array(256) { vec2(0f, 0f) }
    val sizes: Array<vec2> = Array(256) { vec2(0f, 0f) }
    val xAdvances = FloatArray(256) { 0f }
    val kerning: MutableMap<Int, MutableMap<Int, Int>> = HashMap()

    val fileContent = String(Files.readAllBytes(Paths.get(filePath)))
    val fontFileMatcher = fontFilePatternMatcher.matcher(fileContent)
    val commonMatcher = commonPatternMatcher.matcher(fileContent)
    val charMatcher = charPatternMatcher.matcher(fileContent)
    val kerningMatcher = kerningPatternMatcher.matcher(fileContent)
    var charsFound = 0
    val charSet = BooleanArray(256)
    val scaleW: Int
    val scaleH: Int

    if (fontFileMatcher.find()) {
        texture = loadTexture(fontFileMatcher.group(1))
    } else {
        throw IOException("Invalid .fnt file format: no page file found")
    }

    if (commonMatcher.find()) {
        lineHeight = Integer.parseInt(commonMatcher.group(1))
        base = Integer.parseInt(commonMatcher.group(2))
        scaleW = Integer.parseInt(commonMatcher.group(3))
        scaleH = Integer.parseInt(commonMatcher.group(4))
    } else {
        throw IOException("Invalid .fnt file format: no common")
    }

    while (charMatcher.find()) {
        val id = Integer.parseInt(charMatcher.group(1))
        val x = Integer.parseInt(charMatcher.group(2))
        val y = Integer.parseInt(charMatcher.group(3))
        val width = Integer.parseInt(charMatcher.group(4))
        val height = Integer.parseInt(charMatcher.group(5))
        val xoffset = Integer.parseInt(charMatcher.group(6))
        val yoffset = Integer.parseInt(charMatcher.group(7))
        val xadvance = Integer.parseInt(charMatcher.group(8))

        // non ascii characters are not supported
        if (id < 256) {
            val u = x.toFloat() / scaleW
            val v = y.toFloat() / scaleH
            val w = width.toFloat() / scaleW
            val h = height.toFloat() / scaleH

            uvs[id] = arrayOf(vec2(u, v), vec2(u, v + h), vec2(u + w, v + h), vec2(u + w, v))

            charSet[id] = true
            offsets[id] = vec2(xoffset.toFloat(), (-yoffset).toFloat())
            sizes[id] = vec2(width.toFloat(), height.toFloat())
            xAdvances[id] = xadvance.toFloat()
            ++charsFound
        }
    }

    while (kerningMatcher.find()) {
        val id1 = Integer.parseInt(kerningMatcher.group(1))
        val id2 = Integer.parseInt(kerningMatcher.group(2))
        val k = Integer.parseInt(kerningMatcher.group(3))

        kerning.putIfAbsent(id1, HashMap())
        kerning[id1]!![id2] = k
    }

    for (i in 0..255) {
        if (!charSet[i]) {
            uvs[i] = uvs['_'.toInt()]
            offsets[i] = offsets['_'.toInt()]
            sizes[i] = sizes['_'.toInt()]
            xAdvances[i] = xAdvances['_'.toInt()]
        }
    }

    if (charsFound == 0) {
        throw IOException("Invalid .fnt file format: no characters included")
    }

    return Font(lineHeight, base, uvs, offsets, sizes, xAdvances, kerning, 1f, texture)
}

private var fontFilePatternMatcher: Pattern = Pattern.compile(
        "<page\\s+" +
                "id=\"\\d+\"\\s+" +
                "file=\"(.+)\"\\s*" +
                "/>"
)
private var commonPatternMatcher: Pattern = Pattern.compile(
        "<common\\s+" +
                "lineHeight=\"(\\d+)\"\\s+" +
                "base=\"(\\d+)\"\\s+" +
                "scaleW=\"(\\d+)\"\\s+" +
                "scaleH=\"(\\d+)\"\\s+" +
                "pages=\"1\" packed=\"\\d+\"\\s*" +
                "/>"
)
private var charPatternMatcher: Pattern = Pattern.compile(
        "<char " +
                "id=\"(\\d+)\"\\s+" +
                "x=\"(\\d+)\"\\s+" +
                "y=\"(\\d+)\"\\s+" +
                "width=\"(\\d+)\"\\s+" +
                "height=\"(\\d+)\"\\s+" +
                "xoffset=\"([+\\-]?\\d+)\"\\s+" +
                "yoffset=\"([+\\-]?\\d+)\"\\s+" +
                "xadvance=\"(\\d+)\"\\s+" +
                "page=\"0\" chnl=\"\\d+\"\\s*" +
                "/>"
)
private var kerningPatternMatcher: Pattern = Pattern.compile(
        "<kerning " +
                "first=\"(\\d+)\" " +
                "second=\"(\\d+)\" " +
                "amount=\"([+-]\\d+)\"" +
                "/>"
)
