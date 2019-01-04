package lwaf;

// credit for .fnt and .png file generation:
//   http://kvazars.com/littera/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

// TODO: <kerning first="\d+" second="\d+" amount="[+-]\d+"/>
//  not really sure what kerning is or how it works...
//  something to do with nice spacing between characters

// supports single page fonts only (no idea what pages are tbh)
@SuppressWarnings({"unused", "WeakerAccess"})
public class Font {

    private static Pattern fontFilePatternMatcher, commonPatternMatcher, charPatternMatcher; static {
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
    }

    private final int lineHeight, base;
    private final Texture texture;
    private final vec2f[][] textureAtlas = new vec2f[256][4];
    private final vec2f[] charOffsets = new vec2f[256];
    private final vec2f[] charSizes = new vec2f[256];
    private final float[] charWidths = new float[256];

    public Font(String filePath) throws IOException {
        var fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
        var fontFileMatcher = fontFilePatternMatcher.matcher(fileContent);
        var commonMatcher = commonPatternMatcher.matcher(fileContent);
        var charMatcher = charPatternMatcher.matcher(fileContent);
        var charsFound = 0;
        var charSet = new boolean[256];
        int scaleW;
        int scaleH;

        if (fontFileMatcher.find()) {
            texture = new Texture(fontFileMatcher.group(1));
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

                textureAtlas[id] = new vec2f[] {
                        new vec2f(u    , v    ),
                        new vec2f(u    , v + h),
                        new vec2f(u + w, v + h),
                        new vec2f(u + w, v    )
                };

                charSet[id] = true;
                charOffsets[id] = new vec2f(xoffset, -yoffset);
                charSizes[id] = new vec2f(width, height);
                charWidths[id] = xadvance;
                ++charsFound;
            }
        }

        for (int i = 0; i < 256; ++i) {
            if (!charSet[i]) {
                textureAtlas[i] = textureAtlas['_'];
                charOffsets[i] = charOffsets['_'];
                charSizes[i] = charSizes['_'];
                charWidths[i] = charWidths['_'];
            }
        }

        if (charsFound == 0) {
            throw new IOException("Invalid .fnt file format: no characters included");
        }
    }

    // TODO: fix this
    public float getWidth(String text) {
        var total = 0.f;

        for (int i = 0; i < text.length(); ++i) {
            total += getCharWidth(text.charAt(i));
            if (i > 0) {} // apply kerning
        }

        return total;
    }

    public float getHeight() {
        return lineHeight;
    }

    int getLineHeight() {
        return lineHeight;
    }

    int getBase() {
        return base;
    }

    public Texture getTexture() {
        return texture;
    }

    vec2f[] getCharUVPositions(char c) {
        return textureAtlas[c];
    }

    vec2f getCharSize(char c) {
        return charSizes[c];
    }

    vec2f getCharOffset(char c) {
        return charOffsets[c];
    }

    float getCharWidth(char c) {
        return charWidths[c];
    }

}
