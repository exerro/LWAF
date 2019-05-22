package lwaf_3D

import lwaf_core.plus
import lwaf_core.vec3

interface ITranslated<R> {
    val translation: vec3
    fun setTranslation(translation: vec3): R

    fun translateBy(translation: vec3): R
            = setTranslation(this.translation + translation)

    fun setTranslation(x: Float, y: Float, z: Float): R
             = setTranslation(vec3(x, y, z))

    fun translateBy(x: Float, y: Float, z: Float): R
            = setTranslation(translation + vec3(x, y, z))
}
