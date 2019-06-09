package lwaf_3D

import lwaf_3D.property.MutablePositioned
import lwaf_3D.property.MutableRotated
import lwaf_core.*

class Camera(position: vec3 = vec3(0f)) : MutablePositioned, MutableRotated {
    override var rotation: vec3 = vec3(0f)
    override var translation: vec3 = position
    private lateinit var projection: Projection

    val forward: vec3
        get() = rotation.toRotationMatrix() * vec3(0f, 0f, -1f)

    val right: vec3
        get() = rotation.toRotationMatrix() * vec3(1f, 0f, 0f)

    val up: vec3
        get() = rotation.toRotationMatrix() * vec3(0f, 1f, 0f)

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
        get() = (rotation.toInverseRotationMatrix()).mat4() * mat4_translate(-position)

    val projectionMatrix: mat4
        get() = projection.matrix

    fun setProjection(projection: Projection): Camera {
        this.projection = projection
        return this
    }

    fun setPerspectiveProjection(
            aspect: Float,
            FOV: Float = PerspectiveProjection.DEFAULT_FOV,
            near: Float = PerspectiveProjection.DEFAULT_NEAR,
            far: Float = PerspectiveProjection.DEFAULT_FAR
    ): Camera {
        projection = PerspectiveProjection(aspect, FOV, near, far)
        return this
    }

    fun setOrthographicProjection(
            aspect: Float,
            near: Float = OrthographicProjection.DEFAULT_NEAR,
            far: Float = OrthographicProjection.DEFAULT_FAR
    ): Camera {
        projection = OrthographicProjection(aspect, near, far)
        return this
    }

    abstract class Projection {
        abstract val matrix: mat4
    }
}

class PerspectiveProjection(aspect: Float, FOV: Float, near: Float, far: Float) : Camera.Projection() {
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

class OrthographicProjection(aspect: Float, near: Float, far: Float) : Camera.Projection() {
    override val matrix: mat4 = mat4(
            1 / aspect, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, -1 / (far - near), near, // -(far+near)/(far-near), -2*far*near/(far-near),
            0f, 0f, 0f, 1f
    )

    companion object {
        var DEFAULT_NEAR = 0.1f
        var DEFAULT_FAR = 1000.0f
    }
}