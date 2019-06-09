package lwaf_3D.property

import lwaf_core.vec3

interface Positioned: Translated {
    val position: vec3
        get() = translation
}

interface MutablePositioned: MutableTranslated, Positioned {
    override var position: vec3
        get() = translation
        set(value) { translation = value }
}

fun <T: MutablePositioned> T.moveTo(position: vec3): T
        = translateTo(position)

fun <T: MutablePositioned> T.moveTo(x: Float, y: Float, z: Float): T
        = translateTo(x, y, z)

fun <T: MutablePositioned> T.moveBy(position: vec3): T
        = translateBy(position)

fun <T: MutablePositioned> T.moveBy(x: Float, y: Float, z: Float): T
        = translateBy(x, y, z)
