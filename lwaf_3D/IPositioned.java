package lwaf_3D;

import lwaf_core.vec3;

public interface IPositioned<R> extends ITranslated<R> {
    default R setPosition(vec3 position) {
        return setTranslation(position);
    }

    default R moveBy(vec3 movement) {
        return translateBy(movement);
    }

    default R setPosition(float x, float y, float z) {
        return setTranslation(x, y, z);
    }

    default R moveBy(float x, float y, float z) {
        return translateBy(x, y, z);
    }
}
