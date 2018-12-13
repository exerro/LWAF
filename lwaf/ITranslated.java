package lwaf;

@SuppressWarnings("unused")
public interface ITranslated<R> {
    vec3f getTranslation();
    R setTranslation(vec3f translation);

    default R translateBy(vec3f translation) {
        return setTranslation(getTranslation().add(translation));
    }
}
