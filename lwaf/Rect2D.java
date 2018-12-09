package lwaf;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Rect2D extends UI {

    private static VAO vao;

    private vec2f size;

    public Rect2D(vec2f position, vec2f size, vec3f colour) {
        super(position, colour);
        this.size = size;
    }

    public vec2f getSize() {
        return size;
    }

    public void setSize(vec2f size) {
        this.size = size;
    }

    @Override
    void draw() {
        // set shader uniform stuff
        vao.load();
        GL11.glDrawElements(GL11.GL_TRIANGLES, vao.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        vao.unload();
    }

    public static void init() {
        vao = new VAO() {
            {
                int vertexVBOID, normalVBOID, colourVBOID, elementVBOID;

                setVertexCount(6);

                vertexVBOID = genBuffer();
                normalVBOID = genBuffer();
                colourVBOID = genBuffer();
                elementVBOID = genBuffer();

                bindBuffer(vertexVBOID, 0, 3, GL11.GL_FLOAT);
                bindBuffer(normalVBOID, 1, 3, GL11.GL_FLOAT);
                bindBuffer(colourVBOID, 2, 3, GL11.GL_FLOAT);

                enableAttribute(0);
                enableAttribute(1);
                enableAttribute(2);

                bufferData(vertexVBOID, new float[] {
                        -0.5f,  0.5f, 0,
                        -0.5f, -0.5f, 0,
                         0.5f, -0.5f, 0,
                         0.5f,  0.5f, 0
                }, GL_STATIC_DRAW);

                bufferData(normalVBOID, new float[] {
                        0, 0, 1,
                        0, 0, 1,
                        0, 0, 1,
                        0, 0, 1
                }, GL_STATIC_DRAW);

                bufferData(colourVBOID, new float[] {
                        1, 1, 1,
                        1, 1, 1,
                        1, 1, 1,
                        1, 1, 1
                }, GL_STATIC_DRAW);

                bufferElementData(elementVBOID, new int[] {
                        0, 1, 2,
                        0, 2, 3
                }, GL_STATIC_DRAW);
            }
        };
    }

}
