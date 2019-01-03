package lwaf.util;

import lwaf.VAO;
import lwaf.vec3f;

import java.util.Arrays;
import java.util.function.Function;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class SphereVAO extends GenericSmoothSpheroidVAO {
    public final int resolution;

    public SphereVAO(int resolution) {
        this.resolution = resolution;

        var faceCount = icosahedron_faces.length;
        var faces     = icosahedron_faces;
        var vertices  = icosahedron_vertices;

        for (int res = 1; res < resolution; ++res) {
            var vertices_out = new vec3f[vertices.length + 3 * faces.length];
            var faces_out    = new int[faces.length * 4][];

            upscaleVertices(vertices, vertices_out, faces, faces_out);

            vertices   = vertices_out;
            faces      = faces_out;
            faceCount *= 4;
        }

        var elements = new int[faceCount * 3];

        for (int i = 0; i < faces.length; ++i) {
            elements[i * 3    ] = faces[i][0];
            elements[i * 3 + 1] = faces[i][1];
            elements[i * 3 + 2] = faces[i][2];
        }

        setVertexCount(faceCount * 3);
        genSpheroidBuffers(vertices, vec3f.one);
        genColourBuffer();
        genElementBuffer(elements);
    }

    private static void upscaleVertices(vec3f[] vertices_in, vec3f[] vertices_out, int[][] faces_in, int[][] faces_out) {
        int f = 0;
        int v = vertices_in.length;

        System.arraycopy(vertices_in, 0, vertices_out, 0, vertices_in.length);

        for (int[] face : faces_in) {
            var v0 = vertices_in[face[0]];
            var v1 = vertices_in[face[1]];
            var v2 = vertices_in[face[2]];
            var v3 = v0.add(v1).normalise();
            var v4 = v0.add(v2).normalise();
            var v5 = v1.add(v2).normalise();

            faces_out[f++] = new int[] {face[0], v, v + 1};
            faces_out[f++] = new int[] {face[2], v + 1, v + 2};
            faces_out[f++] = new int[] {v, face[1], v + 2};

            // faces_out[f++] = new int[] {v + 1, face[2], v + 2};
            faces_out[f++] = new int[] {v + 2, v + 1, v};

            vertices_out[v++] = v3;
            vertices_out[v++] = v4;
            vertices_out[v++] = v5;
        }
    }

    private static final float t = (float) Math.sqrt(5) / 2 + 1;

    private static vec3f[] icosahedron_vertices = new vec3f[] {
            new vec3f(-1f,  t ,  0f).normalise(),
            new vec3f( 1f,  t ,  0f).normalise(),
            new vec3f(-1f, -t ,  0f).normalise(),
            new vec3f( 1f, -t ,  0f).normalise(),
            new vec3f( 0f, -1f,  t ).normalise(),
            new vec3f( 0f,  1f,  t ).normalise(),
            new vec3f( 0f, -1f, -t ).normalise(),
            new vec3f( 0f,  1f, -t ).normalise(),
            new vec3f( t,   0f, -1f).normalise(),
            new vec3f( t,   0f,  1f).normalise(),
            new vec3f(-t,   0f, -1f).normalise(),
            new vec3f(-t,   0f,  1f).normalise(),
    };

    private static int[][] icosahedron_faces = new int[][] {
            new int[] {0, 11, 5},
            new int[] {0, 5, 1},
            new int[] {0, 1, 7},
            new int[] {0, 7, 10},
            new int[] {0, 10, 11},
            new int[] {1, 5, 9},
            new int[] {5, 11, 4},
            new int[] {11, 10, 2},
            new int[] {10, 7, 6},
            new int[] {7, 1, 8},
            new int[] {3, 9, 4},
            new int[] {3, 4, 2},
            new int[] {3, 2, 6},
            new int[] {3, 6, 8},
            new int[] {3, 8, 9},
            new int[] {4, 9, 5},
            new int[] {2, 4, 11},
            new int[] {6, 2, 10},
            new int[] {8, 6, 7},
            new int[] {9, 8, 1},
    };
}
