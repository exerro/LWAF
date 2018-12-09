package lwaf;

import static java.util.Objects.hash;

@SuppressWarnings({"unused", "WeakerAccess"})
public class vec3f {
    public final float x, y, z;

    public vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public vec3f(vec2f v, float z) {
        this(v.x, v.y, z);
    }

    public vec3f() {
        this(0, 0, 0);
    }

    public vec3f add(vec3f v) {
        return new vec3f(x + v.x, y + v.y, z + v.z);
    }

    public vec3f sub(vec3f v) {
        return new vec3f(x - v.x, y - v.y, z - v.z);
    }

    public vec3f unm() {
        return new vec3f(-x, -y, -z);
    }

    public vec3f mul(vec3f v) {
        return new vec3f(x * v.x, y * v.y, z * v.z);
    }

    public vec3f mul(float s) {
        return new vec3f(x * s, y * s, z * s);
    }

    public vec3f div(float s) {
        return new vec3f(x / s, y / s, z / s);
    }

    public float dot(vec3f v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public float length2() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public vec3f unit() {
        float s = 1 / length();
        return new vec3f(x * s, y * s, z * s);
    }

    @Override public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override public boolean equals(Object other) {
        return other instanceof vec3f && x == ((vec3f) other).x && y == ((vec3f) other).y && z == ((vec3f) other).z;
    }

    @Override public int hashCode() {
        return hash(x, y, z);
    }
}
