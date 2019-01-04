package lwaf;

// credit for .fnt and .png file generation:
//   http://kvazars.com/littera/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// TODO: <kerning first="\d+" second="\d+" amount="[+-]\d+"/>
//  not really sure what kerning is or how it works...
//  something to do with nice spacing between characters

// supports single page fonts only (no idea what pages are tbh)
@SuppressWarnings({"unused", "WeakerAccess"})
public class Font {

    private static Pattern fontFilePatternMatcher, commonPatternMatcher, charPatternMatcher, kerningPatternMatcher; static {
        fontFilePatternMatcher = Pattern.compile(
                "<page\\s+" +
                        "id=\"\\d+\"\\s+" +
                        "file=\"(.+)\"\\s*" +
                        "/>"
        );
        commonPatternMatcher = Pattern.compile(
                "<common\\s+" +
                        "lineHeight=\"(\\d+)\"\\s+" +
                        "base=\"(\\d+)\"\\s+" +
                        "scaleW=\"(\\d+)\"\\s+" +
                        "scaleH=\"(\\d+)\"\\s+" +
                        "pages=\"1\" packed=\"\\d+\"\\s*" +
                        "/>"
        );
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
        );
        kerningPatternMatcher = Pattern.compile(
                "<kerning " +
                        "first=\"(\\d+)\" " +
                        "second=\"(\\d+)\" " +
                        "amount=\"([+-]\\d+)\"" +
                        "/>"
        );
    }

    private final int lineHeight, base;
    private final Texture texture;
    private final vec2f[][] char_uvs;
    private final vec2f[] char_offsets;
    private final vec2f[] char_sizes;
    private final float[] char_x_advance;
    private final Map<Integer, Map<Integer, Integer>> kernings;
    private final float sizeScale;

    private Font(Font font, float size) {
        lineHeight = font.lineHeight;
        base = font.base;
        texture = font.texture;
        char_uvs = font.char_uvs;
        char_offsets = font.char_offsets;
        char_sizes = font.char_sizes;
        char_x_advance = font.char_x_advance;
        kernings = font.kernings;
        sizeScale = size / lineHeight;
    }

    private Font(String filePath) throws IOException {
        var fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
        var fontFileMatcher = fontFilePatternMatcher.matcher(fileContent);
        var commonMatcher = commonPatternMatcher.matcher(fileContent);
        var charMatcher = charPatternMatcher.matcher(fileContent);
        var kerningMatcher = kerningPatternMatcher.matcher(fileContent);
        var charsFound = 0;
        var charSet = new boolean[256];
        int scaleW;
        int scaleH;

        char_uvs = new vec2f[256][4];
        char_offsets = new vec2f[256];
        char_sizes = new vec2f[256];
        char_x_advance = new float[256];
        kernings = new HashMap<>();

        if (fontFileMatcher.find()) {
            texture = Texture.load(fontFileMatcher.group(1));
        }
        else {
            throw new IOException("Invalid .fnt file format: no page file found");
        }

        if (commonMatcher.find()) {
            lineHeight = Integer.parseInt(commonMatcher.group(1));
            base = Integer.parseInt(commonMatcher.group(2));
            scaleW = Integer.parseInt(commonMatcher.group(3));
            scaleH = Integer.parseInt(commonMatcher.group(4));
        }
        else {
            throw new IOException("Invalid .fnt file format: no common");
        }

        while (charMatcher.find()) {
            var id = Integer.parseInt(charMatcher.group(1));
            var x = Integer.parseInt(charMatcher.group(2));
            var y = Integer.parseInt(charMatcher.group(3));
            var width = Integer.parseInt(charMatcher.group(4));
            var height = Integer.parseInt(charMatcher.group(5));
            var xoffset = Integer.parseInt(charMatcher.group(6));
            var yoffset = Integer.parseInt(charMatcher.group(7));
            var xadvance = Integer.parseInt(charMatcher.group(8));

            // non ascii characters are not supported
            if (id < 256) {
                var u = (float) x / scaleW;
                var v = (float) y / scaleH;
                var w = (float) width / scaleW;
                var h = (float) height / scaleH;

                char_uvs[id] = new vec2f[] {
                        new vec2f(u    , v    ),
                        new vec2f(u    , v + h),
                        new vec2f(u + w, v + h),
                        new vec2f(u + w, v    )
                };

                charSet[id] = true;
                char_offsets[id] = new vec2f(xoffset, -yoffset);
                char_sizes[id] = new vec2f(width, height);
                char_x_advance[id] = xadvance;
                ++charsFound;
            }
        }

        while (kerningMatcher.find()) {
            var id1 = Integer.parseInt(kerningMatcher.group(1));
            var id2 = Integer.parseInt(kerningMatcher.group(2));
            var k = Integer.parseInt(kerningMatcher.group(3));

            kernings.putIfAbsent(id1, new HashMap<>());
            kernings.get(id1).put(id2, k);
        }

        for (int i = 0; i < 256; ++i) {
            if (!charSet[i]) {
                char_uvs[i] = char_uvs['_'];
                char_offsets[i] = char_offsets['_'];
                char_sizes[i] = char_sizes['_'];
                char_x_advance[i] = char_x_advance['_'];
            }
        }

        if (charsFound == 0) {
            throw new IOException("Invalid .fnt file format: no characters included");
        }

        sizeScale = 1;
    }

    public static Font load(String filePath) throws IOException {
        return new Font(filePath);
    }

    public static Font safeLoad(String filePath) {
        try {
            return load(filePath);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            while (true);
        }
    }

    public float getWidth(String text) {
        var total = 0.f;

        for (int i = 0; i < text.length() - 1; ++i) {
            total += getCharAdvance(text.charAt(i));
            total += getKerning(text.charAt(i), text.charAt(i + 1));
        }

        total += getCharAdvance(text.charAt(text.length() - 1));

        return total * sizeScale;
    }

    public float getHeight() {
        return lineHeight * sizeScale;
    }

    public Font resizeTo(float size) {
        return new Font(this, size);
    }

    float getLineHeight() {
        return lineHeight * sizeScale;
    }

    float getBase() {
        return base * sizeScale;
    }

    public Texture getTexture() {
        return texture;
    }

    vec2f[] getCharUVPositions(char c) {
        return char_uvs[c];
    }

    vec2f getCharSize(char c) {
        return char_sizes[c].mul(sizeScale);
    }

    vec2f getCharOffset(char c) {
        return char_offsets[c].mul(sizeScale);
    }

    float getCharAdvance(char c) {
        return char_x_advance[c] * sizeScale;
    }

    float getKerning(int id1, int id2) {
        return kernings.containsKey(id1) ? kernings.get(id1).getOrDefault(id2, 0) * sizeScale : 0;
    }

}
