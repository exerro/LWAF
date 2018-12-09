package lwaf;

public class mat4f {

    float el[];

    private mat4f() {
        el = new float[16];
    }

    public static mat4f identity() {
        mat4f m = new mat4f();

        m.el[0] = 1;
        m.el[5] = 1;
        m.el[10] = 1;
        m.el[15] = 1;

        return m;
    }

    public static mat4f translation(float x, float y, float z) {
        mat4f m = mat4f.identity();

        m.el[ 3]  = x;
        m.el[ 7]  = y;
        m.el[11] = z;

        return m;
    }

    public static mat4f translation(vec3f translation) {
        return translation(translation.x, translation.y, translation.z);
    }

    public static mat4f scale(float x, float y, float z) {
        mat4f m = mat4f.identity();

        m.el[0]  = x;
        m.el[5]  = y;
        m.el[10] = z;

        return m;
    }

    public static mat4f scale(vec3f scale) {
        return scale(scale.x, scale.y, scale.z);
    }

    public mat4f copy() {
        mat4f m = new mat4f();

        for (int i = 0; i < 16; ++i) {
            m.el[i] = el[i];
        }

        return m;
    }

    public mat4f transpose() {
        mat4f m = new mat4f();

        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                m.el[4 * x + y] = el[4 * y + x];
            }
        }

        return m;
    }

    public mat4f translate(float x, float y, float z) {
        return mul(translation(x, y, z));
    }

    public mat4f translate(vec3f translation) {
        return translate(translation.x, translation.y, translation.z);
    }

    public mat4f scaleBy(float x, float y, float z) {
        return mul(scale(x, y, z));
    }

    public mat4f scaleBy(vec3f scale) {
        return scaleBy(scale.x, scale.y, scale.z);
    }

    public mat4f mul(mat4f m) {
        mat4f result = new mat4f();

        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                float v = 0;

                for (int j = 0; j < 4; ++j) {
                    v += el[y * 4 + j] * m.el[j * 4 + x];
                }

                result.el[y * 4 + x] = v;
            }
        }

        return result;
    }

    public vec3f mul(vec3f v, float w) {
        float x = v.x, y = v.y, z = v.z;

        return new vec3f(
                el[ 0] * x + el[ 1] * y + el[ 2] * z + el[ 3] * w,
                el[ 4] * x + el[ 5] * y + el[ 6] * z + el[ 7] * w,
                el[ 8] * x + el[ 9] * y + el[10] * z + el[11] * w
        );
    }

    public vec3f mul(vec3f v) {
        return mul(v, 0);
    }

    public vec3f apply(vec3f v) {
        return mul(v, 1);
    }

}