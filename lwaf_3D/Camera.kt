package lwaf_3D

import lwaf_core.*
import lwaf_core.mat4
import lwaf_core.vec3
import lwaf_core.vec4

class Camera(private var position: vec3) : IPositioned<Camera>, IRotated<Camera> {
    override val translation: vec3 get() = position
    private var _rotation: vec3 = vec3(0f)
    override val rotation: vec3 get() = _rotation
    private var projection: Projection? = null

    private val transformationMatrix: mat4
        get() = mat4_translate(position) * (
                mat3_rotate(_rotation.y, vec3(0f, 1f, 0f)) *
                mat3_rotate(_rotation.x, vec3(1f, 0f, 0f)) *
                mat3_rotate(_rotation.z, vec3(0f, 0f, 1f))).mat4()

    val forward: vec3
        get() = (transformationMatrix * vec4(0f, 0f, -1f, 0f)).vec3()

    val right: vec3
        get() = (transformationMatrix * vec4(1f, 0f, 0f, 0f)).vec3()

    val up: vec3
        get() = (transformationMatrix * vec4(0f, 1f, 0f, 0f)).vec3()

    val flatForward: vec3
        get() {
            val (x, _, z) = forward
            return vec3(x, 0f, z).normalise()
        }

    val flatRight: vec3
        get() {
            val (x, _, z) = right
            return vec3(x, 0f, z).normalise()
        }

    val flatUp: vec3
        get() = vec3(0f, 1f, 0f)

    val viewMatrix: mat4
        get() = (mat3_rotate(-_rotation.z, vec3(0f, 0f, 1f)) *
                 mat3_rotate(-_rotation.x, vec3(1f, 0f, 0f)) *
                 mat3_rotate(-_rotation.y, vec3(0f, 1f, 0f))).mat4() *
                mat4_translate(-position)

    val projectionMatrix: mat4
        get() = projection!!.matrix

    fun setProjection(projection: Projection): Camera {
        this.projection = projection
        return this
    }

    fun setPerspectiveProjection(aspect: Float, FOV: Float, near: Float, far: Float): Camera {
        projection = PerspectiveProjection(aspect, FOV, near, far)
        return this
    }

    fun setOrthographicProjection(aspect: Float, near: Float, far: Float): Camera {
        projection = OrthographicProjection(aspect, near, far)
        return this
    }

    override fun setTranslation(translation: vec3): Camera {
        this.position = translation
        return this
    }

    override fun setRotation(rotation: vec3): Camera {
        this._rotation = rotation
        return this
    }

    abstract class Projection {
        abstract val matrix: mat4
    }

    class PerspectiveProjection(aspect: Float, FOV: Float, near: Float, far: Float) : Projection() {
        override val matrix: mat4

        init {
            val S = (1 / Math.tan(FOV * Math.PI / 360)).toFloat()

            matrix = mat4(
                    S / aspect, 0f, 0f, 0f,
                    0f, S, 0f, 0f,
                    0f, 0f, -(far + near) / (far - near), -2f * far * near / (far - near),
                    0f, 0f, -1f, 0f
            )
        }

        companion object {
            var DEFAULT_FOV = 60f
            var DEFAULT_NEAR = 0.1f
            var DEFAULT_FAR = 1000.0f
        }
    }

    class OrthographicProjection(aspect: Float, near: Float, far: Float) : Projection() {

        override val matrix: mat4

        init {
            matrix = mat4(
                    1 / aspect, 0f, 0f, 0f,
                    0f, 1f, 0f, 0f,
                    0f, 0f, -1 / (far - near), near, // -(far+near)/(far-near), -2*far*near/(far-near),
                    0f, 0f, 0f, 1f
            )
        }

        companion object {
            var DEFAULT_NEAR = 0.1f
            var DEFAULT_FAR = 1000.0f
        }
    }
}
