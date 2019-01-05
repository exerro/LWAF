package lwaf;

@SuppressWarnings({"unused", "WeakerAccess"})
public class mat4f {

    final float el[];

    mat4f() {
        el = new float[16];
    }

    public mat4f(float[] els) {
        el = els;
    }

    public static mat4f identity() {
        var m = new mat4f();

        m.el[0] = 1;
        m.el[5] = 1;
        m.el[10] = 1;
        m.el[15] = 1;

        return m;
    }

    // creates a translation matrix
    public static mat4f translation(float x, float y, float z) {
        var m = mat4f.identity();

        m.el[ 3]  = x;
        m.el[ 7]  = y;
        m.el[11] = z;

        return m;
    }

    // creates a translation matrix
    public static mat4f translation(vec3f translation) {
        return translation(translation.x, translation.y, translation.z);
    }

    // creates a scale matrix
    public static mat4f scale(float x, float y, float z) {
        var m = mat4f.identity();

        m.el[0]  = x;
        m.el[5]  = y;
        m.el[10] = z;

        return m;
    }

    // creates a scale matrix
    public static mat4f scale(vec3f scale) {
        return scale(scale.x, scale.y, scale.z);
    }

    // creates a rotation matrix about the `axis` by `theta` radians
    public static mat4f rotation(vec3f axis, float theta) {
        var sin = (float) Math.sin(theta);
        var cos = (float) Math.cos(theta);
        var ux = axis.x;
        var uy = axis.y;
        var uz = axis.z;
        var ux2 = ux*ux;
        var uy2 = uy*uy;
        var uz2 = uz*uz;

        return new mat4f(new float[] {
                cos + ux2*(1-cos),        ux*uy*(1-cos) - uz*sin,   ux*uz*(1-cos) + uy*sin,   0,
                uy*ux*(1-cos) + uz*sin,   cos + uy2*(1-cos),        uy*uz*(1-cos) - ux*sin,   0,
                uz*ux*(1-cos) - uy*sin,   uz*uy*(1-cos) + ux*sin,   cos + uz2*(1-cos),        0,
                0,                        0,                        0,                        1
        });
    }

    // makes a copy of the matrix
    public mat4f copy() {
        var m = new mat4f();
        System.arraycopy(el, 0, m.el, 0, 16);
        return m;
    }

    // returns the transpose of the matrix
    public mat4f transpose() {
        var m = new mat4f();

        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                m.el[4 * x + y] = el[4 * y + x];
            }
        }

        return m;
    }

    // returns the matrix translated
    public mat4f translate(float x, float y, float z) {
        return mul(translation(x, y, z));
    }

    // returns the matrix translated
    public mat4f translate(vec3f translation) {
        return translate(translation.x, translation.y, translation.z);
    }

    // returns the matrix scaled
    public mat4f scaleBy(float s) {
        return mul(scale(s, s, s));
    }

    // returns the matrix scaled
    public mat4f scaleBy(float x, float y, float z) {
        return mul(scale(x, y, z));
    }

    // returns the matrix scaled
    public mat4f scaleBy(vec3f scale) {
        return scaleBy(scale.x, scale.y, scale.z);
    }

    // returns the matrix rotated about `axis` by `theta` radians
    public mat4f rotate(vec3f axis, float theta) {
        return mul(rotation(axis, theta));
    }

    // returns the product of this matrix and `m`
    public mat4f mul(mat4f m) {
        var result = new mat4f();

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

    // returns the application of the matrix to 4D vector `v` with W component `w`
    public vec3f mul(vec3f v, float w) {
        float x = v.x, y = v.y, z = v.z;

        return new vec3f(
                el[ 0] * x + el[ 1] * y + el[ 2] * z + el[ 3] * w,
                el[ 4] * x + el[ 5] * y + el[ 6] * z + el[ 7] * w,
                el[ 8] * x + el[ 9] * y + el[10] * z + el[11] * w
        );
    }

    // returns the application of the matrix to 4D vector `v` with W component 0
    public vec3f mul(vec3f v) {
        return mul(v, 0);
    }

    // returns the application of the matrix to 4D vector `v` with W component 1
    public vec3f apply(vec3f v) {
        return mul(v, 1);
    }

    @Override
    public String toString() {
        var colA = new String[4];
        var colB = new String[4];
        var colC = new String[4];
        var colD = new String[4];
        var cols = new String[][] { colA, colB, colC, colD };
        var lens = new int[] { 0, 0, 0, 0 };
        var result = new StringBuilder();

        for (int x = 0; x < 4; ++x) {
            for (int y = 0; y < 4; ++y) {
                var s = String.valueOf(el[4 * y + x]);

                cols[x][y] = s;
                lens[x] = Math.max(lens[x], s.length());
            }
        }

        for (int y = 0; y < 4; ++y) {
            result.append("[ ");

            for (int x = 0; x < 4; ++x) {
                result.append(cols[x][y]);

                for (int i = 0; i < lens[x] - cols[x][y].length(); ++i) {
                    result.append(" ");
                }

                if (x != 3) {
                    result.append(", ");
                }
            }

            result.append(" ]");

            if (y != 3) {
                result.append("\n");
            }
        }

        return result.toString();
    }
}
