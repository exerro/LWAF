package lwaf.util;

import lwaf.VAO;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class CubeVAO extends VAO {
    public CubeVAO() {
        int vertexVBOID, normalVBOID, colourVBOID, elementVBOID;

        setVertexCount(36);

        vertexVBOID = genBuffer();
        normalVBOID = genBuffer();
        colourVBOID = genBuffer();
        elementVBOID = genBuffer();

        bindBuffer(vertexVBOID, 0, 3, GL_FLOAT);
        bindBuffer(normalVBOID, 1, 3, GL_FLOAT);
        bindBuffer(colourVBOID, 2, 3, GL_FLOAT);

        enableAttribute(0);
        enableAttribute(1);
        enableAttribute(2);

        bufferData(vertexVBOID, vertices, GL_STATIC_DRAW);
        bufferData(normalVBOID, normals, GL_STATIC_DRAW);
        bufferData(colourVBOID, colours, GL_STATIC_DRAW);
        bufferElementData(elementVBOID, elements, GL_STATIC_DRAW);
    }

    protected static final float[] vertices = new float[] {
            // front face
            -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,

            // back face
            -0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,

            // left face
            -0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,

            // right face
            0.5f,  0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,

            // top face
            0.5f,  0.5f,  0.5f,
            0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f,  0.5f,

            // bottom face
            -0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f,  0.5f,
    };

    protected static final float[] normals = new float[] {
            // front face
            0f,  0f,  1f,
            0f,  0f,  1f,
            0f,  0f,  1f,
            0f,  0f,  1f,

            // back face
            0f,  0f, -1f,
            0f,  0f, -1f,
            0f,  0f, -1f,
            0f,  0f, -1f,

            // left face
            -1f,  0f,  0f,
            -1f,  0f,  0f,
            -1f,  0f,  0f,
            -1f,  0f,  0f,

            // right face
            1f,  0f,  0f,
            1f,  0f,  0f,
            1f,  0f,  0f,
            1f,  0f,  0f,

            // top face
            0f,  1f,  0f,
            0f,  1f,  0f,
            0f,  1f,  0f,
            0f,  1f,  0f,

            // bottom face
            0f, -1f,  0f,
            0f, -1f,  0f,
            0f, -1f,  0f,
            0f, -1f,  0f,
    };

    protected static final float[] colours = new float[] {
            // front face
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,

            // back face
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,

            // left face
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,

            // right face
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,

            // top face
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,

            // bottom face
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
            1f, 1f, 1f,
    };

    protected static final int[] elements = new int[] {
            // front face
            0, 1, 2,
            0, 2, 3,

            // back face
            6, 5, 4,
            7, 6, 4,

            // left face
            8,  9, 10,
            8, 10, 11,

            // right face
            12, 13, 14,
            12, 14, 15,

            // top face
            16, 17, 18,
            16, 18, 19,

            // bottom face
            20, 21, 22,
            20, 22, 23,
    };
}