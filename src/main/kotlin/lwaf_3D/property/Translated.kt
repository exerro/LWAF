package lwaf_3D.property

import lwaf_core.plus
import lwaf_core.vec3

interface Translated {
    val translation: vec3
}

interface MutableTranslated: Translated {
    override var translation: vec3
}

fun <T: MutableTranslated> T.translateTo(translation: vec3): T {
    this.translation = translation
    return this
}

fun <T: MutableTranslated> T.translateTo(x: Float, y: Float, z: Float): T
        = translateTo(vec3(x, y, z))

fun <T: MutableTranslated> T.translateBy(translation: vec3): T
        = translateTo(this.translation + translation)

fun <T: MutableTranslated> T.translateBy(x: Float, y: Float, z: Float): T
        = translateBy(vec3(x, y, z))
