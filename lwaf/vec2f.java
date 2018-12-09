package lwaf;

import static java.util.Objects.hash;

@SuppressWarnings({"unused", "WeakerAccess"})
public class vec2f {
    public final float x, y;

    public vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public vec2f() {
        this(0, 0);
    }

    public vec2f add(vec2f v) {
        return new vec2f(x + v.x, y + v.y);
    }

    public vec2f sub(vec2f v) {
        return new vec2f(x - v.x, y - v.y);
    }

    public vec2f unm() {
        return new vec2f(-x, -y);
    }

    public vec2f mul(vec2f v) {
        return new vec2f(x * v.x, y * v.y);
    }

    public vec2f mul(float s) {
        return new vec2f(x * s, y * s);
    }

    public vec2f div(float s) {
        return new vec2f(x / s, y / s);
    }

    public float dot(vec2f v) {
        return x * v.x + y * v.y;
    }

    public float length2() {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public vec2f unit() {
        float s = 1 / length();
        return new vec2f(x * s, y * s);
    }

    @Override public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override public boolean equals(Object other) {
        return other instanceof vec2f && x == ((vec2f) other).x && y == ((vec2f) other).y;
    }

    @Override public int hashCode() {
        return hash(x, y);
    }
}
