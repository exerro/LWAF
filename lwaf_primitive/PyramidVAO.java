package lwaf_primitive;

import lwaf_core.GLVAO;
import lwaf_core.vec3;

public class PyramidVAO extends GLVAO {
    public PyramidVAO(int sides) {
        if (sides < 3)
            throw new IllegalArgumentException("Too few sides for pyramid construction (" + sides + ")");

        float[] vertices = new float[(sides * 4 + 1) * 3];
        vec3[] normals  = new vec3[sides * 4 + 1];
        int[] elements = new int[sides * 6];

        for (int i = 0; i < sides; ++i) {
            var theta = (float) i / sides * 2 * Math.PI;
            var sin = (float) Math.sin(theta);
            var cos = (float) Math.cos(theta);
            var theta2 = (i + 0.5f) / sides * 2 * Math.PI;
            var sin2 = (float) Math.sin(theta2);
            var cos2 = (float) Math.cos(theta2);
            var theta3 = (i - 0.5f) / sides * 2 * Math.PI;
            var sin3 = (float) Math.sin(theta3);
            var cos3 = (float) Math.cos(theta3);

            // left of face
            vertices[i * 3    ] = sin / 2;
            vertices[i * 3 + 1] = -0.5f;
            vertices[i * 3 + 2] = cos / 2;

            // right of face
            vertices[sides * 3 + i * 3    ] = sin / 2;
            vertices[sides * 3 + i * 3 + 1] = -0.5f;
            vertices[sides * 3 + i * 3 + 2] = cos / 2;

            // top of face
            vertices[sides * 6 + i * 3    ] = 0;
            vertices[sides * 6 + i * 3 + 1] = 0.5f;
            vertices[sides * 6 + i * 3 + 2] = 0;

            // bottom face
            vertices[sides * 9 + i * 3    ] = sin / 2;
            vertices[sides * 9 + i * 3 + 1] = -0.5f;
            vertices[sides * 9 + i * 3 + 2] = cos / 2;

            normals[i            ] = new vec3(sin2, 0.5f, cos2).normalise();
            normals[sides     + i] = new vec3(sin3, 0.5f, cos3).normalise();
            normals[sides * 2 + i] = new vec3(sin2, 0.5f, cos2).normalise();
            normals[sides * 3 + i] = new vec3(0, -1, 0);
        }

        vertices[sides * 12    ] = 0;
        vertices[sides * 12 + 1] = -0.5f;
        vertices[sides * 12 + 2] = 0;

        normals[sides * 4] = new vec3(0, -1, 0);

        for (int i = 0; i < sides; ++i) {
            elements[i * 3    ] = i + sides * 2;
            elements[i * 3 + 1] = i;
            elements[i * 3 + 2] = (i + 1) % sides + sides;

            elements[sides * 3 + i * 3    ] = sides * 4;
            elements[sides * 3 + i * 3 + 2] = sides * 3 + i;
            elements[sides * 3 + i * 3 + 1] = sides * 3 + (i + 1) % sides;
        }

        setVertexCount(elements.length);
        genVertexBuffer(vertices);
        genNormalBuffer(Util.vec3fToFloatArray(normals));
        genColourBuffer(vertices.length / 3);
        genElementBuffer(elements);
    }
}
