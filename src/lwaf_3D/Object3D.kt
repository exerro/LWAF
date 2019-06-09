package lwaf_3D

import lwaf_3D.property.*
import lwaf_core.*

interface Object3D: Positioned, Rotated, Scaled {
    fun draw(context: DrawContext3D, shader: GLShaderProgram)
}

interface MutableObject3D: Object3D, MutablePositioned, MutableRotated, MutableScaled
