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
class Font {

    private val lineHeight: Int
    private val base: Int
    val texture: GLTexture
    private val char_uvs: Array<Array<vec2>>
    private val char_offsets: Array<vec2>
    private val char_sizes: Array<vec2>
    private val char_x_advance: FloatArray
    private val kernings: MutableMap<Int, MutableMap<Int, Int>>
    private val sizeScale: Float

    val height: Float
        get() = lineHeight * sizeScale

    private constructor(font: Font, size: Float) {
        lineHeight = font.lineHeight
        base = font.base
        texture = font.texture
        char_uvs = font.char_uvs
        char_offsets = font.char_offsets
        char_sizes = font.char_sizes
        char_x_advance = font.char_x_advance
        kernings = font.kernings
        sizeScale = size / lineHeight
    }

    @Throws(IOException::class)
    private constructor(filePath: String) {
        val fileContent = String(Files.readAllBytes(Paths.get(filePath)))
        val fontFileMatcher = fontFilePatternMatcher!!.matcher(fileContent)
        val commonMatcher = commonPatternMatcher!!.matcher(fileContent)
        val charMatcher = charPatternMatcher!!.matcher(fileContent)
        val kerningMatcher = kerningPatternMatcher!!.matcher(fileContent)
        var charsFound = 0
        val charSet = BooleanArray(256)
        val scaleW: Int
        val scaleH: Int

        char_uvs = Array(256) { Array(4) { vec2(0f, 0f) } }
        char_offsets = Array(256) { vec2(0f, 0f) }
        char_sizes = Array(256) { vec2(0f, 0f) }
        char_x_advance = FloatArray(256) { 0f }
        kernings = HashMap()

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

                char_uvs[id] = arrayOf(vec2(u, v), vec2(u, v + h), vec2(u + w, v + h), vec2(u + w, v))

                charSet[id] = true
                char_offsets[id] = vec2(xoffset.toFloat(), (-yoffset).toFloat())
                char_sizes[id] = vec2(width.toFloat(), height.toFloat())
                char_x_advance[id] = xadvance.toFloat()
                ++charsFound
            }
        }

        while (kerningMatcher.find()) {
            val id1 = Integer.parseInt(kerningMatcher.group(1))
            val id2 = Integer.parseInt(kerningMatcher.group(2))
            val k = Integer.parseInt(kerningMatcher.group(3))

            kernings.putIfAbsent(id1, HashMap())
            kernings[id1]!![id2] = k
        }

        for (i in 0..255) {
            if (!charSet[i]) {
                char_uvs[i] = char_uvs['_'.toInt()]
                char_offsets[i] = char_offsets['_'.toInt()]
                char_sizes[i] = char_sizes['_'.toInt()]
                char_x_advance[i] = char_x_advance['_'.toInt()]
            }
        }

        if (charsFound == 0) {
            throw IOException("Invalid .fnt file format: no characters included")
        }

        sizeScale = 1f
    }

    fun getWidth(text: String): Float {
        var total = 0f

        for (i in 0 until text.length - 1) {
            total += getCharAdvance(text[i])
            total += getKerning(text[i].toInt(), text[i + 1].toInt())
        }

        total += getCharAdvance(text[text.length - 1])

        return total * sizeScale
    }

    fun resizeTo(size: Float): Font {
        return Font(this, size)
    }

    internal fun getLineHeight(): Float {
        return lineHeight * sizeScale
    }

    internal fun getBase(): Float {
        return base * sizeScale
    }

    internal fun getCharUVPositions(c: Char): Array<vec2> {
        return char_uvs[c.toInt()]
    }

    internal fun getCharSize(c: Char): vec2 {
        return char_sizes[c.toInt()].mul(sizeScale)
    }

    internal fun getCharOffset(c: Char): vec2 {
        return char_offsets[c.toInt()].mul(sizeScale)
    }

    internal fun getCharAdvance(c: Char): Float {
        return char_x_advance[c.toInt()] * sizeScale
    }

    internal fun getKerning(id1: Int, id2: Int): Float {
        return (kernings[id1]?.get(id2) ?: 0).toFloat() * sizeScale
    }

    companion object {

        private var fontFilePatternMatcher: Pattern? = null
        private var commonPatternMatcher: Pattern? = null
        private var charPatternMatcher: Pattern? = null
        private var kerningPatternMatcher: Pattern? = null

        init {
            fontFilePatternMatcher = Pattern.compile(
                    "<page\\s+" +
                            "id=\"\\d+\"\\s+" +
                            "file=\"(.+)\"\\s*" +
                            "/>"
            )
            commonPatternMatcher = Pattern.compile(
                    "<common\\s+" +
                            "lineHeight=\"(\\d+)\"\\s+" +
                            "base=\"(\\d+)\"\\s+" +
                            "scaleW=\"(\\d+)\"\\s+" +
                            "scaleH=\"(\\d+)\"\\s+" +
                            "pages=\"1\" packed=\"\\d+\"\\s*" +
                            "/>"
            )
            charPatternMatcher = Pattern.compile(
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
            kerningPatternMatcher = Pattern.compile(
                    "<kerning " +
                            "first=\"(\\d+)\" " +
                            "second=\"(\\d+)\" " +
                            "amount=\"([+-]\\d+)\"" +
                            "/>"
            )
        }

        @Throws(IOException::class)
        fun load(filePath: String): Font {
            return Font(filePath)
        }

        fun safeLoad(filePath: String): Font {
            try {
                return load(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
                System.exit(1)
                while (true);
            }

        }
    }

}
