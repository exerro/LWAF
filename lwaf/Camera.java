package lwaf;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class Camera implements ITranslated<Camera>, IRotated<Camera> {
    private vec3f position;
    private vec3f rotation;
    private Projection projection;

    public Camera(vec3f position) {
        this.position = position;
        this.rotation = vec3f.zero;
    }

    public mat4f getViewMatrix() {
        return mat4f.identity()
                    .translate(position.unm())
                    .rotate(vec3f.y_axis, -rotation.y)
                    .rotate(vec3f.x_axis, -rotation.x)
                    .rotate(vec3f.z_axis, -rotation.z)
                    ;
    }

    public mat4f getProjectionMatrix() {
        return projection.getMatrix();
    }

    public Camera setProjection(Projection projection) {
        this.projection = projection;
        return this;
    }

    public Camera setPerspectiveProjection(float aspect, float FOV, float near, float far) {
        projection = new PerspectiveProjection(aspect, FOV, near, far);
        return this;
    }

    public Camera setOrthographicProjection(float aspect, float near, float far) {
        projection = new OrthographicProjection(aspect, near, far);
        return this;
    }

    @Override
    public vec3f getTranslation() {
        return position;
    }

    @Override
    public Camera setTranslation(vec3f translation) {
        this.position = translation;
        return this;
    }

    @Override
    public vec3f getRotation() {
        return rotation;
    }

    @Override
    public Camera setRotation(vec3f rotation) {
        this.rotation = rotation;
        return this;
    }

    public static abstract class Projection {
        protected abstract mat4f getMatrix();
    }

    public static class PerspectiveProjection extends Projection {
        public static float DEFAULT_FOV = 70;
        public static float DEFAULT_NEAR = 0.1f;
        public static float DEFAULT_FAR = 1000.0f;

        private final mat4f matrix;

        public PerspectiveProjection(float aspect, float FOV, float near, float far) {
            float S = (float) (1 / Math.tan(FOV * Math.PI / 360));

            matrix = new mat4f(new float[] {
                    S / aspect, 0, 0, 0,
                    0, S, 0, 0,
                    0, 0, -(far+near)/(far-near), -2*far*near/(far-near),
                    0, 0, -1, 0
            });
        }

        @Override
        protected mat4f getMatrix() {
            return matrix;
        }
    }

    public static class OrthographicProjection extends Projection {
        public static float DEFAULT_NEAR = 0.1f;
        public static float DEFAULT_FAR = 1000.0f;

        private final mat4f matrix;

        public OrthographicProjection(float aspect, float near, float far) {
            matrix = new mat4f(new float[] {
                    1 / aspect, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, -1/(far-near), near, // -(far+near)/(far-near), -2*far*near/(far-near),
                    0, 0, 0, 1
            });
        }

        @Override
        protected mat4f getMatrix() {
            return matrix;
        }
    }
}
