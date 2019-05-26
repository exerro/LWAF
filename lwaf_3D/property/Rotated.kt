package lwaf_3D.property

import lwaf_core.plus
import lwaf_core.vec3

interface Rotated {
    val rotation: vec3
}

interface MutableRotated: Rotated {
    override var rotation: vec3
}

fun <T: MutableRotated> T.rotateTo(rotation: vec3): T {
    this.rotation = rotation
    return this
}

fun <T: MutableRotated> T.rotateTo(x: Float, y: Float, z: Float): T
        = rotateTo(vec3(x, y, z))

fun <T: MutableRotated> T.rotateBy(rotation: vec3): T
        = rotateTo(this.rotation + rotation)

fun <T: MutableRotated> T.rotateBy(x: Float, y: Float, z: Float): T
        = rotateBy(vec3(x, y, z))
