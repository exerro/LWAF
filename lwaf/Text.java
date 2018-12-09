package lwaf;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

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
        var vertices = new float[text.length() * 8];
        var uvs = new float[text.length() * 8];
        var elements = new int[text.length() * 6];

        int base = font.getBase();
        int lineHeight = font.getLineHeight();
        int ei = 0;
        float sizeScale = size / lineHeight;
        float x = 0, y = (lineHeight - base) * sizeScale;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            int vi = i * 8;

            var width  = font.getCharWidth(c) * sizeScale;
            var size   = font.getCharSize(c).mul(sizeScale);
            var offset = font.getCharOffset(c).mul(sizeScale);
            var cuvs   = font.getCharUVPositions(c);

            uvs[vi    ] = cuvs[0].x;
            uvs[vi + 1] = cuvs[0].y;
            uvs[vi + 2] = cuvs[1].x;
            uvs[vi + 3] = cuvs[1].y;
            uvs[vi + 4] = cuvs[2].x;
            uvs[vi + 5] = cuvs[2].y;
            uvs[vi + 6] = cuvs[3].x;
            uvs[vi + 7] = cuvs[3].y;

            vertices[vi    ] = x + offset.x;
            vertices[vi + 1] = y - offset.y;
            vertices[vi + 2] = x + offset.x;
            vertices[vi + 3] = y - offset.y + size.y;
            vertices[vi + 4] = x + offset.x + size.x;
            vertices[vi + 5] = y - offset.y + size.y;
            vertices[vi + 6] = x + offset.x + size.x;
            vertices[vi + 7] = y - offset.y;

            System.out.println("Char: " + c);
            System.out.println("Top left UV: " + cuvs[0].toString());
            System.out.println("Top left vertex: (" + (x + offset.x) + ", " + (y + offset.y) + ")");
            System.out.println("Character size: " + size.toString());
            System.out.println("Advancing: " + width);
            System.out.println("Scale: " + sizeScale);

            x += width;
        }

        for (int i = 0; i < text.length(); ++i, ei += 6) {
            elements[ei    ] = 4 * i + 0;
            elements[ei + 1] = 4 * i + 1;
            elements[ei + 2] = 4 * i + 2;
            elements[ei + 3] = 4 * i + 0;
            elements[ei + 4] = 4 * i + 2;
            elements[ei + 5] = 4 * i + 3;
        }

        // TODO: populate vertices, uvs, and elements

        vao.setVertexCount(elements.length);

        int vertexVBOID = vao.genBuffer();
        int uvVBOID = vao.genBuffer();
        int elementVBOID = vao.genBuffer();

        vao.bindBuffer(vertexVBOID, 0, 2, GL_FLOAT);
        vao.bindBuffer(uvVBOID, 4, 2, GL_FLOAT);

        vao.enableAttribute(0);
        vao.enableAttribute(4);

        vao.bufferData(vertexVBOID, vertices, GL_STATIC_DRAW);
        vao.bufferData(uvVBOID, uvs, GL_STATIC_DRAW);

        vao.bufferElementData(elementVBOID, elements, GL_STATIC_DRAW);
    }

}
