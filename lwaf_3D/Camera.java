package lwaf_3D;

import lwaf_core.MatrixKt;
import lwaf_core.mat4;
import lwaf_core.vec3;
import lwaf_core.vec4;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class Camera implements IPositioned<Camera>, IRotated<Camera> {
    private vec3 position;
    private vec3 rotation;
    private Projection projection;

    public Camera(vec3 position) {
        this.position = position;
        this.rotation = new vec3(0, 0, 0);
    }

    private mat4 getTransformationMatrix() {
        return MatrixKt.mat4_translate(position)
                .mul(MatrixKt.mat4_rotation(rotation.getY(), new vec3(0, 1, 0)))
                .mul(MatrixKt.mat4_rotation(rotation.getX(), new vec3(1, 0, 0)))
                .mul(MatrixKt.mat4_rotation(rotation.getZ(), new vec3(0, 0, 1)));
    }

    public vec3 getForward() {
        return getTransformationMatrix().mul(new vec4(0, 0, -1, 0)).vec3();
    }

    public vec3 getRight() {
        return getTransformationMatrix().mul(new vec4(1, 0, 0, 0)).vec3();
    }

    public vec3 getUp() {
        return getTransformationMatrix().mul(new vec4(0, 1, 0, 0)).vec3();
    }

    public vec3 getFlatForward() {
        var fwd = getForward();
        return new vec3(fwd.getX(), 0, fwd.getZ()).normalise();
    }

    public vec3 getFlatRight() {
        var fwd = getRight();
        return new vec3(fwd.getX(), 0, fwd.getZ()).normalise();
    }

    public vec3 getFlatUp() {
        return new vec3(0, 1, 0);
    }

    public mat4 getViewMatrix() {
        return MatrixKt.mat4_rotation(-rotation.getZ(), new vec3(0, 0, 1))
                .mul(MatrixKt.mat4_rotation(-rotation.getX(), new vec3(1, 0, 0)))
                .mul(MatrixKt.mat4_rotation(-rotation.getY(), new vec3(0, 1, 0)))
                .mul(MatrixKt.mat4_translate(position.unm()));
    }

    public mat4 getProjectionMatrix() {
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
    public vec3 getTranslation() {
        return position;
    }

    @Override
    public Camera setTranslation(vec3 translation) {
        this.position = translation;
        return this;
    }

    @Override
    public vec3 getRotation() {
        return rotation;
    }

    @Override
    public Camera setRotation(vec3 rotation) {
        this.rotation = rotation;
        return this;
    }

    public static abstract class Projection {
        protected abstract mat4 getMatrix();
    }

    public static class PerspectiveProjection extends Projection {
        public static float DEFAULT_FOV = 60;
        public static float DEFAULT_NEAR = 0.1f;
        public static float DEFAULT_FAR = 1000.0f;

        private final mat4 matrix;

        public PerspectiveProjection(float aspect, float FOV, float near, float far) {
            var S = (float) (1 / Math.tan(FOV * Math.PI / 360));

            matrix = new mat4(
                    S / aspect, 0, 0, 0,
                    0, S, 0, 0,
                    0, 0, -(far+near)/(far-near), -2*far*near/(far-near),
                    0, 0, -1, 0
            );
        }

        @Override
        protected mat4 getMatrix() {
            return matrix;
        }
    }

    public static class OrthographicProjection extends Projection {
        public static float DEFAULT_NEAR = 0.1f;
        public static float DEFAULT_FAR = 1000.0f;

        private final mat4 matrix;

        public OrthographicProjection(float aspect, float near, float far) {
            matrix = new mat4(
                    1 / aspect, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, -1/(far-near), near, // -(far+near)/(far-near), -2*far*near/(far-near),
                    0, 0, 0, 1
            );
        }

        @Override
        protected mat4 getMatrix() {
            return matrix;
        }
    }
}
