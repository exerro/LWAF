package lwaf_primitive;

import lwaf_core.vec3;

public class Util {
    static float[] vec3fToFloatArray(vec3[] vectors) {
        float[] result = new float[vectors.length * 3];

        for (int i = 0; i < vectors.length; ++i) {
            result[i * 3] = vectors[i].getX();
            result[i * 3 + 1] = vectors[i].getY();
            result[i * 3 + 2] = vectors[i].getZ();
        }

        return result;
    }
}
