package lwaf_3D;

import lwaf_core.vec3;

public interface IScaled<R> {
    vec3 getScale();

    R setScale(vec3 scale);

    default R setScale(float x, float y, float z) {
        return setScale(new vec3(x, y, z));
    }

    default R setScale(float scale) {
        return setScale(scale, scale, scale);
    }

    default R scaleBy(vec3 scale) {
        return setScale(getScale().mul(scale));
    }

    default R scaleBy(float x, float y, float z) {
        return scaleBy(new vec3(x, y, z));
    }

    default R scaleBy(float scale) {
        return scaleBy(scale, scale, scale);
    }
}
