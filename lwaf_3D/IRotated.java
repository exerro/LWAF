package lwaf_3D;

import lwaf_core.vec3;

@SuppressWarnings("UnusedReturnValue")
public interface IRotated<R> {
    vec3 getRotation();
    R setRotation(vec3 rotation);

    default R rotateBy(vec3 rotation) {
        return setRotation(getRotation().add(rotation));
    }

    default R setRotation(float x, float y, float z) {
        return setRotation(new vec3(x, y, z));
    }

    default R rotateBy(float x, float y, float z) {
        return setRotation(getRotation().add(new vec3(x, y, z)));
    }
}
