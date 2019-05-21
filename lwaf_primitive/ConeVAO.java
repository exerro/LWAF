package lwaf_primitive;

import lwaf_core.GLVAO;
import lwaf_core.vec3;

// TODO: improve this
//  due to the single normal at the top of the cone
//  there are very obvious vertical lines from lighting when rendering
public class ConeVAO extends GLVAO {
    public ConeVAO(int detail) {
        if (detail < 3)
            throw new IllegalArgumentException("Detail is too low for cone construction (" + detail + ")");

        var vertices = new float[detail * 12];
        var normals  = new vec3[detail * 4];
        var elements = new int[detail * 6];

        for (int i = 0; i < detail; ++i) {
            var theta = (float) i / detail * 2 * Math.PI;
            var sin = (float) Math.sin(theta);
            var cos = (float) Math.cos(theta);
            var theta2 = (i + 0.5f) / detail * 2 * Math.PI;
            var sin2 = (float) Math.sin(theta2);
            var cos2 = (float) Math.cos(theta2);

            vertices[i * 3    ] = vertices[i * 3 + detail * 3    ] = sin / 2;
            vertices[i * 3 + 1] = vertices[i * 3 + detail * 3 + 1] = -0.5f;
            vertices[i * 3 + 2] = vertices[i * 3 + detail * 3 + 2] = cos / 2;

            vertices[i * 3 + detail * 6    ] = 0;
            vertices[i * 3 + detail * 6 + 1] = 0.5f;
            vertices[i * 3 + detail * 6 + 2] = 0;

            vertices[i * 3 + detail * 9    ] = 0;
            vertices[i * 3 + detail * 9 + 1] = -0.5f;
            vertices[i * 3 + detail * 9 + 2] = 0;

            normals[i             ] = new vec3(sin, 0.5f, cos).normalise();
            normals[i + detail    ] = new vec3(0, -1, 0);
            normals[i + detail * 2] = new vec3(sin2, 0.5f, cos2).normalise();
            normals[i + detail * 3] = new vec3(0, -1, 0);
        }

        for (int i = 0; i < detail; ++i) {
            elements[i * 3    ] = i + detail * 2;
            elements[i * 3 + 1] = i;
            elements[i * 3 + 2] = (i + 1) % detail;
            elements[detail * 3 + i * 3    ] = i + detail * 3;
            elements[detail * 3 + i * 3 + 2] = i + detail;
            elements[detail * 3 + i * 3 + 1] = (i + 1) % detail + detail;
        }

        setVertexCount(elements.length);
        genVertexBuffer(vertices);
        genNormalBuffer(Util.vec3fToFloatArray(normals));
        genColourBuffer(vertices.length / 3);
        genElementBuffer(elements);
    }
}
