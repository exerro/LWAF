package lwaf_3D.property

import lwaf_core.times
import lwaf_core.vec3

interface Scaled {
    val scale: vec3
}

interface MutableScaled {
    var scale: vec3
}

fun <T: MutableScaled> T.scaleTo(scale: vec3): T {
    this.scale = scale
    return this
}

fun <T: MutableScaled> T.scaleTo(x: Float, y: Float, z: Float): T
        = scaleTo(vec3(x, y, z))

fun <T: MutableScaled> T.scaleTo(x: Float): T
        = scaleTo(vec3(x))

fun <T: MutableScaled> T.scaleBy(scale: vec3): T
        = scaleTo(this.scale * scale)

fun <T: MutableScaled> T.scaleBy(x: Float, y: Float, z: Float): T
        = scaleBy(vec3(x, y, z))

fun <T: MutableScaled> T.scaleBy(x: Float): T
        = scaleBy(vec3(x))
