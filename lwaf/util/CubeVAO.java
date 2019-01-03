package lwaf.util;

import lwaf.VAO;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class CubeVAO extends VAO {
    public CubeVAO() {
        setVertexCount(36);
        genVertexBuffer(vertices);
        genNormalBuffer(normals);
        genColourBuffer();
        genUVBuffer(uvs);
        genElementBuffer(elements);
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

    protected static final float[] uvs = new float[] {
            // front face
            0.25f, 0.3333f,
            0.25f, 0.6667f,
            0.50f, 0.6667f,
            0.50f, 0.3333f,

            // back face
            1.00f, 0.3333f,
            1.00f, 0.6667f,
            0.75f, 0.6667f,
            0.75f, 0.3333f,

            // left face
            0.00f, 0.3333f,
            0.00f, 0.6667f,
            0.25f, 0.6667f,
            0.25f, 0.3333f,

            // right face
            0.50f, 0.3333f,
            0.50f, 0.6667f,
            0.75f, 0.6667f,
            0.75f, 0.3333f,

            // top face
            0.50f, 0.3333f,
            0.50f, 0.0000f,
            0.25f, 0.0000f,
            0.25f, 0.3333f,

            // bottom face
            0.25f, 0.6667f,
            0.25f, 1.0000f,
            0.50f, 1.0000f,
            0.50f, 0.6667f,
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
