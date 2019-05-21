package lwaf_primitive;

import lwaf_core.GLVAO;

public class CylinderVAO extends GLVAO {
    public CylinderVAO(int detail) {
        if (detail < 3)
            throw new IllegalArgumentException("Detail is too low for cylinder construction (" + detail + ")");

        var vertices = new float[detail * 12 + 6];
        var normals  = new float[detail * 12 + 6];
        var elements = new int[detail * 12];

        for (int i = 0; i < detail; ++i) {
            var theta = (float) i / detail * 2 * Math.PI;
            var sin = (float) Math.sin(theta);
            var cos = (float) Math.cos(theta);

            vertices[i * 3    ] = sin / 2;
            vertices[i * 3 + 1] = 0.5f;
            vertices[i * 3 + 2] = cos / 2;

            vertices[detail * 3 + i * 3    ] = sin / 2;
            vertices[detail * 3 + i * 3 + 1] = -0.5f;
            vertices[detail * 3 + i * 3 + 2] = cos / 2;

            vertices[detail * 6 + i * 3    ] = sin / 2;
            vertices[detail * 6 + i * 3 + 1] = 0.5f;
            vertices[detail * 6 + i * 3 + 2] = cos / 2;

            vertices[detail * 9 + i * 3    ] = sin / 2;
            vertices[detail * 9 + i * 3 + 1] = -0.5f;
            vertices[detail * 9 + i * 3 + 2] = cos / 2;

            normals[i * 3    ] = sin;
            normals[i * 3 + 1] = 0;
            normals[i * 3 + 2] = cos;

            normals[detail * 3 + i * 3    ] = sin;
            normals[detail * 3 + i * 3 + 1] = 0;
            normals[detail * 3 + i * 3 + 2] = cos;

            normals[detail * 6 + i * 3    ] = 0;
            normals[detail * 6 + i * 3 + 1] = 1;
            normals[detail * 6 + i * 3 + 2] = 0;

            normals[detail * 9 + i * 3    ] = 0;
            normals[detail * 9 + i * 3 + 1] = -1;
            normals[detail * 9 + i * 3 + 2] = 0;
        }

        vertices[detail * 12    ] = 0;
        vertices[detail * 12 + 1] = 0.5f;
        vertices[detail * 12 + 2] = 0;
        vertices[detail * 12 + 3] = 0;
        vertices[detail * 12 + 4] = -0.5f;
        vertices[detail * 12 + 5] = 0;

        normals[detail * 12    ] = 0;
        normals[detail * 12 + 1] = 1;
        normals[detail * 12 + 2] = 0;
        normals[detail * 12 + 3] = 0;
        normals[detail * 12 + 4] = -1;
        normals[detail * 12 + 5] = 0;

        for (int i = 0; i < detail; ++i) {
            elements[i * 3    ] = i;
            elements[i * 3 + 1] = i + detail;
            elements[i * 3 + 2] = (i + 1) % detail + detail;

            elements[detail * 3 + i * 3    ] = i;
            elements[detail * 3 + i * 3 + 1] = (i + 1) % detail + detail;
            elements[detail * 3 + i * 3 + 2] = (i + 1) % detail;

            elements[detail * 6 + i * 3    ] = detail * 4;
            elements[detail * 6 + i * 3 + 1] = i + detail * 2;
            elements[detail * 6 + i * 3 + 2] = (i + 1) % detail + detail * 2;

            elements[detail * 9 + i * 3    ] = detail * 4 + 1;
            elements[detail * 9 + i * 3 + 1] = (i + 1) % detail + detail * 3;
            elements[detail * 9 + i * 3 + 2] = i + detail * 3;
        }

        setVertexCount(elements.length);
        genVertexBuffer(vertices);
        genNormalBuffer(normals);
        genColourBuffer(vertices.length / 3);
        genElementBuffer(elements);
    }
}
