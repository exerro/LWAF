package lwaf_3D;

import lwaf_core.vec3;

@SuppressWarnings("unused")
public interface ITranslated<R> {
    vec3 getTranslation();
    R setTranslation(vec3 translation);

    default R translateBy(vec3 translation) {
        return setTranslation(getTranslation().add(translation));
    }

    default R setTranslation(float x, float y, float z) {
        return setTranslation(new vec3(x, y, z));
    }

    default R translateBy(float x, float y, float z) {
        return setTranslation(getTranslation().add(new vec3(x, y, z)));
    }
}
