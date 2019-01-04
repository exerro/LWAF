package lwaf;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Text {

    private final String text;
    private final int lineWidth;
    private final Font font;
    private final float size;
    private final VAO vao;

    public Text(String text, int lineWidth, Font font, float size) {
        this.text = text;
        this.lineWidth = lineWidth;
        this.font = font;
        this.size = size;
        this.vao = new VAO();

        initialiseVAO();
    }

    public String getText() {
        return text;
    }

    public Font getFont() {
        return font;
    }

    VAO getVAO() {
        return vao;
    }

    private void initialiseVAO() {
        // 8 because 4 vec2fs per quad
        var vertices = new float[text.length() * 12];
        var colours = new float[text.length() * 12];
        var uvs = new float[text.length() * 8];
        var elements = new int[text.length() * 6];

        var base = font.getBase();
        var lineHeight = font.getLineHeight();
        var ei = 0;
        var sizeScale = size / lineHeight;
        var x = 0;
        var y = (lineHeight - base) * sizeScale;

        for (int i = 0; i < text.length(); ++i) {
            var c = text.charAt(i);
            var vi = i * 12;
            var uvi = i * 8;

            var width  = font.getCharWidth(c) * sizeScale;
            var size   = font.getCharSize(c).mul(sizeScale);
            var offset = font.getCharOffset(c).mul(sizeScale);
            var cuvs   = font.getCharUVPositions(c);

            uvs[uvi    ] = cuvs[0].x;
            uvs[uvi + 1] = cuvs[0].y;
            uvs[uvi + 2] = cuvs[1].x;
            uvs[uvi + 3] = cuvs[1].y;
            uvs[uvi + 4] = cuvs[2].x;
            uvs[uvi + 5] = cuvs[2].y;
            uvs[uvi + 6] = cuvs[3].x;
            uvs[uvi + 7] = cuvs[3].y;

            vertices[vi     ] = x + offset.x;
            vertices[vi +  1] = y - offset.y;
            vertices[vi +  2] = 0;
            vertices[vi +  3] = x + offset.x;
            vertices[vi +  4] = y - offset.y + size.y;
            vertices[vi +  5] = 0;
            vertices[vi +  6] = x + offset.x + size.x;
            vertices[vi +  7] = y - offset.y + size.y;
            vertices[vi +  8] = 0;
            vertices[vi +  9] = x + offset.x + size.x;
            vertices[vi + 10] = y - offset.y;
            vertices[vi + 11] = 0;

            x += width;
        }

        for (int i = 0; i < text.length(); ++i, ei += 6) {
            elements[ei    ] = 4 * i    ;
            elements[ei + 1] = 4 * i + 1;
            elements[ei + 2] = 4 * i + 2;
            elements[ei + 3] = 4 * i    ;
            elements[ei + 4] = 4 * i + 2;
            elements[ei + 5] = 4 * i + 3;
        }

        // TODO: populate vertices, uvs, and elements

        vao.setVertexCount(elements.length);
        vao.genVertexBuffer(vertices);
        vao.genColourBuffer(vertices.length / 3);
        vao.genUVBuffer(uvs);
        vao.genElementBuffer(elements);
    }

}
