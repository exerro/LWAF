package lwaf_3D

import lwaf_core.plus
import lwaf_core.vec3

interface IRotated<R> {
    val rotation: vec3
    fun setRotation(rotation: vec3): R

    fun rotateBy(rotation: vec3): R {
        return setRotation(this.rotation + rotation)
    }

    fun setRotation(x: Float, y: Float, z: Float): R {
        return setRotation(vec3(x, y, z))
    }

    fun rotateBy(x: Float, y: Float, z: Float): R {
        return setRotation(rotation + vec3(x, y, z))
    }
}
