package lwaf;

@SuppressWarnings("unused")
public interface ITranslated<R> {
    vec3f getTranslation();
    R setTranslation(vec3f translation);

    default R translateBy(vec3f translation) {
        return setTranslation(getTranslation().add(translation));
    }

    default R setTranslation(float x, float y, float z) {
        return setTranslation(new vec3f(x, y, z));
    }

    default R translateBy(float x, float y, float z) {
        return setTranslation(getTranslation().add(new vec3f(x, y, z)));
    }
}
