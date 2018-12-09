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
public class Font {

    private static Pattern fontFilePatternMatcher, commonPatternMatcher, charPatternMatcher; {
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
                        "xoffset=\"(\\d+)\"\\s+" +
                        "yoffset=\"(\\d+)\"\\s+" +
                        "xadvance=\"(\\d+)\"\\s+" +
                        "page=\"0\" chnl=\"\\d+\"\\s*" +
                        "/>"
        );
    }

    private final int lineHeight, base, scaleW, scaleH;
    private final Texture texture;
    private final vec2f[][] textureAtlas = new vec2f[256][4];
    private final boolean[] charSet = new boolean[256];
    private final vec2f[] charOffsets = new vec2f[256];
    private final vec2f[] charSizes = new vec2f[256];
    private final float[] charWidths = new float[256];

    public Font(String filepath) throws IOException {
        var fileContent = new String(Files.readAllBytes(Paths.get(filepath)));
        var fontFileMatcher = fontFilePatternMatcher.matcher(fileContent);
        var commonMatcher = commonPatternMatcher.matcher(fileContent);
        var charMatcher = charPatternMatcher.matcher(fileContent);
        var charsFound = 0;

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
            int id = Integer.parseInt(charMatcher.group(1));
            int x = Integer.parseInt(charMatcher.group(2));
            int y = Integer.parseInt(charMatcher.group(3));
            int width = Integer.parseInt(charMatcher.group(4));
            int height = Integer.parseInt(charMatcher.group(5));
            int xoffset = Integer.parseInt(charMatcher.group(6));
            int yoffset = Integer.parseInt(charMatcher.group(7));
            int xadvance = Integer.parseInt(charMatcher.group(8));

            // non ascii characters are not supported
            if (id < 256) {
                float u = (float) x / scaleW;
                float v = (float) y / scaleH;
                float w = (float) width / scaleW;
                float h = (float) height / scaleH;

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

        if (charsFound == 0) {
            throw new IOException("Invalid .fnt file format: no characters included");
        }
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
