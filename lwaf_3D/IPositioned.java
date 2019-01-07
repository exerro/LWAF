package lwaf_3D;

import lwaf.vec3f;

public interface IPositioned<R> extends ITranslated<R> {
    default R setPosition(vec3f position) {
        return setTranslation(position);
    }

    default R moveBy(vec3f movement) {
        return translateBy(movement);
    }

    default R setPosition(float x, float y, float z) {
        return setTranslation(x, y, z);
    }

    default R moveBy(float x, float y, float z) {
        return translateBy(x, y, z);
    }
}
