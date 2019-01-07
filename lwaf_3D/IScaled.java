package lwaf_3D;

import lwaf.vec3f;

public interface IScaled<R> {
    vec3f getScale();

    R setScale(vec3f scale);

    default R setScale(float x, float y, float z) {
        return setScale(new vec3f(x, y, z));
    }

    default R setScale(float scale) {
        return setScale(scale, scale, scale);
    }

    default R scaleBy(vec3f scale) {
        return setScale(getScale().mul(scale));
    }

    default R scaleBy(float x, float y, float z) {
        return scaleBy(new vec3f(x, y, z));
    }

    default R scaleBy(float scale) {
        return scaleBy(scale, scale, scale);
    }
}
