package lwaf_3D

import lwaf_core.vec3

interface IPositioned<R> : ITranslated<R> {
    fun setPosition(position: vec3): R {
        return setTranslation(position)
    }

    fun moveBy(movement: vec3): R {
        return translateBy(movement)
    }

    fun setPosition(x: Float, y: Float, z: Float): R {
        return setTranslation(x, y, z)
    }

    fun moveBy(x: Float, y: Float, z: Float): R {
        return translateBy(x, y, z)
    }
}
