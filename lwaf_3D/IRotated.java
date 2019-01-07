package lwaf_3D;

import lwaf.vec3f;

@SuppressWarnings("UnusedReturnValue")
public interface IRotated<R> {
    vec3f getRotation();
    R setRotation(vec3f rotation);

    default R rotateBy(vec3f rotation) {
        return setRotation(getRotation().add(rotation));
    }

    default R setRotation(float x, float y, float z) {
        return setRotation(new vec3f(x, y, z));
    }

    default R rotateBy(float x, float y, float z) {
        return setRotation(getRotation().add(new vec3f(x, y, z)));
    }
}
