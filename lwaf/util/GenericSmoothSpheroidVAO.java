package lwaf.util;

import lwaf.VAO;
import lwaf.vec3f;

public abstract class GenericSmoothSpheroidVAO extends VAO {
    // assumes vertices are already normalised
    protected void genSpheroidBuffers(vec3f[] vertices, vec3f scale) {
        float[] vertex_floats, normal_floats;
        var normal_scale = scale.normalise().inverse();
        var normals = new vec3f[vertices.length];

        for (int i = 0; i < vertices.length; ++i) {
            normals[i] = vertices[i].mul(normal_scale);
        }

        normal_floats = vec3fToFloatArray(normals);
        vertex_floats = vec3fToFloatArray(vertices);

        for (int i = 0; i < vertex_floats.length; ++i) {
            vertex_floats[i] = vertex_floats[i] / 2;
        }

        genVertexBuffer(vertex_floats);
        genNormalBuffer(normal_floats);
    }
}
