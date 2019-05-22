package lwaf_3D

import lwaf_core.times
import lwaf_core.vec3

interface IScaled<R> {
    val scale: vec3

    fun setScale(scale: vec3): R

    fun setScale(x: Float, y: Float, z: Float): R {
        return setScale(vec3(x, y, z))
    }

    fun setScale(scale: Float): R {
        return setScale(scale, scale, scale)
    }

    fun scaleBy(scale: vec3): R {
        return setScale(this.scale * scale)
    }

    fun scaleBy(x: Float, y: Float, z: Float): R {
        return scaleBy(vec3(x, y, z))
    }

    fun scaleBy(scale: Float): R {
        return scaleBy(scale, scale, scale)
    }
}
