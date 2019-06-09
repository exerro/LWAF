package lwaf_primitive;

import lwaf_core.GLVAO;
import lwaf_core.vec3;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class GenericSmoothSpheroidVAO extends GLVAO {
    // assumes vertices are already normalised
    protected void genSpheroidBuffers(vec3[] vertices) {
        float[] vertex_floats, normal_floats;
        var normals = new vec3[vertices.length];

        System.arraycopy(vertices, 0, normals, 0, vertices.length);

        normal_floats = Util.vec3fToFloatArray(normals);
        vertex_floats = Util.vec3fToFloatArray(vertices);

        for (int i = 0; i < vertex_floats.length; ++i) {
            vertex_floats[i] = vertex_floats[i] / 2;
        }

        genVertexBuffer(vertex_floats);
        genNormalBuffer(normal_floats);
    }
}
