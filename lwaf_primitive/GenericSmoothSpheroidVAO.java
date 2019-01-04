package lwaf_primitive;

import lwaf.VAO;
import lwaf.vec3f;

@SuppressWarnings({"unused", "WeakerAccess"})
abstract class GenericSmoothSpheroidVAO extends VAO {
    // assumes vertices are already normalised
    protected void genSpheroidBuffers(vec3f[] vertices) {
        float[] vertex_floats, normal_floats;
        var normals = new vec3f[vertices.length];

        System.arraycopy(vertices, 0, normals, 0, vertices.length);

        normal_floats = vec3fToFloatArray(normals);
        vertex_floats = vec3fToFloatArray(vertices);

        for (int i = 0; i < vertex_floats.length; ++i) {
            vertex_floats[i] = vertex_floats[i] / 2;
        }

        genVertexBuffer(vertex_floats);
        genNormalBuffer(normal_floats);
    }
}
