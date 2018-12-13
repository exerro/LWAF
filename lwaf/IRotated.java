package lwaf;

@SuppressWarnings("UnusedReturnValue")
public interface IRotated<R> {
    vec3f getRotation();
    R setRotation(vec3f rotation);

    default R rotateBy(vec3f rotation) {
        return setRotation(getRotation().add(rotation));
    }
}
